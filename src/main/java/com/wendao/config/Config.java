package com.wendao.config;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.annotation.Resource;

@Component
public class Config implements WebMvcConfigurer {

    @Resource
    PayProConfig payConfig;

    /**
     * @description 一些全局变量，放在这里
     * @return void
    */
    @Resource
    private void configureThymeleafStaticVars(ThymeleafViewResolver viewResolver) {
        if(viewResolver != null) {
            viewResolver.addStaticVariable("title",payConfig.getTitle());
            viewResolver.addStaticVariable("indexTitle",payConfig.getIndexTitle());
            viewResolver.addStaticVariable("site",payConfig.getSite());
            viewResolver.addStaticVariable("alipayUserId",payConfig.getAlipayUserId());
            viewResolver.addStaticVariable("alipayCustomQrUrl",payConfig.getAlipayCustomQrUrl());
            viewResolver.addStaticVariable("mobile",payConfig.getMobile());
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public Snowflake snowflake(){
        return new Snowflake(0,0);
    }

}
