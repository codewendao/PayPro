package com.wendao.mapper;

import com.wendao.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author lld
 */
public interface OrderMapper extends BaseMapper<Order> {

    @Select("select * from t_order where state = #{state}")
    List<Order> getByStateIs(Integer state);

    List<Order> getByStateAndOrderType(Integer state, String type);

    @Select(value = "select sum(money) from t_order where state = 1")
    BigDecimal countAllMoney();

    @Select(value = "select sum(money) from t_order where state = 1 and pay_type = #{payType}")
    BigDecimal countAllMoneyByType(String payType);

    @Select(value = "select sum(money) from t_order where state = 1 and create_time between #{date1} and #{date2}")
    BigDecimal countMoney(Date date1, Date date2);

    @Select(value = "select sum(money) from t_order where state = 1 and pay_type = #{payType} and create_time between #{date1} and #{date2}")
    BigDecimal countMoneyByType(String payType, Date date1, Date date2);

    @Delete("DELETE FROM account WHERE money < #{money} AND state = #{state}")
    void deleteByMoneyLessThanAndState(BigDecimal money, Integer state);
}
