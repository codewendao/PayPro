package com.wendao.service.impl;

import com.wendao.config.PayProConfig;
import com.wendao.entity.Product;
import com.wendao.model.ResponseVO;
import com.wendao.model.req.GetProductListReq;
import com.wendao.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Product 服务实现 —— 从 YAML 配置文件读取产品数据
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    PayProConfig payProConfig;

    @Override
    public Product getProductById(Integer productId) {
        PayProConfig.ProductConfig config = payProConfig.getProductConfigById(productId);
        return config != null ? toProductEntity(config) : null;
    }

    @Override
    public ResponseVO<Product> get(Integer productId) {
        Product product = getProductById(productId);
        if (product != null) {
            // 脱敏：公开接口不返回下载链接
            product.setDownloadUrl(null);
            return ResponseVO.successResponse(product);
        }
        return ResponseVO.errorResponse("获取产品失败");
    }

    @Override
    public ResponseVO<List<Product>> getListByType(GetProductListReq req) {
        List<PayProConfig.ProductConfig> configs = payProConfig.getProductConfigsByTypes(req.getTypes());
        List<Product> products = configs.stream()
                .map(this::toProductEntity)
                .collect(Collectors.toList());
        // 脱敏：公开接口不返回下载链接
        products.forEach(p -> p.setDownloadUrl(null));
        return ResponseVO.successResponse(products);
    }

    /**
     * 将 YAML 配置对象转换为 Product 实体
     */
    private Product toProductEntity(PayProConfig.ProductConfig config) {
        Product product = new Product();
        product.setId(config.getId());
        product.setProductName(config.getProductName());
        product.setMoney(config.getMoney());
        product.setDescription(config.getDescription());
        product.setExtend(config.getExtend());
        product.setType(config.getType());
        product.setDownloadUrl(config.getDownloadUrl());
        return product;
    }
}
