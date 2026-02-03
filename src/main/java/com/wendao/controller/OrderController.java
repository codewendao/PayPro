package com.wendao.controller;

import cn.hutool.log.StaticLog;
import com.wendao.config.PayProConfig;
import com.wendao.entity.Order;
import com.wendao.model.ResponseVO;
import com.wendao.common.utils.*;
import com.wendao.enums.OrderStatesEnum;
import com.wendao.model.req.GetOrderListReq;
import com.wendao.model.req.OrderReq;
import com.wendao.model.resp.AddOrderResp;
import com.wendao.service.OrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @author lld
 */
@Controller
@Api(tags = "开放接口",description = "订单管理")
@CacheConfig(cacheNames = "order")
public class OrderController {

    private static final Logger log= LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    PayProConfig payProConfig;

    @RequestMapping(value = "/order/list")
    @ApiOperation(value = "获取支付数据列表")
    @ResponseBody
    public ResponseVO<IPage<Order>> getOrderList(@RequestBody GetOrderListReq req){
        IPage<Order> ipage = orderService.list(req);
        return ResponseVO.successResponse(ipage);
    }

    @RequestMapping(value = "/order/state/{id}",method = RequestMethod.GET)
    @ApiOperation(value = "获取支付状态")
    @ResponseBody
    public ResponseVO getOrderState(@PathVariable String id){
        Order order=null;
        try {
            order = orderService.getOrderById(getOrderId(id));
        }catch (Exception e){
            return ResponseVO.errorResponse("获取支付数据失败");
        }
        return ResponseVO.successResponse(order.getState());
    }

    @RequestMapping(value = "/order/{id}",method = RequestMethod.GET)
    @ApiOperation(value = "获取支付数据")
    @ResponseBody
    public ResponseVO<Object> getPayData(@PathVariable String id,
                                     @RequestParam String token){

        /** 比对连接上的token和redis中的是否一致*/
        String temp = redisTemplate.opsForValue().get(id);
        if(!token.equals(temp)){
            return ResponseVO.errorResponse("无效的Token或链接");
        }
        Order order = null;
        try {
            order = orderService.getOrderById(getOrderId(id));
        }catch (Exception e){
            StaticLog.error(e);
            return ResponseVO.errorResponse("获取支付数据失败");
        }
        return ResponseVO.successResponse(order);
    }

    @RequestMapping(value = "/order/add",method = RequestMethod.POST)
    @ApiOperation(value = "添加支付订单")
    @ResponseBody
    public ResponseVO<AddOrderResp> addOrder(@RequestBody @Validated OrderReq orderReq, HttpServletRequest request){
        String agent = request.getHeader("user-agent");
        orderReq.setDevice(agent);
        return orderService.addOrder(orderReq, request);
    }

    @RequestMapping(value = "/order/edit",method = RequestMethod.POST)
    @ApiOperation(value = "编辑支付订单")
    @ResponseBody
    @CacheEvict(key = "#id")
    public ResponseVO editOrder(@ModelAttribute Order order,
                                  @RequestParam String id,
                                  @RequestParam String token){

        String temp = redisTemplate.opsForValue().get(id);
        if(!token.equals(temp)){
            return ResponseVO.errorResponse("无效的Token或链接");
        }

        try {
            order.setId(getOrderId(order.getId()));
            Order p = orderService.getOrderById(getOrderId(order.getId()));
            order.setState(p.getState());
            order.setCreateTime(StringUtils.getDate(order.getTime()));
            orderService.updateOrder(order);
        }catch (Exception e){
            return ResponseVO.errorResponse("编辑支付订单失败");
        }
        return ResponseVO.successResponse();
    }

    /**
     * @description 审核通过。
     * @param id, token, myToken, sendType, model
     * @return java.lang.String
    */
    @RequestMapping(value = "/order/pass",method = RequestMethod.GET)
    @ApiOperation(value = "审核通过支付订单")
    @CacheEvict(key = "#id")
    public String pass(@RequestParam String id,
                         @RequestParam String token,
                         @RequestParam String myToken,
                         @RequestParam String sendType,
                         Model model){

        String temp=redisTemplate.opsForValue().get(id);
        if(!token.equals(temp)){
            model.addAttribute("errorMsg","无效的Token或链接");
            return "error";
        }
        if(!myToken.equals(payProConfig.getToken().getValue())){
            model.addAttribute("errorMsg","您未通过二次验证");
            return "error";
        }
        try {
            //通知回调
            orderService.pass(id);
            StaticLog.info("通过了{}",id);
        }catch (Exception e){
            model.addAttribute("errorMsg","处理数据出错");
            return "error";
        }
        return "redirect:/success";
    }

    @RequestMapping(value = "/order/back",method = RequestMethod.GET)
    @ApiOperation(value = "审核驳回支付订单")
    @CacheEvict(key = "#id")
    public String backPay(@RequestParam String id,
                          @RequestParam String token,
                          @RequestParam String myToken,
                          Model model){

        String temp = redisTemplate.opsForValue().get(id);
        if(!token.equals(temp)){
            model.addAttribute("errorMsg","无效的Token或链接");
            return "error";
        }
        if(!myToken.equals(payProConfig.getToken().getValue())){
            model.addAttribute("errorMsg","您未通过二次验证");
            return "error";
        }
        try {
            orderService.changeOrderState(getOrderId(id), OrderStatesEnum.FAIL_PAY.getState());
            //通知回调
            Order order = orderService.getOrderById(getOrderId(id));
            if(StringUtils.isNotBlank(order.getEmail())&&EmailUtils.checkEmail(order.getEmail())){
                emailUtils.sendTemplateMail(payProConfig.getEmail().getSender(),order.getEmail(),"【Pay个人收款支付系统】支付失败通知","order-fail",order);
            }
        }catch (Exception e){
            model.addAttribute("errorMsg","处理数据出错");
            return "error";
        }
        return "redirect:/success";
    }

    @RequestMapping(value = "/order/del",method = RequestMethod.GET)
    @ApiOperation(value = "删除支付订单")
    @ResponseBody
    @CacheEvict(key = "#id")
    public ResponseVO<Object> dePay(@RequestParam String id,
                                 @RequestParam String token){

        String temp = redisTemplate.opsForValue().get(id);
        if(!token.equals(temp)){
            return ResponseVO.errorResponse("无效的Token或链接");
        }
        try {
            //通知回调
            Order order = orderService.getOrderById(getOrderId(id));
            if(StringUtils.isNotBlank(order.getEmail())&&EmailUtils.checkEmail(order.getEmail())){
                // 删除订单取消发邮件
            }
            orderService.delOrder(getOrderId(id));
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseVO.errorResponse("删除支付订单失败");
        }
        return ResponseVO.successResponse();
    }

    /**
     * 数据统计
     * @return
     */
    @RequestMapping(value = "/order/statistic",method = RequestMethod.POST)
    @ResponseBody
    public ResponseVO<Object> statistic(@RequestParam Integer type,
                                    @RequestParam(required = false) String start,
                                    @RequestParam(required = false) String end,
                                    @RequestParam String myToken){

        if(!payProConfig.getToken().getValue().equals(myToken)){
            return ResponseVO.errorResponse("二次密码验证不正确");
        }
        return ResponseVO.successResponse(orderService.statistic(type, start, end));
    }

    public String getOrderId(String id){
        return id;
    }
}
