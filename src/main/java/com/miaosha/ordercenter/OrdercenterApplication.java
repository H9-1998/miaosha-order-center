package com.miaosha.ordercenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.miaosha.ordercenter.dao")
public class OrdercenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdercenterApplication.class, args);
    }

}
