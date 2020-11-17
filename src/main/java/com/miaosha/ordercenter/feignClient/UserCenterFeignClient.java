package com.miaosha.ordercenter.feignClient;

import com.miaosha.ordercenter.entity.UserInfo;
import com.miaosha.ordercenter.response.CommonReturnType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @auhor: dhz
 * @date: 2020/11/17 14:06
 * 调用user-center的feignClient
 */

@FeignClient(name = "user-center")
public interface UserCenterFeignClient {

    /**
     * 获取用户信息
     * @param token
     * @return
     */
    @GetMapping("/user/get-user-info")
    CommonReturnType getUserInfo(@RequestHeader("x-token") String token);
}
