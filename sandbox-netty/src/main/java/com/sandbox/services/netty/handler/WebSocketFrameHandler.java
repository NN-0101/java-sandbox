package com.sandbox.services.netty.handler;

import com.alibaba.fastjson2.JSONObject;
import com.zozo.netty.channel.ChannelGroup;
import com.zozo.netty.constant.Constant;
import com.zozo.netty.constant.RedisKeyConstant;
import com.zozo.netty.domain.NettyMessageBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @description: \
 * @author: xp
 * @create: 2025/4/27
 */
@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final RedisTemplate<String, Object> redisTemplate;

    public WebSocketFrameHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String request = msg.text();
        NettyMessageBO nettyMeg = JSONObject.parseObject(request, NettyMessageBO.class);
        //往下传递
        ctx.fireChannelRead(nettyMeg);
    }

    /**
     * 单纯记录连接事件
     *
     * @param ctx ChannelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("新客户端连接: (ps单纯记录连接事件) " + ctx.channel().remoteAddress());
    }

    /**
     * 记录断开事件
     *
     * @param ctx ChannelHandlerContext
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 从 Channel 属性中获取用户 ID
        AttributeKey<String> userIdKey = AttributeKey.valueOf(Constant.UID);
        AttributeKey<String> clientKey = AttributeKey.valueOf(Constant.CLIENT_ID);
        String uid = ctx.channel().attr(userIdKey).get();
        String clientId = ctx.channel().attr(clientKey).get();
        if (uid != null) {
            String redisKey = String.format(RedisKeyConstant.USER_NETTY_CONNECTION_REDIS_KEY, uid);
            // 1. 删除 Redis 中的用户连接记录
            redisTemplate.opsForHash().delete(redisKey, clientId);
            // 2. 从 ChannelGroup 中移除 Channel
            ChannelGroup.removeChannel(uid, clientId);
            log.info("Uid {} clientId: {} disconnect，cleaning completed", uid, clientId);
        }
        log.info("客户端断开连接: {}", ctx.channel().remoteAddress());
    }

    /**
     * 异常事件
     *
     * @param ctx   ChannelHandlerContext
     * @param cause cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 触发清理
        this.channelInactive(ctx);
        log.error("netty error : (ps异常事件)", cause);
        ctx.close();
    }
}
