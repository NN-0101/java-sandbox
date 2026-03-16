package com.sandbox.services.living.netty.websocket.handler;

import com.alibaba.fastjson2.JSONObject;
import com.sandbox.services.living.netty.websocket.channel.ChannelGroup;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 消息帧处理器
 * <p>
 * 职责：
 * 1. 协议转换：将 Netty 的 TextWebSocketFrame 转换为业务对象 NettyMessageBO
 * 2. 连接生命周期管理：处理客户端连接、断开、异常事件
 * 3. 资源清理：连接断开时从 ChannelGroup 中移除并清理相关资源
 * 4. 作为消息处理管道的入口适配器，转换后传递给下游业务处理器
 * 工作流程：
 * - 接收客户端发送的 WebSocket 文本帧 -> 解析为业务对象 -> 传递给下一个处理器
 * - 监控连接状态 -> 连接建立/断开时记录日志并清理资源
 * - 捕获异常 -> 触发清理流程并关闭连接
 *
 * @author 0101
 * @create 2026/3/16
 */
@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 读取客户端发送的 WebSocket 文本消息
     * 处理流程：
     * 1. 从 TextWebSocketFrame 中提取文本内容
     * 2. 将 JSON 格式的文本解析为 NettyMessageBO 业务对象
     * 3. 将解析后的对象传递给管道中的下一个处理器（责任链模式）
     * 这样做的目的是将底层协议细节（WebSocket 帧）与业务逻辑解耦，
     * 下游处理器只需要处理 NettyMessageBO 对象，无需关心原始消息格式。
     *
     * @param ctx ChannelHandlerContext 处理器上下文，包含管道、通道等信息
     * @param msg TextWebSocketFrame 客户端发送的 WebSocket 文本帧
     * @throws Exception 解析异常时抛出
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String request = msg.text();
        NettyMessageBO nettyMeg = JSONObject.parseObject(request, NettyMessageBO.class);
        //往下传递
        ctx.fireChannelRead(nettyMeg);
    }

    /**
     * 处理新客户端连接事件
     * 当客户端成功建立 WebSocket 连接时调用。
     * 目前仅用于记录连接日志，实际的身份认证、连接注册等操作在其他处理器中完成。
     * 注意：这里没有进行 Channel 注册，因为此时客户端还未发送任何消息，
     * 用户身份信息（如 uid）尚未获取，真正的注册逻辑在收到业务消息后进行。
     *
     * @param ctx ChannelHandlerContext 处理器上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("新客户端连接: (ps单纯记录连接事件) " + ctx.channel().remoteAddress());
    }

    /**
     * 处理客户端断开连接事件
     * 当连接关闭时调用，负责清理与该连接相关的所有资源：
     * 1. 从 Channel 的属性中获取用户标识（uid 和 clientId）
     * 2. 从全局的 ChannelGroup 中移除该连接
     * 3. 记录断开日志
     * 这样可以确保用户离线时，系统状态得到及时更新，避免资源泄露。
     *
     * @param ctx ChannelHandlerContext 处理器上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 从 Channel 属性中获取用户 ID
        AttributeKey<String> userIdKey = AttributeKey.valueOf(Constant.UID);
        AttributeKey<String> clientKey = AttributeKey.valueOf(Constant.CLIENT_ID);
        String uid = ctx.channel().attr(userIdKey).get();
        String clientId = ctx.channel().attr(clientKey).get();
        if (uid != null) {
            ChannelGroup.removeChannel(uid, clientId);
            log.info("Uid {} clientId: {} disconnect，cleaning completed", uid, clientId);
        }
        log.info("客户端断开连接: {}", ctx.channel().remoteAddress());
    }

    /**
     * 处理异常事件
     * 当管道处理过程中发生异常时调用：
     * 1. 主动调用 channelInactive 确保资源清理（因为异常后可能不会触发 channelInactive）
     * 2. 记录错误日志
     * 3. 关闭连接释放资源
     * 这种设计确保了即使在异常情况下，也能正确清理连接资源，避免连接泄露。
     *
     * @param ctx   ChannelHandlerContext 处理器上下文
     * @param cause 异常对象
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 触发清理
        this.channelInactive(ctx);
        log.error("netty error : (ps异常事件)", cause);
        ctx.close();
    }
}
