package com.sandbox.services.netty.handler;

import com.sandbox.services.netty.domain.NettyMessageBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @description: 处理连接的handler
 * @author: xp
 * @create: 2025/5/1
 */
@Slf4j
public class ConnHandler extends BaseBusinessHandler {

    private final RedisTemplate<String, Object> redisTemplate;

    private final String serverInstanceId;

    public ConnHandler(RedisTemplate<String, Object> redisTemplate, String serverInstanceId) {
        this.redisTemplate = redisTemplate;
        this.serverInstanceId = serverInstanceId;
    }

    @Override
    protected void process(ChannelHandlerContext ctx, NettyMessageBO msg) {
        String uid = msg.getUid();
        String clientId = msg.getClientId();
        // 保存用户与实例的映射到 Redis（有效期 1 分钟） 用户id 客户端 对应实例
        String redisKey = String.format(RedisKeyConstant.USER_NETTY_CONNECTION_REDIS_KEY, uid);
        redisTemplate.opsForHash().put(redisKey, clientId, serverInstanceId);
        redisTemplate.expire(redisKey, 60, TimeUnit.SECONDS);
        log.info("Uid: {} clientId: {} serverInstanceId {}", uid, clientId, serverInstanceId);
        ChannelGroup.addChannel(uid, clientId, ctx.channel());
        // 通过 ctx.channel().attr() 将用户 ID 绑定到 Channel，确保断开时能快速定位需清理的资源。
        AttributeKey<String> userIdKey = AttributeKey.valueOf(Constant.UID);
        AttributeKey<String> clientKey = AttributeKey.valueOf(Constant.CLIENT_ID);
        ctx.channel().attr(userIdKey).set(uid);
        ctx.channel().attr(clientKey).set(clientId);
        //TODO 查询没有接收到的消息,重新推送
    }

    @Override
    public int getHandlerType() {
        return MessageTypeEnum.CONN.getValue();
    }
}
