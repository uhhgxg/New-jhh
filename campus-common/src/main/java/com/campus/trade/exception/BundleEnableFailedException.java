package com.campus.trade.exception;

/**
 * 捆绑包启用失败异常
 */
public class BundleEnableFailedException extends BaseException {

    public BundleEnableFailedException(){}

    public BundleEnableFailedException(String msg){
        super(msg);
    }
}
