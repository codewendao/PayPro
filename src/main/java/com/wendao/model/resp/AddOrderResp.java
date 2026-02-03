package com.wendao.model.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class AddOrderResp {

    /**
     * 唯一标识
     */
    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "金额")
    private BigDecimal money;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "支付随机码")
    private String payNum;

    /**
     * 是否自定义输入
     */
    @ApiModelProperty(value = "是否自定义输入,如果是自定义输入的话，在微信支付下需要使用customer的二维码，如果不是自定义输入，则寻找静态二维码")
    private Boolean custom;

    /**
     * 支付二维码标识
     */
    private Integer payQrNum;
}
