package com.wendao.service.impl;

import com.wendao.config.PayProConfig;
import com.wendao.entity.AutoPassPay;
import com.wendao.entity.Order;
import com.wendao.entity.PayChatMessage;
import com.wendao.exception.ApiException;
import com.wendao.mapper.AutoPassPayMapper;
import com.wendao.mapper.OrderMapper;
import com.wendao.mapper.PayChatMessageMapper;
import com.wendao.mapper.ProductMapper;
import com.wendao.dto.MsgContentsDTO;
import com.wendao.dto.WeChatMsgDTO;
import com.wendao.enums.OrderStatesEnum;
import com.wendao.model.req.GetOrderListReq;
import com.wendao.model.req.OpenApiOrderReq;
import com.wendao.model.resp.AddOrderResp;
import com.wendao.model.resp.CountResp;
import com.wendao.model.resp.OpenApiOrderResp;
import com.wendao.service.OrderService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import com.wendao.entity.Product;
import com.wendao.model.ResponseVO;
import com.wendao.common.utils.*;
import com.wendao.model.req.OrderReq;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wendao.utils.OpenApiSignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author lld
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    OrderService thisService;

    @Autowired
    PayProConfig payProConfig;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    AutoPassPayMapper autoPassPayMapper;

    @Autowired
    Snowflake snowflake;

    @Autowired
    PayChatMessageMapper payChatMessageMapper;

    @Autowired
    OpenApiSignUtil openApiSignUtil;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public Order getOrderById(String id) {
        Order byId = orderMapper.selectById(id);
        byId.setTime(StringUtils.getTimeStamp(byId.getCreateTime()));
        return byId;
    }

    @Override
    public int addOrder(Order pay) {
        pay.setId(UUID.randomUUID().toString().replace("-", ""));
        pay.setCreateTime(new Date());
        pay.setState(OrderStatesEnum.WAIT_PAY.getState());
        orderMapper.insert(pay);
        return 1;
    }

    @Override
    public int updateOrder(Order pay) {
        pay.setUpdateTime(new Date());
        orderMapper.updateById(pay);
        return 1;
    }

    @Override
    public int changeOrderState(String id, Integer state) {

        Order pay = getOrderById(id);
        pay.setState(state);
        pay.setUpdateTime(new Date());
        orderMapper.updateById(pay);
        return 1;
    }

    @Override
    public int delOrder(String id) {
        orderMapper.deleteById(id);
        return 1;
    }

    @Override
    public CountResp statistic(Integer type, String start, String end) {

        CountResp count = new CountResp();
        if (type == -1) {
            // 总
            count.setAmount(orderMapper.countAllMoney());
            count.setWeixin(orderMapper.countAllMoneyByType("Wechat"));
            count.setAlipay(orderMapper.countAllMoneyByType("Alipay"));
            return count;
        }
        Date startDate = null, endDate = null;
        if (type == 0) {
            // 今天
            startDate = DateUtils.getDayBegin();
            endDate = DateUtils.getDayEnd();
        }
        if (type == 6) {
            // 昨天
            startDate = DateUtils.getBeginDayOfYesterday();
            endDate = DateUtils.getEndDayOfYesterDay();
        } else if (type == 1) {
            // 本周
            startDate = DateUtils.getBeginDayOfWeek();
            endDate = DateUtils.getEndDayOfWeek();
        } else if (type == 2) {
            // 本月
            startDate = DateUtils.getBeginDayOfMonth();
            endDate = DateUtils.getEndDayOfMonth();
        } else if (type == 3) {
            // 本年
            startDate = DateUtils.getBeginDayOfYear();
            endDate = DateUtils.getEndDayOfYear();
        } else if (type == 4) {
            // 上周
            startDate = DateUtils.getBeginDayOfLastWeek();
            endDate = DateUtils.getEndDayOfLastWeek();
        } else if (type == 5) {
            // 上个月
            startDate = DateUtils.getBeginDayOfLastMonth();
            endDate = DateUtils.getEndDayOfLastMonth();
        } else if (type == -2) {
            // 自定义
            startDate = DateUtils.parseStartDate(start);
            endDate = DateUtils.parseEndDate(end);
        }
        count.setAmount(orderMapper.countMoney(startDate, endDate));
        count.setWeixin(orderMapper.countMoneyByType("Wechat", startDate, endDate));
        count.setAlipay(orderMapper.countMoneyByType("Alipay", startDate, endDate));
        return count;
    }

    @Override
    @Transactional
    public ResponseVO<AddOrderResp> addOrder(OrderReq req, HttpServletRequest request) {

        if(StringUtils.isBlank(String.valueOf(req.getMoney()))){
            return ResponseVO.errorResponse("请填写完整信息和正确金额");
        }

        String ip = IpInfoUtils.getIpAddr(request);

        Order entity = new Order();
        BeanUtils.copyProperties(req, entity);

        try {
            if (req.getCustom() != null && !req.getCustom()) {
                //自定义金额生成四位数随机标识
                int i = new Random().nextInt(payProConfig.getQrCodeNum()) + 1;
                entity.setPayQrNum(i);
            }

            entity.setPayNum(StringUtils.getRandomNum());

            thisService.addOrder(entity);
        } catch (Exception e) {
            log.error(e.toString());
            return ResponseVO.errorResponse("添加捐赠支付订单失败");
        }
        //记录缓存
        redisTemplate.opsForValue().set(ip, "added", payProConfig.getRateLimit().getIpExpire(), TimeUnit.MINUTES);

        //给管理员发送审核邮件
        String tokenAdmin = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(entity.getId(), tokenAdmin, payProConfig.getToken().getExpire(), TimeUnit.DAYS);
        entity = getAdminUrl(entity, entity.getId(), tokenAdmin, payProConfig.getToken().getValue());
        emailUtils.sendTemplateMail(payProConfig.getEmail().getSender(), payProConfig.getEmail().getReceiver(), "【" + payProConfig.getTitle() + "】待审核处理", "payment-review", entity);

        AddOrderResp addPayResp = new AddOrderResp();
        addPayResp.setId(entity.getId());
        addPayResp.setPayNum(entity.getPayNum());
        addPayResp.setPayType(entity.getPayType());
        addPayResp.setMoney(entity.getMoney());
        addPayResp.setPayQrNum(entity.getPayQrNum());
        addPayResp.setCustom(entity.getCustom());
        return ResponseVO.successResponse(addPayResp);
    }

    /**
     * 处理充值业务,直接调用游戏的接口。订单状态在游戏的地方修改
     */
    @Override
    @Transactional
    public int pass(String id) {
        //Pay pay = thisService.changePayState(id, 1);
        Order pay = orderMapper.selectById(id);
        if (pay.getProductId() != null) {
            Product product = productMapper.selectById(pay.getProductId());
            if (product.getType().equals("CODE")) {
                emailUtils.sendTemplateMail(payProConfig.getEmail().getSender(), pay.getEmail(), "【Pay个人收款支付系统】支付成功通知（附下载链接）",
                        "order-success", pay);
                pay.setState(OrderStatesEnum.SUCCESS_PAY.getState());
                orderMapper.updateById(pay);
            }
        }
        return 1;
    }

    @Override
    public IPage<Order> list(GetOrderListReq req) {

        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery();
        wrapper.in(Order::getState, req.getStates());
        if (StringUtils.isNotBlank(req.getOrderBy())) {

            if (req.getOrder().equals("createTime")) {
                if (req.getOrder().equals("desc")) {
                    wrapper.orderByDesc(Order::getCreateTime);
                } else {
                    wrapper.orderByAsc(Order::getCreateTime);
                }
            }

            if (req.getOrder().equals("money")) {
                if (req.getOrder().equals("desc")) {
                    wrapper.orderByDesc(Order::getMoney);
                } else {
                    wrapper.orderByAsc(Order::getMoney);
                }
            }
        }

        if (StringUtils.isNotBlank(req.getKeyword())) {
            //字符串类型的处理，统一全部like查询
            wrapper.like(Order::getEmail, req.getKeyword());
            wrapper.or().like(Order::getNickName, req.getKeyword());
        }
        Page<Order> page = new Page<>(req.getPageIndex(), req.getPageSize());
        Page<Order> payPage = orderMapper.selectPage(page, wrapper);

        for (Order record : payPage.getRecords()) {
            // 屏蔽隐私数据
            record.setId("");
            record.setEmail("");
            record.setPayNum(null);
            record.setMobile(null);
            record.setCustom(null);
            record.setDevice(null);
        }
        return payPage;
    }

    @Override
    public Order getByPayNum(String desc, Date time) {
        // 获取当天的00:00:00
        Date todayStart = DateUtil.beginOfDay(time);
        // 获取前一天的00:00:00
        Date yesterdayStart = DateUtil.beginOfDay(DateUtil.offsetDay(time, -1));

        QueryWrapper<Order> payQueryWrapper = new QueryWrapper<>();
        payQueryWrapper.lambda()
                .eq(Order::getPayNum, desc)
                // 添加时间范围条件：从前一天00:00:00到今天00:00:00
                .between(Order::getCreateTime, yesterdayStart, todayStart);
        return orderMapper.selectOne(payQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpenApiOrderResp createOpenApiOrder(OpenApiOrderReq req) {
        if (!openApiSignUtil.verifyTimestamp(req.getTimestamp())) {
            throw new ApiException(ApiException.ErrorCode.TIMESTAMP_ERROR, "请求时间戳无效或已过期");
        }

        if (!openApiSignUtil.verifySign(req)) {
            throw new ApiException(ApiException.ErrorCode.SIGN_ERROR, "签名验证失败");
        }

        if (req.getAmount() == null || req.getAmount().compareTo(new BigDecimal("0")) <= 0) {
            throw new ApiException(ApiException.ErrorCode.AMOUNT_ERROR, "金额必须大于0");
        }

        if (req.getAmount().compareTo(new BigDecimal("100000")) > 0) {
            throw new ApiException(ApiException.ErrorCode.AMOUNT_ERROR, "金额超出限制，单笔订单不能超过100000元");
        }

        Order existingOrder = orderMapper.selectById(req.getOrderNo());
        if (existingOrder != null) {
            throw new ApiException(ApiException.ErrorCode.DUPLICATE_ORDER, "订单号已存在");
        }

        Order order = new Order();
        order.setId(req.getOrderNo());
        order.setMoney(req.getAmount());
        order.setPayType(req.getPayType());
        order.setNickName(req.getNickName());
        order.setEmail(req.getEmail());
        order.setNotifyUrl(req.getNotifyUrl());
        order.setUserId(req.getUserId());
        order.setProductId(req.getProductId());
        order.setOrderSource("OPENAPI");
        order.setState(OrderStatesEnum.WAIT_PAY.getState());
        order.setCreateTime(new Date());
        order.setPayNum(StringUtils.getRandomNum());

        String qrUrl = "";

        int i = new Random().nextInt(payProConfig.getQrCodeNum()) + 1;
        order.setPayQrNum(i);
        /** 查看二维码是否存在 */
        boolean b = checkQrFileExists(req.getPayType(), req.getAmount(), i);

        // 格式化金额为两位小数
        String formattedAmount = String.format("%.2f", req.getAmount());

        /** 如果不存在 */
        if(!b) {
            qrUrl = payProConfig.getSite() + "/assets/qr/" + req.getPayType() + "/" +
                    formattedAmount + "/" + i + ".png";
            req.setCustom(false);
        } else {
            qrUrl = payProConfig.getSite() + "/assets/qr/" + req.getPayType() + "/" + "custom.png";
            req.setCustom(true);
        }
        // 获取支付类型配置
        Boolean useLocalQrCodeConfig = payProConfig.getUseLocalQrCode(req.getPayType());
        String returnUrl = payProConfig.getSite() + "/payment.html?" +
                "orderId=" + req.getOrderNo() +
                "&money=" + req.getAmount() +
                "&payType=" + req.getPayType() +
                "&payNum=" + order.getPayNum() +
                "&customerQr=" + req.getCustom() +
                "&picName=" + formattedAmount +
                "&qrCode=" + "undefined" +
                "&payQrNum=" + i +
                "&useLocalQrCode=" + useLocalQrCodeConfig;
        try {
            orderMapper.insert(order);
        } catch (Exception e) {
            log.error("创建OpenApi订单失败: {}", e.getMessage(), e);
            throw new ApiException(ApiException.ErrorCode.SYSTEM_ERROR, "创建订单失败");
        }

        return OpenApiOrderResp.builder()
                .orderId(order.getId())
                .orderNo(req.getOrderNo())
                .amount(req.getAmount())
                .payType(req.getPayType())
                .payNum(order.getPayNum())
                .state(order.getState())
                .message("订单创建成功")
                .qrCodeUrl(qrUrl)
                .returnUrl(returnUrl)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 检查指定支付类型和金额的二维码文件是否存在
     *
     * @param payType 支付类型，如 "wechat"、"alipay"，需与文件夹名称匹配
     * @param amount  金额，将格式化为两位小数作为文件夹名
     * @param qrNum   二维码编号，用于构建文件名
     * @return 文件存在返回 true，否则返回 false
     */
    private boolean checkQrFileExists(String payType, BigDecimal amount, int qrNum) {
        // 格式化金额为两位小数，与文件夹名称一致
        String amountStr = String.format("%.2f", amount);
        // 构建文件路径：static/qr/{payType}/{amount}/{qrNum}.png
        String filePath = "classpath:static/qr/" + payType.toLowerCase() + "/" + amountStr + "/" + qrNum + ".png";
        try {
            Resource resource = resourceLoader.getResource(filePath);
            return resource.exists();
        } catch (Exception e) {
            // 记录日志或处理异常
            return false;
        }
    }


    /**
     * 拼接管理员链接
     */
    public Order getAdminUrl(Order pay, String id, String token, String myToken) {

        String pass = payProConfig.getSite() + "/order/pass?id=" + id + "&token=" + token + "&myToken=" + myToken;
        pay.setPassUrl(pass);

        String back = payProConfig.getSite() + "/order/back?id=" + id + "&token=" + token + "&myToken=" + myToken;
        pay.setBackUrl(back);

        String edit = payProConfig.getSite() + "/order-edit?id=" + id + "&token=" + token;
        pay.setEditUrl(edit);

        String del = payProConfig.getSite() + "/order-del?id=" + id + "&token=" + token;
        pay.setDelUrl(del);

        String close = payProConfig.getSite() + "/order-close?id=" + id + "&token=" + token;
        pay.setCloseUrl(close);

        String statistic = payProConfig.getSite() + "/statistic?myToken=" + myToken;
        pay.setStatistic(statistic);
        return pay;
    }
}
