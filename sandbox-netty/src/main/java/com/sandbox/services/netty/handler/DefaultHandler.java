package com.sandbox.services.netty.handler;

import com.zozo.netty.domain.NettyMessageBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @description: 兜底处理器
 * @author: xp
 * @create: 2025/5/1
 */
public class DefaultHandler extends SimpleChannelInboundHandler<NettyMessageBO> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyMessageBO msg) throws Exception {
        System.err.println("未处理的消息类型: " + msg.getType());
    }
}
