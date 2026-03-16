package com.sandbox.services.living.netty.websocket;

import com.zozo.services.service.MessageService;
import com.zozo.services.websocket.handler.ConnHandler;
import com.zozo.services.websocket.handler.DefaultHandler;
import com.zozo.services.websocket.handler.HeardHandler;
import com.zozo.services.websocket.handler.WebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * @description: WebSocket初始化器
 * @author: xp
 * @create: 2025/4/27
 */
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // HTTP编解码器
        pipeline.addLast(new HttpServerCodec());
        // 支持大数据流
        pipeline.addLast(new ChunkedWriteHandler());
        // HTTP聚合器
        pipeline.addLast(new HttpObjectAggregator(65536));
        // 添加自定义的HTTP请求过滤器
//        pipeline.addLast(new TokenAuthHandler(redisTemplate));
        // WebSocket协议处理器
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        //客户端 60 秒后不发送消息自动断开
//        pipeline.addLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS));
        // 自定义处理器
        pipeline.addLast(new WebSocketFrameHandler(redisTemplate));
        // 新连接处理器
        pipeline.addLast(new ConnHandler(messageService));
        // 心跳处理器
        pipeline.addLast(new HeardHandler(redisTemplate));
        // 兜底处理器
        pipeline.addLast(new DefaultHandler());
    }
}
