package com.wendao.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
public class GenLicenseUtils {

    private static String SALT = "MySecretPaySync@20261/*-555";
    /**
     * 生成授权码逻辑
     */
    public static String generateLicense(String machineCode) {
        try {
            // 1. 拼接字符串（注意：必须先处理掉机器码两端的空格）
            String rawStr = machineCode.trim() + SALT;

            // 2. MD5 计算
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(rawStr.getBytes(StandardCharsets.UTF_8));

            // 3. 稳妥的十六进制转换逻辑
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                // 确保每个字节都转为 2 位十六进制，不足补 0
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }

            // 4. 返回大写结果
            return sb.toString().toUpperCase();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
