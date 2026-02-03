package com.wendao.model.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CountResp {

    private BigDecimal amount = new BigDecimal("0.00");

    private BigDecimal weixin = new BigDecimal("0.00");

    private BigDecimal alipay = new BigDecimal("0.00");


}
