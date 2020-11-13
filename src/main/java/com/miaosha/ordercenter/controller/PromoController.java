package com.miaosha.ordercenter.controller;

import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.error.EmBusinessError;
import com.miaosha.ordercenter.model.UserModel;
import com.miaosha.ordercenter.response.CommonReturnType;
import com.miaosha.ordercenter.service.PromoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public CommonReturnType getPromoToken(Integer promoId, Integer itemId, String token) throws BusinessException {

//        if (redisTemplate.opsForValue().get(token) == null)
//            // 用户未登录
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);

        Map userModel = (LinkedHashMap)redisTemplate.opsForValue().get(token);
        // todo 从用户中心取用户信息
        List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
        instances.stream().forEach( instance-> {
            log.info(instance.getHost());
        });
//        String promoToken = promoService.generatePromoToken(itemId, promoId, )
        return CommonReturnType.create(userModel);
    }


}
