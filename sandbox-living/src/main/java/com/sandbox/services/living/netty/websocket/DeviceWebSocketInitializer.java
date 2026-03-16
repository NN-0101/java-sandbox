package com.sandbox.services.living.netty.websocket;

import com.sandbox.services.living.netty.websocket.handler.device.DeviceConnHandler;
import com.sandbox.services.living.netty.websocket.handler.device.DeviceDefaultHandler;
import com.sandbox.services.living.netty.websocket.handler.device.DeviceFrameHandler;
import com.sandbox.services.living.netty.websocket.handler.device.DeviceHeardHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

/**
 * 设备WebSocket服务器通道初始化器
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>管道装配</b>：为每个新连接创建并配置Netty管道(ChannelPipeline)</li>
 *   <li><b>处理器编排</b>：按照处理顺序添加各类处理器，形成完整的消息处理链</li>
 *   <li><b>协议支持</b>：配置HTTP、WebSocket协议所需的各类处理器</li>
 * </ul>
 *
 * <p>处理器顺序（按执行顺序）：</p>
 * <ol>
 *   <li><b>HTTP协议层</b>：HttpServerCodec, ChunkedWriteHandler, HttpObjectAggregator</li>
 *   <li><b>WebSocket协议层</b>：WebSocketServerProtocolHandler - 完成WebSocket握手</li>
 *   <li><b>空闲检测</b>：ReadTimeoutHandler - 60秒无读操作自动断开</li>
 *   <li><b>协议转换</b>：DeviceFrameHandler - WebSocket帧 ↔ 业务对象</li>
 *   <li><b>业务处理链</b>：DeviceConnHandler → DeviceHeardHandler → 其他业务处理器</li>
 *   <li><b>兜底处理</b>：DeviceDefaultHandler - 处理未知消息类型</li>
 * </ol>
 *
 * <p>设计理念：</p>
 * <ul>
 *   <li><b>关注点分离</b>：每个处理器职责单一，通过管道串联</li>
 *   <li><b>可扩展性</b>：新增业务处理器只需添加到管道相应位置</li>
 *   <li><b>协议透明</b>：业务处理器只需处理DeviceMessageBO，无需关心WebSocket细节</li>
 * </ul>
 *
 * @author 0101
 * @create 2026/3/16
 */
public class DeviceWebSocketInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // ========== 1. HTTP协议支持层 ==========
        // HTTP编解码器：处理HTTP请求和响应，完成HTTP协议的消息编解码
        pipeline.addLast(new HttpServerCodec());

        // 分块写入处理器：支持大文件/数据流传输，处理Chunked数据
        pipeline.addLast(new ChunkedWriteHandler());

        // HTTP聚合器：将HTTP消息聚合为完整的FullHttpRequest/Response
        // 参数65536表示最大内容长度为64KB，超过会抛出异常
        pipeline.addLast(new HttpObjectAggregator(65536));

        // ========== 2. WebSocket协议层 ==========
        // WebSocket协议处理器：处理WebSocket握手、控制帧（Ping/Pong/Close）
        // 参数"/ws/device"指定了WebSocket的访问路径
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws/device"));

        // ========== 3. 连接管理 ==========
        // 读超时处理器：客户端60秒内不发送任何消息，自动断开连接
        // 防止僵尸连接占用系统资源
        pipeline.addLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));

        // ========== 4. 协议转换 ==========
        // 设备帧处理器：将TextWebSocketFrame转换为DeviceMessageBO
        // 作为业务处理链的入口
        pipeline.addLast(new DeviceFrameHandler());

        // ========== 5. 业务处理链 ==========
        // 连接认证处理器：处理设备认证，将设备标识绑定到Channel
        pipeline.addLast(new DeviceConnHandler());

        // 心跳处理器：处理设备心跳，维持连接活性
        pipeline.addLast(new DeviceHeardHandler());

        // TODO: 可在此添加其他业务处理器（数据处理、命令下发等）
        // pipeline.addLast(new DeviceDataHandler());
        // pipeline.addLast(new DeviceCommandHandler());

        // ========== 6. 兜底处理器 ==========
        // 默认处理器：处理所有未被前面处理器处理的消息
        // 记录未知消息类型，防止消息被静默丢弃
        pipeline.addLast(new DeviceDefaultHandler());
    }
}