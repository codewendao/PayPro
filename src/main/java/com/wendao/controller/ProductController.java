package com.wendao.controller;

import com.wendao.entity.Product;
import com.wendao.model.ResponseVO;
import com.wendao.model.req.GetProductListReq;
import com.wendao.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 **/
@Controller()
@RequestMapping("product")
@Api(tags = "产品接口",description = "产品接口")
@CacheConfig(cacheNames = "pay")
public class ProductController {

    @Autowired
    ProductService productService;

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    @ApiOperation(value = "根据id获取产品")
    @ResponseBody
    public ResponseVO<Product> get(Integer productId){
        return productService.get(productId);
    }

    @RequestMapping(value = "/getListByType",method = RequestMethod.POST)
    @ApiOperation(value = "根据类型获取产品")
    @ResponseBody
    public ResponseVO<List<Product>> getListByType(@RequestBody GetProductListReq req){
        return productService.getListByType(req);
    }
}
