package com.hellowd.push.zookeeper.impl;

import com.hellowd.push.zookeeper.Strategy;

import java.util.Collections;
import java.util.List;

/**
 * Created by Helloworld
 * User : USER
 * Date : 2015-12-24
 * Time : 오후 4:29
 * To change this template use File | Settings | File and Code Templates.
 */
public class StrategyStandbyImpl implements Strategy {
    @Override
    public String elect(String path, List<String> nodeList) {
        Collections.sort(nodeList);

        return nodeList.get(0);
    }
}
