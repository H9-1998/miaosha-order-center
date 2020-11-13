package com.miaosha.ordercenter.controller;

import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.error.EmBusinessError;
import com.miaosha.ordercenter.response.CommonReturnType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @auhor: dhz
 * @date: 2020/11/13 18:37
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public CommonReturnType doError(HttpServletRequest request, HttpServletResponse response, Exception e){
        log.error(e.getMessage(), e);
        Map<String, Object> responseData = new HashMap<>();
        if (e instanceof BusinessException){
            responseData.put("errCode", ((BusinessException) e).getErrCode());
            responseData.put("errMsg", ((BusinessException) e).getErrMsg());
        } else if (e instanceof ServletRequestBindingException){
            responseData.put("errCode", EmBusinessError.UNKNOW_ERROR.getErrCode());
            responseData.put("errMsg", "url绑定路由问题");
        }else if (e instanceof NoHandlerFoundException){
            responseData.put("errCode", EmBusinessError.UNKNOW_ERROR.getErrCode());
            responseData.put("errMsg", "没有找到对应的访问路径");
        }else {
            responseData.put("errCode", EmBusinessError.UNKNOW_ERROR.getErrCode());
            responseData.put("errMsg", EmBusinessError.UNKNOW_ERROR.getErrMsg());
        }
        return CommonReturnType.create(responseData, "fail");
    }
}
