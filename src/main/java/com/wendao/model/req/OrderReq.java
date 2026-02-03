package com.wendao.model.req;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderReq implements Serializable {

    private String nickName;

    private Long productId;

    @NotNull(message = "金额不能为空")
    private BigDecimal money;

    /**
     * 描述
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
     * 是否自定义输入
     */
    private Boolean custom;

    private String userId;

    /**
     * 支付设备是否为移动端
     */
    private Boolean mobile;

    /**
     * 用户支付设备信息
     */
    private String device;

    /**
     * 生成二维码编号标识token
     */
    private String tokenNum;

    private String time;

    @JsonIgnore
    private String passUrl;

    /**
     * 含小程序
     */
    @JsonIgnore
    private String passUrl2;

    /**
     * 含xboot
     */
    @JsonIgnore
    private String passUrl3;

    @JsonIgnore
    private String backUrl;

    @JsonIgnore
    private String editUrl;

    @JsonIgnore
    private String delUrl;

    @JsonIgnore
    private String closeUrl;

    @JsonIgnore
    private String statistic;

}
