package com.hellowd.push.zookeeper;

import com.hellowd.push.zookeeper.impl.StrategyFairImpl;
import com.hellowd.push.zookeeper.impl.StrategyStandbyImpl;
import org.springframework.util.StringUtils;

/**
 * Created by Helloworld
 * User : USER
 * Date : 2015-12-24
 * Time : 오후 4:25
 * To change this template use File | Settings | File and Code Templates.
 */
public class StrategyFactory {

    public static final String STRATEGY_STANDBY = "standby";
    public static final String STRATEGY_FAIR 	= "fair";
//    public static final String STRATEGY_LATENCY = "latency";
//    public static final String STRATEGY_TRAFFIC	= "traffic";

    public static Strategy createStrategy(String strategyType){

        if(StringUtils.isEmpty(strategyType))
            strategyType = STRATEGY_STANDBY;

        if(STRATEGY_STANDBY.equals(strategyType))
            return new StrategyStandbyImpl();
        if(STRATEGY_FAIR.equals(strategyType))
            return new StrategyFairImpl();

        throw new IllegalArgumentException("미구현된 Strategy 입니다. : " + strategyType);
    }

}
