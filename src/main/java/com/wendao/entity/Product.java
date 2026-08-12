package com.wendao.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 产品实体（从 YAML 配置文件读取，非数据库表）
 */
@Data
public class Product implements Serializable {

    /**
     * 唯一标识
     */
    private Integer id;

    private String productName;

    private BigDecimal money;

    /**
     * 留言
     */
    private String description;

    private String extend;

    /** 产品类型 GAME CODE */
    private String type;

    /** 支付成功后邮件发送的下载链接（仅后端使用，公开接口不会返回） */
    private String downloadUrl;

}
