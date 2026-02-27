package com.wendao.service.impl;

import com.wendao.config.PayProConfig;
import com.wendao.entity.AutoPassPay;
import com.wendao.entity.Order;
import com.wendao.entity.PayChatMessage;
import com.wendao.mapper.AutoPassPayMapper;
import com.wendao.mapper.OrderMapper;
import com.wendao.mapper.PayChatMessageMapper;
import com.wendao.mapper.ProductMapper;
import com.wendao.dto.MsgContentsDTO;
import com.wendao.dto.WeChatMsgDTO;
import com.wendao.enums.OrderStatesEnum;
import com.wendao.model.req.GetOrderListReq;
import com.wendao.model.resp.AddOrderResp;
import com.wendao.model.resp.CountResp;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
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
            record.setTestEmail("");
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
    @Transactional
    public void autoPass(WeChatMsgDTO dto) {

        QueryWrapper<AutoPassPay> autoPassPayQueryWrapper = new QueryWrapper<>();
        autoPassPayQueryWrapper.lambda().eq(AutoPassPay::getMessageId, dto.getId());
        Long l = autoPassPayMapper.selectCount(autoPassPayQueryWrapper);
        if (l > 0) {
            return;
        }

        String desc = dto.getContents().extractRemark();
        Order byPayNum = null;
        if (StringUtils.isNotBlank(desc)) {
            /** 找到匹配的订单*/
            byPayNum = thisService.getByPayNum(desc,dto.getTime());
            thisService.pass(byPayNum.getId());
        }

        AutoPassPay autoPassPay = new AutoPassPay();
        autoPassPay.setId(snowflake.nextId());
        if (byPayNum != null) {
            autoPassPay.setOrderId(byPayNum.getId());
        }
        autoPassPay.setMessageCreateTime(dto.getTime());
        autoPassPay.setMessageId(dto.getId());
        autoPassPay.setMessageDesc(desc);
        autoPassPayMapper.insert(autoPassPay);
    }

    @Override
    @Transactional
    public void autoPass(PayChatMessage dto) {

        String desc = null;
        if (dto.getPlatformType().equals("weixin")) {
            MsgContentsDTO bean = JSONUtil.toBean(dto.getContents(), MsgContentsDTO.class);
            desc = bean.extractRemark();
            if (StringUtils.isBlank(desc)) {
                dto.setProcessStatus(2);
                payChatMessageMapper.updateById(dto);
                log.info("处理:{}.没有提取到备注" + JSONUtil.toJsonStr(dto));
                return;
            }
        }

        Order byPayNum = null;
        if (StringUtils.isNotBlank(desc)) {
            /** 找到匹配的订单*/
            byPayNum = thisService.getByPayNum(desc,dto.getTime());
            if (byPayNum != null){
                thisService.pass(byPayNum.getId());
                dto.setOrderId(byPayNum.getId());
                dto.setProcessStatus(1);
                payChatMessageMapper.updateById(dto);
            }

        }
    }

    /**
     * 拼接管理员链接
     */
    public Order getAdminUrl(Order pay, String id, String token, String myToken) {

        String pass = payProConfig.getSite() + "/order/pass?sendType=0&id=" + id + "&token=" + token + "&myToken=" + myToken;
        pay.setPassUrl(pass);

        String pass2 = payProConfig.getSite() + "/order/pass?sendType=1&id=" + id + "&token=" + token + "&myToken=" + myToken;
        pay.setPassUrl2(pass2);

        String pass3 = payProConfig.getSite() + "/order/pass?sendType=2&id=" + id + "&token=" + token + "&myToken=" + myToken;
        pay.setPassUrl3(pass3);

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
