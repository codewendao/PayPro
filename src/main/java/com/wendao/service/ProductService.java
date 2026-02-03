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
}
