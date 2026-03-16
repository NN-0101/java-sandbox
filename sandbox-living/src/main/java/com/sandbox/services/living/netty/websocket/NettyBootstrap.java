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
 * Netty服务器启动引导类
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>服务器启动</b>：配置并启动Netty服务器，监听指定端口</li>
 *   <li><b>线程模型管理</b>：创建和管理BossGroup、WorkerGroup线程组</li>
 *   <li><b>多服务支持</b>：同时启动设备服务器和用户服务器，监听不同端口</li>
 *   <li><b>生命周期管理</b>：配合Spring容器，在应用启动/停止时初始化和销毁资源</li>
 * </ul>
 *
 * <p>线程模型设计：</p>
 * <ul>
 *   <li><b>设备服务器</b>：
 *     <ul>
 *       <li>Boss Group: 1个线程，负责接受设备连接</li>
 *       <li>Worker Group: 默认线程数（CPU核心数*2），负责处理设备IO事件</li>
 *     </ul>
 *   </li>
 *   <li><b>用户服务器</b>：
 *     <ul>
 *       <li>Boss Group: 2个线程，负责接受用户连接（用户连接数较多）</li>
 *       <li>Worker Group: 8个线程，负责处理用户IO事件</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>配置项：</p>
 * <ul>
 *   <li>netty.websocket.port: WebSocket服务基础端口（未直接使用）</li>
 *   <li>netty.device.port: 设备服务器端口，默认9001</li>
 *   <li>netty.user.port: 用户服务器端口，默认9002</li>
 * </ul>
 *
 * @author xp
 * @create 2025/4/27
 */
@Slf4j
@Configuration
public class NettyBootstrap {

    @Value("${netty.device.port:9001}")
    private int devicePort;

    @Value("${netty.user.port:9002}")
    private int userPort;

    private EventLoopGroup deviceBossGroup;
    private EventLoopGroup deviceWorkerGroup;
    private EventLoopGroup userBossGroup;
    private EventLoopGroup userWorkerGroup;

    /**
     * 启动Netty服务器
     *
     * <p>在Spring容器初始化完成后自动调用，
     * 分别启动设备服务器和用户服务器。</p>
     *
     * @throws InterruptedException 端口绑定被中断时抛出
     */
    @PostConstruct
    public void start() throws InterruptedException {
        startDeviceServer();
        startUserServer();
        log.info("Netty服务器启动完成 - 设备端口: {}, 用户端口: {}", devicePort, userPort);
    }

    /**
     * 关闭Netty服务器
     *
     * <p>在Spring容器销毁前自动调用，
     * 优雅关闭所有线程组，释放系统资源。</p>
     */
    @PreDestroy
    public void shutdown() {
        if (deviceBossGroup != null) {
            deviceBossGroup.shutdownGracefully();
        }
        if (deviceWorkerGroup != null) {
            deviceWorkerGroup.shutdownGracefully();
        }

        if (userBossGroup != null) {
            userBossGroup.shutdownGracefully();
        }
        if (userWorkerGroup != null) {
            userWorkerGroup.shutdownGracefully();
        }
        log.info("Netty服务器已关闭");
    }

    /**
     * 启动设备WebSocket服务器
     *
     * <p>配置说明：</p>
     * <ul>
     *   <li>Boss线程数：1（设备连接相对较少）</li>
     *   <li>Worker线程数：默认（CPU核心数*2，适合IO密集型）</li>
     *   <li>SO_BACKLOG：128，等待队列长度</li>
     *   <li>SO_KEEPALIVE：true，启用TCP keep-alive</li>
     * </ul>
     *
     * @throws InterruptedException 端口绑定被中断时抛出
     */
    private void startDeviceServer() throws InterruptedException {
        deviceBossGroup = new NioEventLoopGroup(1);
        deviceWorkerGroup = new NioEventLoopGroup();

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
     * 启动用户WebSocket服务器
     *
     * <p>配置说明：</p>
     * <ul>
     *   <li>Boss线程数：2（用户连接较多，提高连接接受能力）</li>
     *   <li>Worker线程数：8（显式指定，处理用户IO事件）</li>
     *   <li>SO_BACKLOG：256，更大的等待队列</li>
     *   <li>SO_KEEPALIVE：true，启用TCP keep-alive</li>
     * </ul>
     *
     * @throws InterruptedException 端口绑定被中断时抛出
     */
    private void startUserServer() throws InterruptedException {
        userBossGroup = new NioEventLoopGroup(2);
        userWorkerGroup = new NioEventLoopGroup(8);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(userBossGroup, userWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new UserWebSocketInitializer()) // 假设存在用户服务器初始化器
                .option(ChannelOption.SO_BACKLOG, 256)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(userPort).sync();
        if (future.isSuccess()) {
            log.info("用户WebSocket服务器启动成功，端口: {}", userPort);
        }
    }
}