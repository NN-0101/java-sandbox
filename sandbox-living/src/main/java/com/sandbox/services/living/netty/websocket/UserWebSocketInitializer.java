package com.sandbox.services.living.netty.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @description:
 * @author: 0101
 * @create: 2026/3/16
 */
public class UserWebSocketInitializer extends ChannelInitializer<SocketChannel> {
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
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws/user"));
    }
}
