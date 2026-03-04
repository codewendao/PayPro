package com.wendao.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OpenApiOrderReq implements Serializable {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @NotBlank(message = "支付方式不能为空")
    private String payType;

    private String nickName;

    private String description;

    private String email;

    private String notifyUrl;

    private String userId;

    private Long productId;

    @NotBlank(message = "签名不能为空")
    private String sign;

    private Long timestamp;


    /** 是否是自定义金额 */
    private Boolean custom;
}
