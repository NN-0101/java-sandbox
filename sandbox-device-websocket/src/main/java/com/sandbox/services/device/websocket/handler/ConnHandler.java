package com.sandbox.services.device.websocket.handler;

import com.sandbox.services.device.websocket.channel.ChannelGroup;
import com.sandbox.services.device.websocket.domain.MessageBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @description: 处理连接的handler
 * @author: xp
 * @create: 2025/5/1
 */
@Slf4j
public class ConnHandler extends BaseBusinessHandler {

    private final String serverInstanceId;

    public ConnHandler(String serverInstanceId) {
        this.serverInstanceId = serverInstanceId;
    }

    @Override
    protected void process(ChannelHandlerContext ctx, MessageBO msg) {
        String deviceId = msg.getDeviceId();
        ChannelGroup.addChannel(deviceId, ctx.channel());
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
