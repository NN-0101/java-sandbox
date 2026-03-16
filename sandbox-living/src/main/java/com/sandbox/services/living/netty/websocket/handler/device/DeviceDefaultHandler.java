package com.sandbox.services.living.netty.websocket.handler.device;

import com.sandbox.services.living.model.bo.websocket.device.DeviceMessageBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 设备消息兜底处理器
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>未知消息处理</b>：捕获管道中没有处理器处理的消息类型</li>
 *   <li><b>异常监控</b>：记录未处理的消息类型，便于发现协议不匹配或遗漏的处理器</li>
 *   <li><b>消息终结</b>：作为管道中的最后一个处理器，防止消息继续传播</li>
 * </ul>
 *
 * <p>设计理念：</p>
 * <ul>
 *   <li><b>fail-fast</b>：快速发现未处理的消息类型，避免消息被静默丢弃</li>
 *   <li><b>监控告警</b>：通过错误日志触发监控系统告警，及时发现问题</li>
 *   <li><b>协议完整性</b>：确保所有定义的消息类型都有对应的处理器</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>设备发送了未定义的消息类型</li>
 *   <li>新增消息类型但忘记添加对应的处理器</li>
 *   <li>协议版本不匹配导致的消息类型错误</li>
 * </ul>
 *
 * @author xp
 * @create 2025/5/1
 */
@Slf4j
public class DeviceDefaultHandler extends SimpleChannelInboundHandler<DeviceMessageBO> {

    /**
     * 处理未匹配的消息类型
     *
     * @param ctx Channel处理器上下文
     * @param msg 未处理的设备消息对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DeviceMessageBO msg) {
        // 记录错误日志，包含设备标识（如果已认证）和未处理的消息类型
        String macId = ctx.channel().attr(AttributeKey.valueOf("macId")).get().toString();
        if (macId != null) {
            log.error("设备 {} 发送了未处理的消息类型: {}", macId, msg.getType());
        } else {
            log.error("未认证设备发送了未处理的消息类型: {}, remoteAddress={}",
                    msg.getType(), ctx.channel().remoteAddress());
        }

        // 可根据需要发送错误响应给设备
        // sendErrorResponse(ctx, "unsupported message type: " + msg.getType());
    }
}