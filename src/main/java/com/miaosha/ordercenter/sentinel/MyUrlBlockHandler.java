package com.miaosha.ordercenter.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miaosha.ordercenter.error.BusinessException;
import com.miaosha.ordercenter.error.EmBusinessError;
import com.miaosha.ordercenter.response.CommonReturnType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @auhor: dhz
 * @date: 2020/11/22 15:14
 */
@Component
public class MyUrlBlockHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse response, BlockException e) throws Exception {
        BusinessException businessException = null;
//        EmBusinessError emBusinessError = null;
        if (e instanceof FlowException){
            businessException = new BusinessException(EmBusinessError.FLOW_EXCEPTION);
//            emBusinessError = EmBusinessError.FLOW_EXCEPTION;
        }
        else if (e instanceof DegradeException){
            businessException = new BusinessException(EmBusinessError.DEGRADE_EXCEPTION);
//            emBusinessError = EmBusinessError.DEGRADE_EXCEPTION;
        }
        else if (e instanceof SystemBlockException){
            businessException = new BusinessException(EmBusinessError.SYSTEM_BLOCK_EXCEPTION);
//            emBusinessError = EmBusinessError.SYSTEM_BLOCK_EXCEPTION;
        }
        else if (e instanceof ParamFlowException){
            businessException = new BusinessException(EmBusinessError.PARAM_FLOW_EXCEPTION);
//            emBusinessError = EmBusinessError.PARAM_FLOW_EXCEPTION;
        }
        else if (e instanceof AuthorityException){
            businessException = new BusinessException(EmBusinessError.AUTHORITY_EXCEPTION);
//            emBusinessError = EmBusinessError.AUTHORITY_EXCEPTION;
        }
        response.setStatus(200);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        response.setContentType("application/json;charset=utf-8");

        new ObjectMapper().writeValue(
                response.getWriter(),
                CommonReturnType.create(businessException)
        );
    }
}
