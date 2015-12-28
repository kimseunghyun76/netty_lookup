package com.hellowd.push.zookeeper;

import java.util.List;

/**
 * Created by Helloworld
 * User : USER
 * Date : 2015-12-24
 * Time : 오후 4:25
 * To change this template use File | Settings | File and Code Templates.
 */
public interface Strategy {
    public String elect(String path, List<String> nodeList);
}
