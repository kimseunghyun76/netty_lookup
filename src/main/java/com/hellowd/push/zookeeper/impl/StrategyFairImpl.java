package com.hellowd.push.zookeeper.impl;

import com.hellowd.push.zookeeper.Strategy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Helloworld
 * User : USER
 * Date : 2015-12-24
 * Time : 오후 4:30
 * To change this template use File | Settings | File and Code Templates.
 */
public class StrategyFairImpl implements Strategy {

    private static Map<String, Map<String,Long>> routeMap = new ConcurrentHashMap<String, Map<String,Long>>();


    @Override
    public String elect(String path, List<String> nodeList) {

        Map<String,Long> counterMap = routeMap.get(path);

        if(counterMap ==  null){
            counterMap = new ConcurrentHashMap<String,Long>();

            for(String node : nodeList){
                counterMap.put(node , 0L);
            }

            routeMap.put(path,counterMap);
        }else{
            for(String node : nodeList){
                if(!counterMap.containsKey(node)){
                    counterMap.put(node,0L);
                }
            }
        }

        String elected = null;
        long minCount = Long.MAX_VALUE;

        for(String node : nodeList){
            long count = counterMap.get(node);

            if(count < minCount){
                elected = node;
                minCount = count;
            }
        }

        counterMap.put(elected , minCount+1);

        return elected;
    }

}
