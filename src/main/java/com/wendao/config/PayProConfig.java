package com.wendao.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("PayConfig")
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
     * 阿里的用户id
     * */
    private String alipayUserId;

    /**
     * 阿里的自定义收款码
     * */
    private String alipayCustomQrUrl;

    /**
     * mobile
     * */
    private String mobile;

    /**
     * name
     * */
    private String name;

    /**
     * appId 支付宝后台看
     * */
    private String alipayDmfAppId;

    /**
     * 应用私钥，自己上传
     * */
    private String alipayDmfAppPrivateKey;

    /**
     * 阿里公钥
     * */
    private String alipayDmfPublicKey;

    /**
     * 阿里当面付主题
     * */
    private String alipayDmfSubject;

    /**
     * 支持邮箱
     * */
    private String supportMail;

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
     * 订单配置
     */
    private Order order = new Order();

    /**
     * 二维码数量配置
     */
    private Integer qrCodeNum;

    /** 项目下载地址 */
    private String downloadUrl;

    private List<PayMethod> payMethods;

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
         * ip限流(秒)
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

    @Data
    public static class Order {
        /**
         * 订单超时时间（分钟）
         */
        private Long timeoutMinutes = 30L;
    }

    @Data
    public static class PayMethod {
        private String id;
        private String name;
        private String description;
        private String icon;
        private boolean status;
        private boolean allowNight;
        private boolean useLocalQrCode;
    }

    // 根据支付类型ID获取useLocalQrCode
    public Boolean getUseLocalQrCode(String payType) {
        if (payMethods == null) {
            return false;
        }
        return payMethods.stream()
                .filter(method -> method.getId().equals(payType))
                .map(PayMethod::isUseLocalQrCode)
                .findFirst()
                .orElse(false);
    }

    // 获取支付类型映射
    public Map<String, Boolean> getPayTypeMap() {
        if (payMethods == null) {
            return new HashMap<>();
        }
        return payMethods.stream()
                .collect(Collectors.toMap(PayMethod::getId, PayMethod::isUseLocalQrCode));
    }
}
