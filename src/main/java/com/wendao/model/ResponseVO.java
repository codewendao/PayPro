package com.wendao.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;

    private String msg;

    private T data;

    public static ResponseVO successResponse(Object data) {
        return new ResponseVO(200, "success", data);
    }

    public static ResponseVO successResponse() {
        return new ResponseVO(200, "success");
    }

    public static ResponseVO errorResponse() {
        return new ResponseVO(500, "系统内部异常");
    }

    public static ResponseVO errorResponse(int code, String msg) {
        return new ResponseVO(code, msg);
    }

    public static ResponseVO errorResponse(String msg) {
        return new ResponseVO(500, msg);
    }

    public boolean isOk(){
        return this.code == 200;
    }


    public ResponseVO(int code, String msg) {
        this.code = code;
        this.msg = msg;
//		this.data = null;
    }

    public ResponseVO success(){
        this.code = 200;
        this.msg = "success";
        return this;
    }

    public ResponseVO success(T data){
        this.code = 200;
        this.msg = "success";
        this.data = data;
        return this;
    }

}
