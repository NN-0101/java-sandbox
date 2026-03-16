package com.sandbox.services.living.netty.websocket;

import com.sandbox.services.living.netty.websocket.handler.DeviceFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @description:
 * @author: 0101
 * @create: 2026/3/16
 */
public class DeviceWebSocketInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // ========== HTTP协议支持层 ==========
        // HTTP编解码器：处理HTTP请求和响应
        pipeline.addLast(new HttpServerCodec());

        // 分块写入处理器：支持大文件/数据流传输
        pipeline.addLast(new ChunkedWriteHandler());

        // HTTP聚合器：将HTTP消息聚合为完整的FullHttpRequest/Response
        // 参数65536表示最大内容长度为64KB
        pipeline.addLast(new HttpObjectAggregator(65536));

        // WebSocket协议处理器（设备专用路径）
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws/device"));
        //客户端 60 秒后不发送消息自动断开
//        pipeline.addLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
        // 消息转换
        pipeline.addLast(new DeviceFrameHandler());
        // 新连接处理器
        pipeline.addLast(new ConnHandler(messageService));
        // 心跳处理器
        pipeline.addLast(new HeardHandler(redisTemplate));
        // 兜底处理器
        pipeline.addLast(new DefaultHandler());
    }
}
