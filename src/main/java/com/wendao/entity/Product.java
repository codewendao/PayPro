package com.wendao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author lld
 */
@Data
@TableName("t_product")
public class Product implements Serializable{

    /**
     * 唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String productName;

    private BigDecimal money;

    /**
     * 留言
     */
    private String description;

    private Date createTime;

    private Date updateTime;

    private String extend;

    /** 产品类型 GAME CODE*/
    private String type;

    @TableLogic(delval = "1",value = "0")
    private Integer del;

}
