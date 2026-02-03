package com.wendao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author lld
 */
@SpringBootApplication
//启用缓存
@EnableCaching
//启用异步
@EnableAsync
//定时任务
@EnableScheduling
@MapperScan(basePackages = {"com.wendao.mapper"})
public class PayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }
}
