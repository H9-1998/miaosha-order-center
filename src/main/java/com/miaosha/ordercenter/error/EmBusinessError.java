package com.miaosha.ordercenter.error;

public enum EmBusinessError implements CommonError {
    //通用类型错误 10001
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),
    UNKNOW_ERROR(10002, "未知错误"),

    // 20000 开头为用户信息相关错误定义
    USER_NOT_EXIST(20001, "用户不存在"),
    LOGIN_ERROR(20002, "用户名或密码错误"),
    USER_NOT_LOGIN(20003, "用户未登陆"),
    OTP_CODE_ERROR(20004, "验证码错误"),
    HAS_BEEN_REGISTER(20005, "该手机号已被注册"),

    //30000 开头为商品信息相关错误定义
    ITEM_NOT_EXIST(30001, "商品不存在"),
    PROMO_NOT_EXIST(30002, "活动不存在"),

    //40000 开头为订单信息相关错误定义
    STOCK_NOT_ENOUGH(40001, "商品库存不足"),
    MQ_SEND_FAIL(40002, "异步消息失败"),
    RATELIMIT(40003, "活动太火爆，请稍后重试"),
    ;

    private EmBusinessError(int errCode, String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
