package com.sandbox.services.living.netty.websocket.handler;

import com.alibaba.fastjson2.JSONObject;
import com.sandbox.services.living.model.bo.websocket.DeviceMessageBO;
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
 *   <li><b>连接管理</b>：监控设备连接的生命周期（建立、断开、异常）</li>
 *   <li><b>资源清理</b>：设备断开时从{@link DeviceChannelGroup}中移除连接，防止内存泄漏</li>
 *   <li><b>消息路由</b>：作为消息处理管道的入口，将解析后的业务对象传递给下游处理器</li>
 * </ul>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li><b>消息接收</b>：接收设备发送的WebSocket文本帧 → 解析JSON为DeviceMessageBO → 传递给下游处理器</li>
 *   <li><b>连接建立</b>：记录新设备连接日志（此时尚未认证，不进行注册）</li>
 *   <li><b>连接断开</b>：获取设备标识(macId) → 从DeviceChannelGroup移除 → 记录断开日志</li>
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
     * <p>为什么要这样做？</p>
     * <ul>
     *   <li><b>解耦</b>：下游处理器无需关心WebSocket协议细节和JSON解析逻辑</li>
     *   <li><b>复用</b>：DeviceMessageBO可以在整个业务链路中统一使用</li>
     *   <li><b>清晰</b>：每个处理器职责单一，便于维护和测试</li>
     * </ul>
     *
     * @param ctx Channel处理器上下文，包含管道、通道、属性等信息
     * @param msg 设备发送的WebSocket文本帧
     * @throws Exception JSON解析异常或下游处理异常
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 提取文本内容
        String request = msg.text();

        // JSON反序列化为设备消息对象
        DeviceMessageBO deviceMessageBO = JSONObject.parseObject(request, DeviceMessageBO.class);

        // 传递给下一个处理器（如认证处理器、业务处理器等）
        ctx.fireChannelRead(deviceMessageBO);

        // 注意：这里不需要释放msg，Netty会自动处理引用计数
    }

    /**
     * 处理设备连接建立事件
     *
     * <p>当设备成功建立WebSocket连接时调用。</p>
     *
     * <p><b>为什么不在这里注册设备？</b></p>
     * <ul>
     *   <li>此时设备还未发送任何消息，无法获取设备标识(macId)</li>
     *   <li>真正的认证和注册逻辑在后续处理器中，当收到包含设备标识的业务消息时才执行</li>
     *   <li>避免未认证的连接占用注册表资源</li>
     * </ul>
     *
     * <p><b>当前处理：</b></p>
     * <ul>
     *   <li>记录新连接日志，便于监控和调试</li>
     *   <li>可根据需要扩展：连接计数器、监控指标收集等</li>
     * </ul>
     *
     * @param ctx Channel处理器上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 记录连接信息
        log.info("新设备连接: remoteAddress={}", ctx.channel().remoteAddress());
        // TODO: 可扩展点 - 添加连接监控指标
        // MetricsCollector.incrementConnectionCount();
    }

    /**
     * 处理设备断开连接事件
     *
     * <p>当设备连接关闭（正常断开或网络异常）时调用，负责清理资源：</p>
     * <ol>
     *   <li><b>获取设备标识</b>：从Channel的Attribute中读取设备macId（由认证处理器设置）</li>
     *   <li><b>移除连接</b>：从{@link DeviceChannelGroup}中移除该设备的连接记录</li>
     *   <li><b>记录日志</b>：记录设备断开信息，便于问题追踪</li>
     * </ol>
     *
     * <p><b>为什么需要这个清理过程？</b></p>
     * <ul>
     *   <li>防止内存泄漏：不清理会导致DeviceChannelGroup中积累大量已断开的连接</li>
     *   <li>状态同步：确保系统知道设备已离线，避免向已断开的设备发送消息</li>
     *   <li>资源释放：帮助GC回收Channel相关的对象</li>
     * </ul>
     *
     * @param ctx Channel处理器上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 定义获取设备标识的AttributeKey
        AttributeKey<String> macIdKey = AttributeKey.valueOf("macId");

        // 从Channel属性中获取设备macId（由认证处理器在设备认证成功后设置）
        String macId = ctx.channel().attr(macIdKey).get();

        // 如果设备已认证（macId不为空），则从连接组中移除
        if (macId != null) {
            DeviceChannelGroup.removeChannel(macId);
            log.info("设备离线: macId={}, remoteAddress={}, 资源清理完成",
                    macId, ctx.channel().remoteAddress());
        } else {
            // 未认证的设备断开，只需记录日志
            log.info("未认证设备断开连接: remoteAddress={}", ctx.channel().remoteAddress());
        }
    }

    /**
     * 处理异常事件
     *
     * <p>当管道处理过程中发生异常时调用，确保资源正确释放：</p>
     * <ol>
     *   <li><b>触发清理</b>：主动调用{@link #channelInactive(ChannelHandlerContext)}进行资源清理</li>
     *   <li><b>记录异常</b>：打印错误日志，包含异常堆栈</li>
     *   <li><b>关闭连接</b>：调用{@link ChannelHandlerContext#close()}释放底层资源</li>
     * </ol>
     *
     * <p><b>为什么需要主动调用channelInactive？</b></p>
     * <ul>
     *   <li>在某些异常情况下，channelInactive可能不会被自动调用</li>
     *   <li>如果不清理，会导致DeviceChannelGroup中存在无效连接</li>
     *   <li>主动清理确保系统状态的一致性</li>
     * </ul>
     *
     * @param ctx   Channel处理器上下文
     * @param cause 异常对象
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        try {
            // 主动触发资源清理（确保连接被正确移除）
            this.channelInactive(ctx);

            // 记录异常信息
            log.error("设备连接异常: remoteAddress={}, 错误类型={}",
                    ctx.channel().remoteAddress(), cause.getClass().getSimpleName(), cause);
        } finally {
            // 关闭连接，释放Netty资源
            ctx.close();
        }
    }
}