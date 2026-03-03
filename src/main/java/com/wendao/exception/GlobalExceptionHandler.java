package com.wendao.exception;

import com.wendao.model.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseVO<?> handleApiException(ApiException e) {
        log.warn("API异常 - 错误码: {}, 错误信息: {}", e.getCode(), e.getMessage());
        return ResponseVO.builder()
                .code(e.getCode())
                .msg(e.getMessage())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseVO<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return ResponseVO.builder()
                .code(ApiException.ErrorCode.INVALID_PARAM)
                .msg(e.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseVO<?> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseVO.builder()
                .code(ApiException.ErrorCode.SYSTEM_ERROR)
                .msg("系统内部异常")
                .build();
    }
}
