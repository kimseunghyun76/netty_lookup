package com.hellowd.push.service;

import com.hellowd.monitor.ServerMonitor;
import com.hellowd.push.PushServer;
import com.hellowd.push.api.RestApiRequestTemplate;
import com.hellowd.push.api.exception.RequestParamException;
import com.hellowd.push.api.exception.ServiceException;
import com.hellowd.push.zookeeper.RouteProtocol;
import com.hellowd.push.zookeeper.exception.RequestProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by Helloworld
 * User : USER
 * Date : 2015-12-23
 * Time : 오후 6:52
 * To change this template use File | Settings | File and Code Templates.
 */
@Service("Lookup")
@Scope("prototype")
public class Lookup extends RestApiRequestTemplate {

    @Autowired
    private PushServer server;


    //reqData

    public Lookup(Map<String,String> reqData){
        super(reqData);
    }

    @Override
    public void requestParamValidation() throws RequestParamException {



    }

    @Override
    public void service() throws ServiceException {
        String uri = this.reqData.get("REQUEST_URI");
        RouteProtocol route = new RouteProtocol(uri.substring(uri.lastIndexOf('/') +1));

        ServerMonitor serverMonitor = server.getServerMonitor();
        serverMonitor.incrementAndGetRequests();
        serverMonitor.addAndGetRecvedBytes(this.reqData.get("REQUEST_ALL").getBytes().length);

        long start = System.currentTimeMillis();
        try {
            server.createResponse(route);
            if( !route.isSuccess() )
                serverMonitor.incrementAndGetErrors();
        } catch (RequestProcessingException e) {
            serverMonitor.incrementAndGetErrors();
        } finally {
            long elapsed = System.currentTimeMillis()-start;

            serverMonitor.setCurrentProcessTime(elapsed);
            serverMonitor.addAndGetTotalProcessTime(elapsed);
        }

        //그 다음 노드 별로 분기를 타서 해당 상태 점검 한 뒤에 결과 값을 뿌려줌 아래와 같이 말이지..
        this.apiResult.addProperty("status", route.isSuccess());
        this.apiResult.addProperty("data",route.getResponse());
    }
}
