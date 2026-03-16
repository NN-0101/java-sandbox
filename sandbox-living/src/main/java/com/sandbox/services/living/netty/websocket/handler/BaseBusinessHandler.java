package com.sandbox.services.living.netty.websocket.handler;

import com.sandbox.services.living.model.bo.websocket.BaseMessageBO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 业务处理器基类 - 责任链模式的核心抽象类（泛型版本）
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>消息分发</b>：根据消息类型将消息路由到对应的具体处理器</li>
 *   <li><b>职责链串联</b>：当消息类型不匹配时，传递给下一个处理器</li>
 *   <li><b>模板方法</b>：定义处理流程模板，子类只需实现具体的业务逻辑</li>
 *   <li><b>类型安全</b>：通过泛型支持不同类型的消息对象（设备消息、用户消息等）</li>
 * </ul>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>接收泛型类型T的消息对象（T必须继承自BaseMessageBO）</li>
 *   <li>判断消息类型是否与当前处理器匹配（通过getHandlerType()比较）</li>
 *   <li>如果匹配，调用子类的process()方法处理业务</li>
 *   <li>如果不匹配，调用ctx.fireChannelRead()传递给下一个处理器</li>
 * </ol>
 *
 * <p>设计理念：</p>
 * <ul>
 *   <li><b>开闭原则</b>：新增消息类型只需添加新的子类，无需修改现有代码</li>
 *   <li><b>单一职责</b>：每个子类只处理一种消息类型</li>
 *   <li><b>责任链模式</b>：通过管道串联多个处理器，实现消息的路由和处理</li>
 *   <li><b>泛型设计</b>：支持多种消息类型，提高代码复用性和类型安全性</li>
 * </ul>
 *
 * <p>泛型参数说明：</p>
 * <ul>
 *   <li><b>T extends BaseMessageBO</b>：消息类型，必须继承自BaseMessageBO</li>
 *   <li>这样设计可以支持不同类型的消息（如DeviceMessageBO、UserMessageBO等）</li>
 *   <li>子类在继承时需要指定具体的消息类型，如：BaseBusinessHandler&lt;DeviceMessageBO&gt;</li>
 * </ul>
 *
 * <p>使用方式（设备消息处理器）：</p>
 * <pre>
 * public class DeviceConnHandler extends BaseBusinessHandler&lt;DeviceMessageBO&gt; {
 *     {@literal @}Override
 *     protected void process(ChannelHandlerContext ctx, DeviceMessageBO msg) {
 *         String macId = msg.getMacId();  // 直接使用DeviceMessageBO的特有方法
 *         // 具体的业务处理逻辑
 *     }
 *
 *     {@literal @}Override
 *     public int getHandlerType() {
 *         return MessageTypeEnum.CONN.getValue();
 *     }
 * }
 * </pre>
 *
 * <p>使用方式（用户消息处理器）：</p>
 * <pre>
 * public class UserAuthHandler extends BaseBusinessHandler&lt;UserMessageBO&gt; {
 *     {@literal @}Override
 *     protected void process(ChannelHandlerContext ctx, UserMessageBO msg) {
 *         String userId = msg.getUserId();  // 直接使用UserMessageBO的特有方法
 *         String token = msg.getToken();
 *         // 用户认证逻辑
 *     }
 *
 *     {@literal @}Override
 *     public int getHandlerType() {
 *         return MessageTypeEnum.USER_AUTH.getValue();
 *     }
 * }
 * </pre>
 *
 * <p>管道配置示例：</p>
 * <pre>
 * // 设备服务器管道配置
 * pipeline.addLast(new DeviceFrameHandler());     // 将TextWebSocketFrame转换为DeviceMessageBO
 * pipeline.addLast(new DeviceConnHandler());      // 处理CONN消息
 * pipeline.addLast(new DeviceHeardHandler());     // 处理HEARTBEAT消息
 * pipeline.addLast(new DeviceDefaultHandler());   // 兜底处理
 *
 * // 用户服务器管道配置
 * pipeline.addLast(new UserFrameHandler());       // 将TextWebSocketFrame转换为UserMessageBO
 * pipeline.addLast(new UserAuthHandler());        // 处理用户认证
 * pipeline.addLast(new UserHeartbeatHandler());   // 处理用户心跳
 * pipeline.addLast(new UserDefaultHandler());     // 兜底处理
 * </pre>
 *
 * @param <T> 消息类型，必须继承自BaseMessageBO
 * @author xp
 * @create 2025/5/1
 */
public abstract class BaseBusinessHandler<T extends BaseMessageBO> extends SimpleChannelInboundHandler<T> {

    /**
     * 业务处理模板方法
     *
     * <p>子类实现此方法，编写具体的业务逻辑。
     * 此方法仅在消息类型匹配时被调用。</p>
     *
     * <p>由于使用了泛型，子类可以直接使用具体消息类型的特有方法，
     * 无需进行类型转换，保证了类型安全。</p>
     *
     * @param ctx Channel处理器上下文，包含管道、通道、属性等信息
     * @param msg 泛型类型的消息对象，具体类型由子类指定
     */
    protected abstract void process(ChannelHandlerContext ctx, T msg);

    /**
     * 获取当前处理器支持的消息类型
     *
     * <p>子类返回它能够处理的消息类型值，
     * 用于与{@link BaseMessageBO#getType()}进行比较。</p>
     *
     * <p>消息类型值通常定义在枚举类中，如：</p>
     * <pre>
     * public enum MessageTypeEnum {
     *     CONN(1, "连接"),
     *     HEARTBEAT(2, "心跳"),
     *     DATA(3, "数据"),
     *     // ...
     * }
     * </pre>
     *
     * @return 消息类型值，对应具体业务含义
     */
    public abstract int getHandlerType();

    /**
     * 消息读取入口 - 实现责任链路由逻辑
     *
     * <p>这是Netty框架调用的入口方法，实现了消息的路由分发：</p>
     * <ul>
     *   <li><b>类型匹配</b>：比较消息的type字段与当前处理器支持的type</li>
     *   <li><b>业务处理</b>：如果匹配，调用子类的process()方法执行业务逻辑</li>
     *   <li><b>责任链传递</b>：如果不匹配，通过fireChannelRead()传递给下一个处理器</li>
     * </ul>
     *
     * <p>这种设计实现了责任链模式，每个处理器只关心自己能够处理的消息类型，
     * 无法处理的消息会自动沿管道向下传递，直到找到能够处理的处理器或到达末端。</p>
     *
     * @param ctx Channel处理器上下文
     * @param msg 泛型类型的消息对象
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) {
        if (msg.getType() == getHandlerType()) {
            // 消息类型匹配，由当前处理器处理
            process(ctx, msg);
        } else {
            // 消息类型不匹配，传递给管道中的下一个处理器
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 异常处理
     *
     * <p>可选的异常处理方法，子类可以根据需要重写此方法。
     * 默认实现将异常传递给下一个处理器。</p>
     *
     * @param ctx Channel处理器上下文
     * @param cause 异常对象
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 默认将异常传递给下一个处理器
        ctx.fireExceptionCaught(cause);
    }
}