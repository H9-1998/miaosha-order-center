package com.miaosha.ordercenter.controller;

import com.miaosha.ordercenter.service.OrderService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auhor: dhz
 * @date: 2020/11/13 19:18
 */
@RestController
@RequestMapping("/order")
@Api(tags = {"订单处理相关api"})
public class OrderController {

    @Autowired
    private OrderService orderService;


}
