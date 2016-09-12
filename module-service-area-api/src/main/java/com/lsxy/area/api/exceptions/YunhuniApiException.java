package com.lsxy.area.api.exceptions;

import com.lsxy.area.api.ApiReturnCodeEnum;

/**
 * Created by liups on 2016/8/26.
 */
public abstract class YunhuniApiException extends Exception{
    public YunhuniApiException(Throwable t) {
        super(t);
    }

    public YunhuniApiException() {
        super();
    }

    public abstract ApiReturnCodeEnum getApiExceptionEnum();

    public final String getCode(){
        return getApiExceptionEnum().getCode();
    }

    public final String getMessage(){
        return getApiExceptionEnum().getMsg();
    }
}
