package com.wendao.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.wendao.model.req.OpenApiOrderReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class OpenApiSignUtil {

    private static final Logger log = LoggerFactory.getLogger(OpenApiSignUtil.class);

    @Value("${paypro.openapi.secret:default_secret_key}")
    private String secretKey;

    public String generateSign(Map<String, Object> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        log.info("开始生成签名，参数数量: {}", keys.size());

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            Object value = params.get(key);
            if (value != null && !"".equals(value) && !"sign".equals(key)) {
                // 对BigDecimal进行特殊处理，保持与前端一致的格式
                String valueStr;
                if (value instanceof BigDecimal) {
                    valueStr = ((BigDecimal) value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
                } else {
                    valueStr = value.toString();
                }
                // 检查 valueStr 是否包含空格
                log.info("处理参数: key={}, value类型={}, value值=[{}], 包含空格={}",
                        key, value.getClass().getSimpleName(), valueStr, valueStr.contains(" "));
                String paramPart = key + "=" + valueStr + "&";
                log.info("添加参数: key={}, value={}, paramPart={}", key, value, paramPart);
                sb.append(paramPart);
            }
        }
        sb.append("key=").append(secretKey);

        String signStr = sb.toString();
        log.info("服务端签名字符串: {}", signStr);
        log.info("服务端使用的密钥: {}", secretKey);

        return SecureUtil.md5(signStr).toUpperCase();
    }

    public boolean verifySign(OpenApiOrderReq req) {
        Map<String, Object> params = new TreeMap<>();
        params.put("orderNo", req.getOrderNo());
        params.put("amount", req.getAmount());
        params.put("payType", req.getPayType());
        params.put("nickName", req.getNickName());
        params.put("description", req.getDescription());
        params.put("email", req.getEmail());
        params.put("notifyUrl", req.getNotifyUrl());
        params.put("userId", req.getUserId());
        params.put("productId", req.getProductId());
        params.put("timestamp", req.getTimestamp());

        log.info("服务端接收到的参数: orderNo={}, amount={}, payType={}, nickName={}, description={}, email={}, notifyUrl={}, userId={}, productId={}, timestamp={}",
                req.getOrderNo(), req.getAmount(), req.getPayType(), req.getNickName(),
                req.getDescription(), req.getEmail(), req.getNotifyUrl(), req.getUserId(),
                req.getProductId(), req.getTimestamp());
        log.info("服务端接收到的签名: {}", req.getSign());

        // 检查 notifyUrl 是否包含空格
        if (req.getNotifyUrl() != null) {
            String notifyUrl = req.getNotifyUrl();
            log.info("notifyUrl 原始值: [{}]", notifyUrl);
            log.info("notifyUrl 长度: {}, 包含空格: {}", notifyUrl.length(), notifyUrl.contains(" "));
            log.info("notifyUrl 字节数组: {}", notifyUrl.getBytes());
        }

        String calculatedSign = generateSign(params);
        boolean isValid = calculatedSign.equals(req.getSign());

        if (!isValid) {
            log.warn("签名验证失败 - 期望: {}, 实际: {}", calculatedSign, req.getSign());
        }

        return isValid;
    }

    public boolean verifyTimestamp(Long timestamp) {
        if (timestamp == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long diff = Math.abs(currentTime - timestamp);
        long maxDiff = 5 * 60 * 1000;
        
        if (diff > maxDiff) {
            log.warn("时间戳验证失败 - 当前: {}, 请求: {}, 差值: {}ms", currentTime, timestamp, diff);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {

        System.out.println(DigestUtil.md5Hex("amount=10.00&description=1&email=test@example.com&nickName=1"));
    }
}
