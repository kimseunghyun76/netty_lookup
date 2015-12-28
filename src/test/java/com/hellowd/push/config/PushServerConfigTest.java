package com.hellowd.push.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.*;

/**
 * Created by Helloworld
 * User : USER
 * Date : 2015-12-28
 * Time : 오전 11:34
 * To change this template use File | Settings | File and Code Templates.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PushServerConfig.class, loader = AnnotationConfigContextLoader.class)
public class PushServerConfigTest {

    @Value("${boss.thread.count}")
    private int bossThreadCount;

    @Value("${worker.thread.count}")
    private int workerThreadCount;

    @Value("${tcp.port}")
    private int tcpPort;

    @Value("${http.port}")
    private int httpPort;

    @Value("${zookeeper.hostPort}")
    private String zookeeperHostPort;

    @Value("${zookeeper.sessionTimeout}")
    private int zookeeperSessionTimeout;



    @Test
    public void 최소bossThreadCount체크() throws Exception {
        assertNotNull(this.bossThreadCount);
        assertTrue(this.bossThreadCount > 0);
    }

    @Test
    public void 최소workerThreadCount체크() throws Exception {
        assertNotNull(this.workerThreadCount);
        assertTrue(this.workerThreadCount > 0);
    }

    @Test
    public void tcp포트체크() throws Exception {
        assertNotNull(this.tcpPort);
        assertTrue(this.tcpPort  != this.httpPort );
        assertTrue(this.tcpPort > 0 && this.tcpPort < 65536 );
    }

    @Test
    public void http포트체크() throws Exception {
        assertNotNull(this.httpPort);
        assertTrue(this.httpPort > 0 && this.tcpPort < 65536 );
    }

    @Test
    public void 주키퍼포트체크() throws Exception {
        assertNotNull(this.zookeeperHostPort);
        assertTrue(this.zookeeperHostPort.split(",").length > 0);
    }

    @Test
    public void 주키퍼세션타임아웃체크() throws Exception {
        assertNotNull(this.zookeeperSessionTimeout);
        assertTrue(this.zookeeperSessionTimeout > 0 );
    }

}