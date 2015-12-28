package com.hellowd.push.zookeeper.exception;

/**
 * Created by Helloworld
 * User : USER
 * Date : 2015-12-24
 * Time : 오후 4:48
 * To change this template use File | Settings | File and Code Templates.
 */
public class RequestProcessingException extends Exception{

    private static final long serialVersionUID = -4165315093501553912L;

    private String errmsg;

    public RequestProcessingException(String errmsg) {
        this.errmsg = errmsg;
    }

}
