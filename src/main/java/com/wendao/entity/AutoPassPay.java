package com.wendao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lld
 */
@TableName("t_auto_pass_pay")
@Data
public class AutoPassPay implements Serializable{

    /**
     * 唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 支付订单id*/
    private String orderId;

    private Long messageId;

    private Date messageCreateTime;

    private Date createTime;

    private String messageDesc;

}
