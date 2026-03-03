package com.wendao.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final int code;
    private final String message;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ApiException(String message) {
        this(500, message);
    }

    public static class ErrorCode {
        public static final int SUCCESS = 200;
        public static final int INVALID_PARAM = 400;
        public static final int SIGN_ERROR = 401;
        public static final int TIMESTAMP_ERROR = 402;
        public static final int DUPLICATE_ORDER = 403;
        public static final int AMOUNT_ERROR = 404;
        public static final int SYSTEM_ERROR = 500;
    }
}
