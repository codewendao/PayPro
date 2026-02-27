package com.wendao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lld
 */
@TableName("t_order")
@Data
public class Order implements Serializable{

    /**
     * 唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    private String nickName;

    private BigDecimal money;

    private String userId;

    /**
     * 留言
     */
    private String description;

    private Date createTime;

    private Date updateTime;

    /**
     * 用户通知邮箱
     */
    private String email;

    private String testEmail;

    /**
     * 显示状态 0待审核 1确认显示 2驳回 3通过不展示 4已扫码
     */
    private Integer state=0;

    private String payType;

    /**
     * 支付标识
     */
    private String payNum;

    /**
     * 支付二维码标识
     */
    private Integer payQrNum;

    /**
     * 是否自定义输入
     */
    private Boolean custom;

    /**
     * 支付设备是否为移动端
     */
    private Boolean mobile;

    /**
     * 用户支付设备信息
     */
    private String device;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 生成二维码编号标识token
     */
    @TableField(exist = false)
    private String tokenNum;

    @TableField(exist = false)
    private String time;

    @TableField(exist = false)
    @JsonIgnore
    private String passUrl;

    /**
     * 含小程序
     */
    @TableField(exist = false)
    @JsonIgnore
    private String passUrl2;

    /**
     * 含xboot
     */
    @TableField(exist = false)
    @JsonIgnore
    private String passUrl3;

    @TableField(exist = false)
    @JsonIgnore
    private String backUrl;

    @TableField(exist = false)
    @JsonIgnore
    private String editUrl;

    @TableField(exist = false)
    @JsonIgnore
    private String delUrl;

    @TableField(exist = false)
    @JsonIgnore
    private String closeUrl;

    @TableField(exist = false)
    @JsonIgnore
    private String statistic;

}
