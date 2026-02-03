package com.wendao.model.req;

import lombok.Data;

import java.util.List;

/**
 * @description:
 **/
@Data
public class GetOrderListReq {
    /**
     * 当前页码
     */
    private Integer pageIndex = 1;

    /**
     * 每页显示条数
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方式 ASC/DESC
     */
    private String order = "DESC";

    private String keyword;

    private List<Integer> states;
}