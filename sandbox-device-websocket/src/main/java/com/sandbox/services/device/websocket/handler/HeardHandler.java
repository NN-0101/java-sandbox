package com.sandbox.services.device.websocket.handler;

import com.alibaba.fastjson2.JSONObject;
import com.zozo.netty.constant.Constant;
import com.zozo.netty.constant.RedisKeyConstant;
import com.zozo.netty.domain.NettyMessageBO;
import com.zozo.netty.domain.PushMessageBO;
import com.zozo.netty.enumeration.MessageContentTypeEnum;
import com.zozo.netty.enumeration.MessageTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @description: 处理心跳的handler
 * @author: xp
 * @create: 2025/5/1
 */
@Slf4j
public class HeardHandler extends BaseBusinessHandler {

    private final RedisTemplate<String, Object> redisTemplate;

    public HeardHandler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void process(ChannelHandlerContext ctx, NettyMessageBO msg) {
        // 从 Channel 属性中获取用户信息
        AttributeKey<String> userIdKey = AttributeKey.valueOf(Constant.UID);
        AttributeKey<String> clientKey = AttributeKey.valueOf(Constant.CLIENT_ID);
        String uid = ctx.channel().attr(userIdKey).get();
        String clientId = ctx.channel().attr(clientKey).get();
        log.info("receive heartbeat  Uid :{} ,clientId :{}", uid, clientId);
        String redisKey = String.format(RedisKeyConstant.USER_NETTY_CONNECTION_REDIS_KEY, uid);
        redisTemplate.expire(redisKey, 60, TimeUnit.SECONDS);

        PushMessageBO pushMessageBO = new PushMessageBO();
        pushMessageBO.setContent(Constant.HEARD);
        pushMessageBO.setMessageType(MessageTypeEnum.HEARD.getValue());
        pushMessageBO.setContentType(MessageContentTypeEnum.TEXT.getValue());
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSONObject.toJSONString(pushMessageBO));
        ctx.writeAndFlush(textWebSocketFrame);
    }

    @Override
    public int getHandlerType() {
        return MessageTypeEnum.HEARD.getValue();
    }
}
