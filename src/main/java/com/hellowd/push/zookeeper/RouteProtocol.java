package com.hellowd.push.zookeeper;

/**
 * Created by Helloworld
 * User : USER
 * Date : 2015-12-24
 * Time : 오후 4:04
 * To change this template use File | Settings | File and Code Templates.
 */
public class RouteProtocol {

    private String path;
    private String response;
    private boolean success;

    public RouteProtocol(String path){
        this.path = path.trim();
    }

    public String getResult() {
        return success+"|"+response;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
