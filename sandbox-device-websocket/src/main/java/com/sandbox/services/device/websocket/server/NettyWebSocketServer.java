package com.sandbox.services.device.websocket.server;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;

/**
 * @description:
 * @author: xp
 * @create: 2025/4/27
 */
@Slf4j
@Configuration
public class NettyWebSocketServer {

    @Value("${netty.websocket.port}")
    private int port;

    @Value("${netty.websocket.name}")
    private String serverName;

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    @PostConstruct
    public void start() throws InterruptedException, NacosException {
        bossGroup = new NioEventLoopGroup(1);
        //workerGroup的线程设置建议：
        //CPU密集型：N = CPU核心数
        //I/O密集型：N = CPU核心数 * 2
        //混合型：N = CPU核心数 * 1.5
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WebSocketChannelInitializer(getCurrentServerId()));

        bootstrap.bind(port).sync().channel();
        log.info("Netty WebSocket Server started on port: {}", port);
    }

    @PreDestroy
    public void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

//    /**
//     * Netty服务注册到nacos
//     *
//     * @throws NacosException 异常
//     */
//    private void registerToNacos() throws NacosException {
//
//        // 1. 获取 Nacos 命名服务实例
//        Properties properties = new Properties();
//        properties.setProperty("serverAddr", nacosDiscoveryProperties.getServerAddr());
//        properties.setProperty("namespace", nacosDiscoveryProperties.getNamespace());
//        properties.setProperty("username", nacosDiscoveryProperties.getUsername());
//        properties.setProperty("password", nacosDiscoveryProperties.getPassword());
//
//        NamingService namingService = NacosFactory.createNamingService(properties);
//
//        Instance instance = new Instance();
//        // 显式设置实例 ID
//        instance.setInstanceId(getCurrentServerId());
//        instance.setIp(nacosDiscoveryProperties.getIp());
//        instance.setPort(port);
//        instance.setServiceName(serverName);
//        instance.setClusterName(nacosDiscoveryProperties.getClusterName());
//        instance.setMetadata(nacosDiscoveryProperties.getMetadata());
//
//        // 3. 注册实例到 Nacos
//        namingService.registerInstance(serverName, nacosDiscoveryProperties.getGroup(), instance);
//    }
//
//    /**
//     * 获取当前服务器的实例 ID（从 Nacos 元数据）
//     *
//     * @return 实例 ID
//     */
//    public String getCurrentServerId() {
//        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
//        // 对应配置文件中的 metadata.instance-id
//        return metadata.get("instance-id");
//    }
}
