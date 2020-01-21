package com.seckill.error;

public enum EnumError implements CommonError{
    PARAMETER_INVALIDATION_ERROR(00001, "PARAMETER INVALIDATION"),
    USER_NOT_EXIST(10001, "USER NOT EXIST");

    private EnumError(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    private int errorCode;
    private String errorMsg;

    @Override
    public int getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErrorMsg() {
        return this.errorMsg;
    }

    @Override
    public CommonError setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;

        return this;
    }
}
