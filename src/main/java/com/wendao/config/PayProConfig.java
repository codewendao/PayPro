package com.wendao.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component("PayProConfig")
@ConfigurationProperties(prefix = "paypro")
@Data
public class PayProConfig implements WebMvcConfigurer {

    /**
     * 标题(浏览器上)
     * */
    private String title;

    /**
     * 标题（首页上）
     * */
    private String indexTitle;

    /**
     * 站点
     * */
    private String site;

    /**
     * 邮箱配置
     */
    private Email email = new Email();

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * token配置
     */
    private Token token = new Token();

    /**
     * 二维码数量配置
     */
    private Integer qrCodeNum;

    /** 项目下载地址 */
    private String downloadUrl;

    /**
     * 邮箱配置内部类
     */
    @Data
    public static class Email {
        /**
         * 收件人
         */
        private String receiver;

        /**
         * 发件人
         */
        private String sender;
    }

    /**
     * 邮箱配置内部类
     */
    @Data
    public static class RateLimit {
        /**
         * ip限流(分钟)
         */
        private Long ipExpire;

    }

    @Data
    public static class Token {
        /**
         * 过期时间单位天
         */
        private Long expire;

        /**
         * 加密token值
         */
        private String value;

    }

}

