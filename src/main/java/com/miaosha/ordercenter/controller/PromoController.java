package com.miaosha.ordercenter.controller;

import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.response.CommonReturnType;
import com.miaosha.ordercenter.service.PromoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auhor: dhz
 * @date: 2020/11/13 19:46
 */
@RestController
@Api(tags = {"活动相关api"})
public class PromoController {

    @Autowired
    private PromoService promoService;

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


}
