package com.wendao.dto;

import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author:
 * @description:
 * @create: 2026-01-28 16:05
 **/
@Data
public class MsgContentsDTO {
    private String desc;
    private String title;
    private String url;

    /**
     * 辅助方法：从 desc 中提取“付款方备注”
     * @return 如果存在备注则返回备注内容，否则返回 null 或空字符串
     */
    public String extractRemark() {
        if (desc == null) return null;
        // 使用之前定义的正则：匹配“付款方备注”之后到换行符之前的内容
        Pattern pattern = Pattern.compile("付款方备注(.+)");
        Matcher matcher = pattern.matcher(desc);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
}
