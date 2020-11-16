package com.miaosha.ordercenter.controller;

import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.response.CommonReturnType;
import com.miaosha.ordercenter.service.PromoService;
import com.miaosha.ordercenter.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Id;
import java.util.List;

/**
 * @auhor: dhz
 * @date: 2020/11/13 19:46
 */
@RestController
@Slf4j
@Api(tags = {"活动相关api"})
public class PromoController {

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 发布活动
     * @param promoId 活动id
     * @return
     * @throws BusinessException
     */
    @ApiOperation("发布活动")
    @GetMapping("/publish-promo")
    public CommonReturnType publishPromo(@RequestParam("promoId") Integer promoId) throws BusinessException {
        promoService.publishPromo(promoId);
        return CommonReturnType.create("活动发布成功");
    }

    @ApiOperation("获取秒杀令牌")
    @GetMapping("get-promo-token")
    public CommonReturnType getPromoToken(@RequestHeader("x-token") String token,
                                          @RequestParam("promoId") Integer promoId,
                                          @RequestParam("itemId") Integer itemId
                                          ) throws BusinessException {

        // 解析jwt获取用户信息
        Claims user = jwtUtil.getClaimsFromToken(token);
        Integer userId = Integer.parseInt(user.get("id").toString());
        // 生成秒杀令牌
        String promoToken = promoService.generatePromoToken(itemId, promoId, userId);
        return CommonReturnType.create(promoToken);
    }


}
