package com.wendao.service.impl;

import com.wendao.entity.Product;
import com.wendao.model.ResponseVO;
import com.wendao.mapper.ProductMapper;
import com.wendao.model.req.GetProductListReq;
import com.wendao.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 **/
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductMapper productMapper;

    @Override
    public ResponseVO<Product> get(Integer productId) {
        Product product = productMapper.selectById(productId);
        if(product != null){
            return ResponseVO.successResponse(product);
        }
        return ResponseVO.errorResponse("获取产品失败");
    }

    @Override
    public ResponseVO<List<Product>> getListByType(GetProductListReq req) {
        QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
        productQueryWrapper.lambda().in(Product::getType,req.getTypes());
        return ResponseVO.successResponse(productMapper.selectList(productQueryWrapper));
    }
}
