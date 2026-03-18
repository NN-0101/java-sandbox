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
 * Netty 服务器启动引导类
 *
 * <p>该类是 Netty 服务器的启动入口，负责配置和启动多个 WebSocket 服务器实例，
 * 并与 Spring Boot 应用的生命周期进行集成。通过使用 {@link PostConstruct} 和
 * {@link PreDestroy} 注解，确保服务器在应用启动时自动启动，在应用关闭时优雅停止。
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>服务器启动：</b>配置并启动 Netty 服务器，监听指定端口，等待客户端连接</li>
 *   <li><b>线程模型管理：</b>创建和管理 BossGroup、WorkerGroup 线程组，负责连接接受和 IO 事件处理</li>
 *   <li><b>多服务支持：</b>同时启动设备服务器和用户服务器，监听不同端口，处理不同类型的客户端</li>
 *   <li><b>生命周期管理：</b>配合 Spring 容器，在应用启动/停止时初始化和销毁资源，确保资源正确释放</li>
 *   <li><b>配置管理：</b>从配置文件读取端口等参数，支持灵活的部署配置</li>
 * </ul>
 *
 * <p><b>线程模型设计：</b>
 * <ul>
 *   <li><b>Reactor 线程模型：</b>采用主从 Reactor 多线程模型，BossGroup 负责接受连接，
 *       WorkerGroup 负责处理 IO 读写和业务逻辑</li>
 *   <li><b>设备服务器：</b>
 *     <ul>
 *       <li>Boss Group: 1 个线程，负责接受设备连接（设备连接数相对较少）</li>
 *       <li>Worker Group: 默认线程数（CPU 核心数 * 2），负责处理设备 IO 事件</li>
 *     </ul>
 *   </li>
 *   <li><b>用户服务器：</b>
 *     <ul>
 *       <li>Boss Group: 2 个线程，负责接受用户连接（用户连接数较多，提高连接接受能力）</li>
 *       <li>Worker Group: 8 个线程，显式指定，负责处理用户 IO 事件</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><b>配置项：</b>
 * <ul>
 *   <li><b>netty.device.port:</b> 设备服务器端口，默认 9001</li>
 *   <li><b>netty.user.port:</b> 用户服务器端口，默认 9002</li>
 * </ul>
 *
 * <p><b>启动流程：</b>
 * <ol>
 *   <li>Spring 容器初始化完成后，{@link PostConstruct} 注解的方法被自动调用</li>
 *   <li>分别启动设备服务器和用户服务器，绑定各自端口</li>
 *   <li>服务器启动后，等待客户端连接并处理消息</li>
 *   <li>应用关闭时，{@link PreDestroy} 注解的方法被调用，优雅关闭所有线程组</li>
 * </ol>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>资源隔离：</b>设备服务器和用户服务器使用独立的线程组，避免相互影响</li>
 *   <li><b>优雅关闭：</b>使用 {@link EventLoopGroup#shutdownGracefully()} 确保正在处理的任务完成后再释放资源</li>
 *   <li><b>可配置化：</b>端口号通过 {@link Value} 注解从配置文件注入，支持不同环境灵活配置</li>
 *   <li><b>启动确认：</b>绑定端口后检查 {@link ChannelFuture#isSuccess()} 并记录日志，便于监控启动状态</li>
 *   <li><b>线程优化：</b>根据业务特点为不同服务器配置不同的线程数，平衡资源利用和性能</li>
 * </ul>
 *
 * @author xp
 * @see ServerBootstrap
 * @see EventLoopGroup
 * @see NioEventLoopGroup
 * @see DeviceWebSocketInitializer
 * @see UserWebSocketInitializer
 * @since 2025-04-27
 */
@Slf4j
@Configuration
public class NettyBootstrap {

    /**
     * 设备 WebSocket 服务器端口
     *
     * <p>从配置文件读取，如果未配置则使用默认值 9001。
     * 设备端应连接到此端口进行通信。
     */
    @Value("${netty.device.port:9001}")
    private int devicePort;

    /**
     * 用户 WebSocket 服务器端口
     *
     * <p>从配置文件读取，如果未配置则使用默认值 9002。
     * 用户端（如 Web 应用、移动 App）应连接到此端口。
     */
    @Value("${netty.user.port:9002}")
    private int userPort;

    /**
     * 设备服务器的 Boss 线程组
     *
     * <p>负责接受设备端的连接请求，将连接分发给 Worker 线程组处理。
     */
    private EventLoopGroup deviceBossGroup;

    /**
     * 设备服务器的 Worker 线程组
     *
     * <p>负责处理设备连接的 IO 读写和业务逻辑。
     */
    private EventLoopGroup deviceWorkerGroup;

    /**
     * 用户服务器的 Boss 线程组
     *
     * <p>负责接受用户端的连接请求，将连接分发给 Worker 线程组处理。
     */
    private EventLoopGroup userBossGroup;

    /**
     * 用户服务器的 Worker 线程组
     *
     * <p>负责处理用户连接的 IO 读写和业务逻辑。
     */
    private EventLoopGroup userWorkerGroup;

    /**
     * 启动 Netty 服务器
     *
     * <p>在 Spring 容器初始化完成后自动调用，分别启动设备服务器和用户服务器。
     * 此方法会阻塞直到端口绑定完成，但不会阻塞主线程（sync 方法在当前线程等待）。
     *
     * <p><b>执行时机：</b>所有 Bean 初始化完成后，依赖注入完成后执行。
     *
     * @throws InterruptedException 端口绑定被中断时抛出
     */
    @PostConstruct
    public void start() throws InterruptedException {
        log.info("正在启动 Netty 服务器...");
        startDeviceServer();
        startUserServer();
        log.info("Netty 服务器启动完成 - 设备端口: {}, 用户端口: {}", devicePort, userPort);
    }

    /**
     * 关闭 Netty 服务器
     *
     * <p>在 Spring 容器销毁前自动调用，优雅关闭所有线程组，释放系统资源。
     * 使用 {@link EventLoopGroup#shutdownGracefully()} 确保：
     * <ul>
     *   <li>不再接受新任务</li>
     *   <li>等待已提交的任务执行完成</li>
     *   <li>释放所有资源</li>
     * </ul>
     *
     * <p><b>执行时机：</b>应用上下文关闭时执行。
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭 Netty 服务器...");

        // 关闭设备服务器线程组
        if (deviceBossGroup != null) {
            deviceBossGroup.shutdownGracefully();
            log.debug("设备服务器 Boss 线程组已关闭");
        }
        if (deviceWorkerGroup != null) {
            deviceWorkerGroup.shutdownGracefully();
            log.debug("设备服务器 Worker 线程组已关闭");
        }

        // 关闭用户服务器线程组
        if (userBossGroup != null) {
            userBossGroup.shutdownGracefully();
            log.debug("用户服务器 Boss 线程组已关闭");
        }
        if (userWorkerGroup != null) {
            userWorkerGroup.shutdownGracefully();
            log.debug("用户服务器 Worker 线程组已关闭");
        }

        log.info("Netty 服务器已关闭");
    }

    /**
     * 启动设备 WebSocket 服务器
     *
     * <p>配置并启动设备服务器，使用 {@link DeviceWebSocketInitializer} 初始化管道。
     *
     * <p><b>配置说明：</b>
     * <ul>
     *   <li><b>Boss 线程数：</b>1 - 设备连接相对较少，单线程足够</li>
     *   <li><b>Worker 线程数：</b>默认（CPU 核心数 * 2）- 适合 IO 密集型场景</li>
     *   <li><b>SO_BACKLOG：</b>128 - 等待队列长度，超过此数的连接会被拒绝</li>
     *   <li><b>SO_KEEPALIVE：</b>true - 启用 TCP keep-alive，自动检测死连接</li>
     * </ul>
     *
     * <p><b>TCP 参数说明：</b>
     * <ul>
     *   <li>{@link ChannelOption#SO_BACKLOG}：已完成三次握手但未被应用程序接受的连接队列长度</li>
     *   <li>{@link ChannelOption#SO_KEEPALIVE}：启用 TCP 保活探测，空闲 2 小时后开始探测</li>
     * </ul>
     *
     * @throws InterruptedException 端口绑定被中断时抛出
     */
    private void startDeviceServer() throws InterruptedException {
        // 创建线程组
        deviceBossGroup = new NioEventLoopGroup(1);
        deviceWorkerGroup = new NioEventLoopGroup(); // 使用默认线程数

        // 创建启动引导类
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(deviceBossGroup, deviceWorkerGroup)
                .channel(NioServerSocketChannel.class)           // 使用 NIO 传输
                .childHandler(new DeviceWebSocketInitializer())  // 设置管道初始化器
                .option(ChannelOption.SO_BACKLOG, 128)           // 设置等待队列长度
                .childOption(ChannelOption.SO_KEEPALIVE, true);  // 启用 TCP keep-alive

        // 绑定端口并同步等待
        ChannelFuture future = bootstrap.bind(devicePort).sync();

        // 检查启动结果
        if (future.isSuccess()) {
            log.info("设备 WebSocket 服务器启动成功，端口: {}", devicePort);
        } else {
            log.error("设备 WebSocket 服务器启动失败，端口: {}", devicePort, future.cause());
        }
    }

    /**
     * 启动用户 WebSocket 服务器
     *
     * <p>配置并启动用户服务器，使用 {@link UserWebSocketInitializer} 初始化管道。
     *
     * <p><b>配置说明：</b>
     * <ul>
     *   <li><b>Boss 线程数：</b>2 - 用户连接较多，多线程提高连接接受能力</li>
     *   <li><b>Worker 线程数：</b>8 - 显式指定，处理用户 IO 事件</li>
     *   <li><b>SO_BACKLOG：</b>256 - 更大的等待队列，应对高并发连接</li>
     *   <li><b>SO_KEEPALIVE：</b>true - 启用 TCP keep-alive</li>
     * </ul>
     *
     * <p><b>线程数选择依据：</b>
     * <ul>
     *   <li>Boss 线程数通常设置为 1-2，取决于连接建立的并发量</li>
     *   <li>Worker 线程数可根据实际业务压力调整，一般为 CPU 核心数的 2-4 倍</li>
     * </ul>
     *
     * @throws InterruptedException 端口绑定被中断时抛出
     */
    private void startUserServer() throws InterruptedException {
        // 创建线程组
        userBossGroup = new NioEventLoopGroup(2);
        userWorkerGroup = new NioEventLoopGroup(8); // 显式指定 8 个线程

        // 创建启动引导类
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(userBossGroup, userWorkerGroup)
                .channel(NioServerSocketChannel.class)           // 使用 NIO 传输
                .childHandler(new UserWebSocketInitializer())    // 设置管道初始化器
                .option(ChannelOption.SO_BACKLOG, 256)           // 设置等待队列长度
                .childOption(ChannelOption.SO_KEEPALIVE, true);  // 启用 TCP keep-alive

        // 绑定端口并同步等待
        ChannelFuture future = bootstrap.bind(userPort).sync();

        // 检查启动结果
        if (future.isSuccess()) {
            log.info("用户 WebSocket 服务器启动成功，端口: {}", userPort);
        } else {
            log.error("用户 WebSocket 服务器启动失败，端口: {}", userPort, future.cause());
        }
    }
}