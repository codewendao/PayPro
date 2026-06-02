package com.wendao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wendao.entity.Order;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author lld
 */
public interface OrderMapper extends BaseMapper<Order> {

    @Select("select * from t_order where state = #{state} "
            + "and ((expire_time is not null and expire_time < now()) "
            + "or (expire_time is null and create_time < #{expireTime}))")
    List<Order> getExpiredOrders(Integer state, Date expireTime);

    @Select(value = "select sum(money) from t_order where state = 1")
    BigDecimal countAllMoney();

    @Select(value = "select sum(money) from t_order where state = 1 and pay_type = #{payType}")
    BigDecimal countAllMoneyByType(String payType);

    @Select(value = "select sum(money) from t_order where state = 1 and create_time between #{date1} and #{date2}")
    BigDecimal countMoney(Date date1, Date date2);

    @Select(value = "select sum(money) from t_order where state = 1 and pay_type = #{payType} and create_time between #{date1} and #{date2}")
    BigDecimal countMoneyByType(String payType, Date date1, Date date2);

    /**
     * 根据实际金额和时间窗口查找减额模式订单(用于自动匹配)
     */
    @Select("SELECT * FROM t_order WHERE actual_amount = #{actualAmount} " +
            "AND match_mode = 'DECREMENT' " +
            "AND state IN (0, 4) " +
            "AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY create_time ASC LIMIT 1")
    Order getDecrementOrderByAmount(BigDecimal actualAmount, Date startTime, Date endTime);
}
