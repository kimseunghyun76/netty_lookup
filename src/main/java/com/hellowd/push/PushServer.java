package com.hellowd.push;

import com.hellowd.monitor.ServerMonitor;
import com.hellowd.push.zookeeper.RouteProtocol;
import com.hellowd.push.zookeeper.Strategy;
import com.hellowd.push.zookeeper.StrategyFactory;
import com.hellowd.push.zookeeper.exception.RequestProcessingException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * Created by Helloworld
 * User : USER
 * Date : 2015-12-23
 * Time : 오후 4:45
 * To change this template use File | Settings | File and Code Templates.
 */

@Component
public class PushServer implements Watcher {

    static Logger logger = LoggerFactory.getLogger(PushServer.class);

    @Autowired
    @Qualifier("tcpSocketAddress")
    private InetSocketAddress address;

    @Autowired
    @Qualifier("bossThreadCount")
    private int bossThreadCount;

    @Autowired
    @Qualifier("workerThreadCount")
    private int workerThreadCount;

    @Autowired
    @Qualifier("httpPort")
    private int httpPort;


    @Autowired
    @Qualifier("tcpPort")
    private int tcpPort;

    @Autowired
    @Qualifier("zookeeperHostPort")
    private String zookeeperHostPort;

    @Autowired
    @Qualifier("zookeeperSessionTimeout")
    private int zookeeperSessionTimeout;

    private ZooKeeper zk;
    private ServerMonitor serverMonitor;

    public void start() {
        //이때 ServerMonitor가 시작을 하게 된답니다.
        serverMonitor = new ServerMonitor();

        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadCount);
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadCount);

        try{
            zk = new ZooKeeper(zookeeperHostPort,zookeeperSessionTimeout, this);


        }catch (IOException e) {
            logger.error(e.toString(),e);
        } catch (Exception e){
            logger.error("zookeeper 에러다 " + e.toString(),e);
        }

        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new PushServerInitializer());
            b.bind(httpPort).sync().channel().closeFuture().sync();

        }catch (InterruptedException e){
            logger.error(e.toString(),e);
        } finally{
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.info("셧다운 되었습니다.");
        }

    }

    public ServerMonitor getServerMonitor() {
        return serverMonitor;
    }

    /**
     * Zookeeper 에 대한..
     *
     *
     * @param route
     */
    public void createResponse(RouteProtocol route) throws RequestProcessingException {
        if( zk != null && zk.getState() == ZooKeeper.States.CONNECTED){
            try{
                List<String> nodeList = zk.getChildren("/"+route.getPath() , false);
                if(nodeList.size() > 0){
                    String childNode = null;

                    if(nodeList.size() == 1){
                        childNode = nodeList.get(0);
                    }else{
                        byte[] strategyType = zk.getData("/"+route.getPath() , false ,null);

                        Strategy strategy = StrategyFactory.createStrategy((strategyType != null) ? new String(strategyType) : null);
                        childNode = strategy.elect(route.getPath(),nodeList);
                    }

                    byte[] hostPort = zk.getData("/"+ route.getPath() + childNode , false, null);

                    if( hostPort != null){
                        route.setSuccess(true);
                        route.setResponse(new String(hostPort));
                    }else{
                        route.setSuccess(false);
                        route.setResponse("NODATA");
                    }
                }else{
                    route.setSuccess(false);
                    route.setResponse("NOCHILD");
                }
            }catch (KeeperException e){
                logger.error(e.toString(),e);

                KeeperException.Code code = e.code();

                route.setSuccess(false);
                route.setResponse(code.toString());
            }catch (Exception e){
                logger.error(e.toString(),e);

                route.setSuccess(false);
                route.setResponse(e.toString());

                throw new RequestProcessingException(e.toString());
            }
        }else{
            route.setSuccess(false);
            route.setResponse("CONNECTLOST");
        }
    }





    /**
     * Zookeeper에서는 특정 노드의 변화을 알수 있는데,
     * 폴링 방식을 취할 경우에, 지속적인 커넥션 비용 혹은 반응시간에 문제가 있을 수 있다.
     *
     * 주키퍼는 그리하여 메세지 이벤트 방식을 사용한다.(Watcher 를 구현해서 사용한다.)
     * event type 으로 상태를 구분한다.
     * process 함수는 clientcnx 클래스의 쓰레드에서 호출한다.
     * 커넥션 정보등을 노드에 저장하고, 해당 장비가 장애로 인해 커넥션이 끊어지면, 해당 노드가 빠짐으로
     * 해당 이벤트를 체크해 다른 장비 쪽으로 request 가 가능하다.
     *
     * @param watchedEvent
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        logger.info("Watcher Process가 시작되었습니다.");

        if(watchedEvent.getType() == Event.EventType.None){
            Event.KeeperState state = watchedEvent.getState();

            if(state == Event.KeeperState.SyncConnected){
                logger.info("주키퍼 세션이 (재)연결 되었습니다.");
            }else if(state == Event.KeeperState.Disconnected){
                logger.warn("연결이 끊겼네요... 연결이 될때까지 계속 연결 시도를 하겠습니다.");
            }else if(state == Event.KeeperState.Expired){
                logger.warn("주키퍼 세션이 만료되었습니다. 이 서버는 세션을 재생성 할것입니다.");

                try{
                    if(zk != null)
                        zk.close();
                    if(!StringUtils.isEmpty(zookeeperHostPort))
                        zk = new ZooKeeper(zookeeperHostPort,zookeeperSessionTimeout, this);
                }catch (IOException e){
                    logger.error(e.toString(), e);
                } catch (InterruptedException e) {
                    logger.error(e.toString(),e);
                }
            }
        }

    }
}
