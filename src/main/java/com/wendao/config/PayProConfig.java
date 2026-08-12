package com.wendao.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.util.Collections;
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
     * 二维码外部存储目录（绝对路径，如 /app/appsystems/qr）。
     * 留空则使用 classpath 下的 static/assets/qr/，兼容现有打包方式。
     */
    private String qrDir;

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

    /**
     * 产品列表（从 YAML 配置文件读取）
     */
    private List<ProductConfig> products;

    /**
     * 减额匹配配置
     */
    private Decrement decrement = new Decrement();

    @Data
    public static class Decrement {
        /**
         * 是否启用减额匹配模式
         */
        private boolean enabled = false;

        /**
         * 最大减额槽位数(同一基础价格最多支持多少个并发减额订单)
         */
        private int maxCount = 5;

        /**
         * 每次减额的步长(元)
         */
        private BigDecimal step = new BigDecimal("0.01");
    }

    /**
     * 产品配置内部类（对应 YAML 中 paypro.products 列表的每一项）
     */
    @Data
    public static class ProductConfig {
        private Integer id;
        private String productName;
        private BigDecimal money;
        private String description;
        private String extend;
        private String type;
        private String downloadUrl;
    }

    /**
     * 根据 ID 从配置中查找产品
     * @return 产品配置，未找到返回 null
     */
    public ProductConfig getProductConfigById(Integer id) {
        if (products == null) return null;
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据类型列表从配置中筛选产品
     * @return 匹配的产品列表，无匹配返回空列表
     */
    public List<ProductConfig> getProductConfigsByTypes(List<String> types) {
        if (products == null || types == null) return Collections.emptyList();
        return products.stream()
                .filter(p -> types.contains(p.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        if (org.springframework.util.StringUtils.hasText(qrDir)) {
            String location = qrDir.endsWith("/") ? "file:" + qrDir : "file:" + qrDir + "/";
            registry.addResourceHandler("/assets/qr/**")
                    .addResourceLocations(location, "classpath:/static/assets/qr/");
        }
    }
}
