package com.sandbox.services.netty.handler;

import com.sandbox.services.netty.domain.NettyMessageBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @description: 业务处理器基类
 * @author: xp
 * @create: 2025/5/1
 */
public abstract class BaseBusinessHandler extends SimpleChannelInboundHandler<NettyMessageBO> {

    /**
     * 所有子类只需实现process方法
     *
     * @param ctx ChannelHandlerContext
     * @param msg NettyMeg
     */
    protected abstract void process(ChannelHandlerContext ctx, NettyMessageBO msg);

    /**
     * 由子类返回它处理的类型
     *
     * @return type
     */
    public abstract int getHandlerType();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyMessageBO msg) throws Exception {
        if (msg.getType() == getHandlerType()) {
            process(ctx, msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
