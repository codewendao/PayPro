package com.wendao.enums;


public enum OrderStatesEnum {

    //直接接口获取
    WAIT_PAY(0,"待支付"),

    SUCCESS_PAY(1,"支付成功"),

    FAIL_PAY(2,"支付失败"),

    SCANE_QR(4,"已扫码"),


    ;

    private Integer state;
    private String desc;

    public Integer getState() {
        return state;
    }

    public String getDesc() {
        return desc;
    }

    OrderStatesEnum(int state, String desc){
        this.state = state;
        this.desc = desc;
    }



}
