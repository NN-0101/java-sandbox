package com.sandbox.services.living.netty.websocket.handler.device;

import com.alibaba.fastjson2.JSONObject;
import com.sandbox.services.living.model.bo.websocket.device.DeviceMessageBO;
import com.sandbox.services.living.netty.websocket.channel.DeviceChannelGroup;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备WebSocket消息帧处理器
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>协议适配</b>：将Netty的WebSocket文本帧({@link TextWebSocketFrame})转换为设备业务对象({@link DeviceMessageBO})</li>
 *   <li><b>连接生命周期管理</b>：监控设备连接的生命周期（建立、断开、异常）</li>
 *   <li><b>资源清理</b>：设备断开时从{@link DeviceChannelGroup}中移除连接，并清理Channel Attribute</li>
 *   <li><b>消息路由</b>：作为消息处理管道的入口，将解析后的业务对象传递给下游处理器</li>
 * </ul>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li><b>消息接收</b>：接收设备发送的WebSocket文本帧 → 解析JSON为DeviceMessageBO → 传递给下游处理器</li>
 *   <li><b>连接建立</b>：记录新设备连接日志（此时尚未认证，不进行注册）</li>
 *   <li><b>连接断开</b>：从Channel Attribute获取设备标识(macId) → 从DeviceChannelGroup移除 → 记录断开日志</li>
 *   <li><b>异常处理</b>：触发资源清理 → 记录异常日志 → 关闭连接</li>
 * </ol>
 *
 * <p>设计理念：</p>
 * <ul>
 *   <li><b>关注点分离</b>：只负责协议转换和连接生命周期，业务逻辑交由下游处理器</li>
 *   <li><b>异常安全</b>：异常时主动调用channelInactive确保资源释放</li>
 *   <li><b>无状态设计</b>：不维护设备状态，状态通过Channel的Attribute存储</li>
 * </ul>
 *
 * @author 0101
 * @create 2026/3/16
 */
@Slf4j
public class DeviceFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 处理设备发送的WebSocket文本消息
     *
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从{@link TextWebSocketFrame}中提取JSON格式的文本内容</li>
     *   <li>使用FastJson2将JSON解析为{@link DeviceMessageBO}业务对象</li>
     *   <li>通过{@link ChannelHandlerContext#fireChannelRead(Object)}将业务对象传递给管道中的下一个处理器</li>
     * </ol>
     *
     * @param ctx Channel处理器上下文，包含管道、通道、属性等信息
     * @param msg 设备发送的WebSocket文本帧
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        String request = msg.text();
        DeviceMessageBO deviceMessageBO = JSONObject.parseObject(request, DeviceMessageBO.class);
        ctx.fireChannelRead(deviceMessageBO);
    }

    /**
     * 处理设备连接建立事件
     *
     * <p>当设备成功建立WebSocket连接时调用。
     * 此时设备还未认证，只记录连接日志，不进行注册。</p>
     *
     * @param ctx Channel处理器上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("新设备连接: remoteAddress={}", ctx.channel().remoteAddress());
    }

    /**
     * 处理设备断开连接事件
     *
     * <p>当设备连接关闭时调用，负责清理资源：</p>
     * <ol>
     *   <li>从Channel的Attribute中获取设备macId（由DeviceConnHandler设置）</li>
     *   <li>从{@link DeviceChannelGroup}中移除该设备的连接记录</li>
     *   <li>记录设备断开日志</li>
     * </ol>
     *
     * @param ctx Channel处理器上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        AttributeKey<String> macIdKey = AttributeKey.valueOf("macId");
        String macId = ctx.channel().attr(macIdKey).get();

        if (macId != null) {
            DeviceChannelGroup.removeChannel(macId);
            log.info("设备离线: macId={}, remoteAddress={}, 资源清理完成",
                    macId, ctx.channel().remoteAddress());
        } else {
            log.info("未认证设备断开连接: remoteAddress={}", ctx.channel().remoteAddress());
        }
    }

    /**
     * 处理异常事件
     *
     * <p>当管道处理过程中发生异常时调用，确保资源正确释放：</p>
     * <ol>
     *   <li>主动调用{@link #channelInactive(ChannelHandlerContext)}进行资源清理</li>
     *   <li>记录异常日志</li>
     *   <li>关闭连接释放资源</li>
     * </ol>
     *
     * @param ctx   Channel处理器上下文
     * @param cause 异常对象
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            this.channelInactive(ctx);
            log.error("设备连接异常: remoteAddress={}, 错误类型={}",
                    ctx.channel().remoteAddress(), cause.getClass().getSimpleName(), cause);
        } finally {
            ctx.close();
        }
    }
}