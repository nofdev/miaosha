package com.shangpin.miaosha;

/**
 * Created by Qiang on 3/4/14.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
