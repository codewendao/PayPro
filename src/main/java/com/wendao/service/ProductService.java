package com.wendao.service;


import com.wendao.entity.Product;
import com.wendao.model.ResponseVO;
import com.wendao.model.req.GetProductListReq;

import java.util.List;

/**
 * @description:
 **/
public interface ProductService {
    ResponseVO<Product> get(Integer productId);

    ResponseVO<List<Product>> getListByType(GetProductListReq req);

    /**
     * 内部使用：获取完整 Product（含 downloadUrl），不包装 ResponseVO。
     * 供 OrderServiceImpl 等后端消费者使用。
     * @return Product or null if not found
     */
    Product getProductById(Integer productId);
}
