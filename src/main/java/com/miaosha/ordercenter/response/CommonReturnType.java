package com.miaosha.ordercenter.response;

public class CommonReturnType {
    //若status=success data内存放返回的数据
    //若status=fail data存放错误码
    private String status;
    private Object data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static CommonReturnType create(Object data){
        CommonReturnType type = CommonReturnType.create(data, "success");
        return type;
    }

    public static CommonReturnType create(Object data, String status){
        CommonReturnType type = new CommonReturnType();
        type.setData(data);
        type.setStatus(status);
        return type;
    }
}
