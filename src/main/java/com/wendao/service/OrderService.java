package com.wendao.service;

import com.wendao.entity.Order;
import com.wendao.model.req.OpenApiOrderReq;
import com.wendao.model.resp.CountResp;
import com.wendao.model.ResponseVO;
import com.wendao.model.req.GetOrderListReq;
import com.wendao.model.req.OrderReq;
import com.wendao.model.resp.AddOrderResp;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wendao.model.resp.OpenApiOrderResp;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * @author
 */
public interface OrderService {

    /**
     * 获得支付
     * @param id
     * @return
     */
    Order getOrderById(String id);

    /**
     * 添加支付
     * @param order
     * @return
     */
    int addOrder(Order order);

    /**
     * 编辑支付
     * @param order
     * @return
     */
    int updateOrder(Order order);

    /**
     * 状态改变
     * @param id
     * @param state
     * @return
     */
    int changeOrderState(String id,Integer state);

    /**
     * 删除除捐赠和审核中以外的数据支付
     * @param id
     * @return
     */
    int delOrder(String id);

    /**
     * 统计数据
     * @param type
     * @param start
     * @param end
     * @return
     */
    CountResp statistic(Integer type, String start, String end);

    ResponseVO<AddOrderResp> addOrder(OrderReq order, HttpServletRequest request);

    int pass(String id);

    IPage<Order> list(GetOrderListReq req);

    OpenApiOrderResp createOpenApiOrder(OpenApiOrderReq req);

    /**
     * 减额匹配: 根据实际支付金额查找订单(用于自动匹配)
     */
    Order getByActualAmount(BigDecimal actualAmount, Date startTime, Date endTime);

    /**
     * 获取指定基础金额的已占用减额槽位索引集合
     */
    Set<Integer> getOccupiedDecrementSlots(String payType, BigDecimal baseAmount);

}
