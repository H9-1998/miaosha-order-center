package com.miaosha.ordercenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

@EnableFeignClients
@SpringBootApplication
@MapperScan("com.miaosha.ordercenter.dao")
public class OrdercenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdercenterApplication.class, args);
    }

}
