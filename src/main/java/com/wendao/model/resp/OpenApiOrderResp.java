package com.wendao.model.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenApiOrderResp implements Serializable {

    private String orderId;

    private String orderNo;

    private BigDecimal amount;

    private String payType;

    private String payNum;

    private Integer state;

    private String message;

    private Long timestamp;

    private String qrCodeUrl;

    private String returnUrl;
}
