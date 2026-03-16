package com.sandbox.services.living.netty.websocket.handler.device;

import com.alibaba.fastjson2.JSONObject;
import com.sandbox.services.living.enumeration.websocket.MessageTypeEnum;
import com.sandbox.services.living.model.bo.websocket.device.DeviceMessageBO;
import com.sandbox.services.living.model.bo.websocket.device.PushDeviceMessageBO;
import com.sandbox.services.living.netty.websocket.handler.BaseBusinessHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备心跳处理器
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>心跳接收</b>：处理设备发送的心跳消息，维持连接活性</li>
 *   <li><b>心跳响应</b>：向设备响应心跳，确认连接正常</li>
 *   <li><b>状态监控</b>：记录心跳日志，可用于监控设备在线状态</li>
 * </ul>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>从Channel Attribute获取设备macId（由DeviceConnHandler设置）</li>
 *   <li>记录心跳日志，可用于更新设备最后在线时间</li>
 *   <li>构造心跳响应消息并发送给设备</li>
 * </ol>
 *
 * <p>设计理念：</p>
 * <ul>
 *   <li><b>轻量快速</b>：心跳处理逻辑应尽可能简单，避免耗时操作</li>
 *   <li><b>无状态</b>：不维护心跳状态，状态通过Channel Attribute存储</li>
 *   <li><b>可扩展</b>：可在此添加心跳计数、最后心跳时间等监控指标</li>
 * </ul>
 *
 * @author xp
 * @create 2025/5/1
 */
@Slf4j
public class DeviceHeardHandler extends BaseBusinessHandler<DeviceMessageBO> {

    /**
     * 处理设备心跳消息
     *
     * @param ctx Channel处理器上下文
     * @param msg 心跳消息对象（type=HEARD）
     */
    @Override
    protected void process(ChannelHandlerContext ctx, DeviceMessageBO msg) {
        // 从Channel Attribute中获取设备MAC地址（由DeviceConnHandler设置）
        AttributeKey<String> macIdKey = AttributeKey.valueOf("macId");
        String macId = ctx.channel().attr(macIdKey).get();

        log.info("接收设备心跳: macId={}", macId);

        // TODO: 可扩展点 - 更新设备最后心跳时间
        // updateLastHeartbeatTime(macId);

        // TODO: 可扩展点 - 心跳计数
        // incrementHeartbeatCount(ctx);

        // 构造心跳响应消息
        PushDeviceMessageBO pushMessageBO = new PushDeviceMessageBO();
        pushMessageBO.setContent(MessageTypeEnum.HEARTBEAT.getDescription());
        pushMessageBO.setMessageType(MessageTypeEnum.HEARTBEAT.getValue());

        // 发送心跳响应
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSONObject.toJSONString(pushMessageBO));
        ctx.writeAndFlush(textWebSocketFrame);
    }

    /**
     * 获取处理器支持的消息类型
     *
     * @return 消息类型值，对应MessageTypeEnum.HEARD
     */
    @Override
    public int getHandlerType() {
        return MessageTypeEnum.HEARTBEAT.getValue();
    }
}