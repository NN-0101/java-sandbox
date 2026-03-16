package com.sandbox.services.living.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: xp
 * @create: 2025/4/27
 */
@Slf4j
@Configuration
public class NettyBootstrap {

    @Value("${netty.websocket.port}")
    private int port;

    @Value("${netty.device.port:9001}")
    private int devicePort;

    @Value("${netty.user.port:9002}")
    private int userPort;

    private EventLoopGroup deviceBossGroup;
    private EventLoopGroup deviceWorkerGroup;
    private EventLoopGroup userBossGroup;
    private EventLoopGroup userWorkerGroup;

    @PostConstruct
    public void start() throws InterruptedException {
        startDeviceServer();
        startUserServer();
        log.info("Netty服务器启动完成 - 设备端口: {}, 用户端口: {}", devicePort, userPort);
    }

    @PreDestroy
    public void shutdown() {
        if (deviceBossGroup != null) {
            deviceBossGroup.shutdownGracefully();
        }
        if (deviceWorkerGroup != null) {
            deviceWorkerGroup.shutdownGracefully();
        }

        // 优雅关闭用户服务器
        if (userBossGroup != null) {
            userBossGroup.shutdownGracefully();
        }
        if (userWorkerGroup != null) {
            userWorkerGroup.shutdownGracefully();
        }
    }

    /**
     * 启动设备服务器
     */
    private void startDeviceServer() throws InterruptedException {
        deviceBossGroup = new NioEventLoopGroup(1);      // 设备连接线程数较少
        //workerGroup的线程设置建议：
        //CPU密集型：N = CPU核心数
        //I/O密集型：N = CPU核心数 * 2
        //混合型：N = CPU核心数 * 1.5
        deviceWorkerGroup = new NioEventLoopGroup();    // 设备工作线程数

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(deviceBossGroup, deviceWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new DeviceWebSocketInitializer())
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(devicePort).sync();
        if (future.isSuccess()) {
            log.info("设备WebSocket服务器启动成功，端口: {}", devicePort);
        }
    }

    /**
     * 启动用户服务器
     */
    private void startUserServer() throws InterruptedException {
        userBossGroup = new NioEventLoopGroup(2);        // 用户连接线程数较多
        userWorkerGroup = new NioEventLoopGroup(8);      // 用户工作线程数较多

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(userBossGroup, userWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new UserWebSocketInitializer())
                .option(ChannelOption.SO_BACKLOG, 256)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(userPort).sync();
        if (future.isSuccess()) {
            log.info("用户WebSocket服务器启动成功，端口: {}", userPort);
        }
    }
}
