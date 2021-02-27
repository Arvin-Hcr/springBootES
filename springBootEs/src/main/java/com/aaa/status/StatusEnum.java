package com.aaa.status;


public enum  StatusEnum {
    /**
     * 枚举定义四个值
     *  1.存在
     *  2.不存在
     *  3.操作成功
     *  4.操作失败
     */

    EXIST("101","数据存在"),
    NOT_EXIST("401","数据不存在"),
    OPRATION_FAILED("200","操作成功"),
    OPRATION_SUCCESS("402","操作失败");

    @Override
    public String toString() {
        return "StatusEnum{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }

    StatusEnum(String code, String msg){
        this.code=code;
        this.msg=msg;
    }
    private String code;
    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
