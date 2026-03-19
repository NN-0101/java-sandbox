package com.sandbox.services.living.model.bo.websocket;

import com.sandbox.services.living.enumeration.websocket.DeviceUpMessageTypeEnum;
import lombok.Data;

/**
 * WebSocket 上行消息基类 - 所有设备/客户端发送给平台的消息对象的父类
 *
 * <p>该类是所有上行消息的抽象基类，定义了上行消息的基础结构。
 * 所有从设备或客户端发送到平台的消息都应继承此类，并通过 {@link #messageType} 字段
 * 标识具体的消息类型，实现消息的多态处理和路由分发。</p>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>类型标识</b>：提供消息类型字段 {@link #messageType}，用于消息路由和分发，
 *       子类可根据具体业务定义对应的类型枚举（如 {@link DeviceUpMessageTypeEnum}）</li>
 *   <li><b>统一基类</b>：作为所有上行消息对象的父类，实现多态处理，管道中可以传递
 *       基类引用，实际类型由子类决定</li>
 *   <li><b>协议定义</b>：定义消息的基础结构，确保所有上行消息都包含类型信息，
 *       为消息处理框架提供统一入口</li>
 * </ul>
 *
 * <p><b>设计理念：</b>
 * <ul>
 *   <li><b>类型驱动</b>：通过 {@code messageType} 字段实现消息的路由，每个处理器根据 {@code messageType} 决定是否处理，
 *       形成清晰的责任链模式</li>
 *   <li><b>开闭原则</b>：新增消息类型时，只需扩展子类并添加对应的处理器，无需修改现有代码</li>
 *   <li><b>多态支持</b>：管道中可以传递 BaseUpMessageBO 引用，实际类型由子类决定，
 *       处理器可通过 instanceof 或类型字段进行判断</li>
 * </ul>
 *
 * <p><b>继承体系示例：</b>
 * <pre>
 * BaseUpMessageBO（基础上行消息，包含 messageType）
 * ├── {@link com.sandbox.services.living.model.bo.websocket.device.DeviceUpMessageBO}
 * │   └── 设备上行消息，包含 macId 设备标识
 * └── UserUpMessageBO（用户上行消息，包含 userId、token 等）
 *     └── 后续可根据需要扩展更多业务类型
 * </pre>
 *
 * <p><b>使用方式：</b>
 * <pre>
 * // 设备连接认证消息
 * public class DeviceUpMessageBO extends BaseUpMessageBO {
 *     private String macId;
 *     // 其他设备特有字段
 * }
 *
 * // 在消息处理器中获取类型
 * public void process(ChannelHandlerContext ctx, BaseUpMessageBO msg) {
 *     int messageType = msg.getMessageType();
 *     if (messageType == DeviceUpMessageTypeEnum.CONN.getCode()) {
 *         DeviceUpMessageBO deviceMsg = (DeviceUpMessageBO) msg;
 *         String macId = deviceMsg.getMacId();
 *         // 处理连接认证
 *     }
 * }
 * </pre>
 *
 * @author 0101
 * @see com.sandbox.services.living.enumeration.websocket.DeviceUpMessageTypeEnum
 * @see com.sandbox.services.living.model.bo.websocket.device.DeviceUpMessageBO
 * @see com.sandbox.services.living.netty.websocket.handler.BaseBusinessHandler
 * @since 2026-03-16
 */
@Data
public class BaseUpMessageBO {

    /**
     * 上行消息类型
     *
     * <p>用于标识消息的具体业务含义，决定了由哪个处理器处理此消息。
     * 子类应配合对应的类型枚举使用，如设备上行消息使用 {@link DeviceUpMessageTypeEnum}。
     *
     * <p>取值说明（以设备上行消息为例）：
     * <ul>
     *   <li>101 - 连接认证请求（{@link DeviceUpMessageTypeEnum#CONN}）</li>
     *   <li>102 - 断开连接通知（{@link DeviceUpMessageTypeEnum#DIS_CONN}）</li>
     *   <li>201 - 心跳请求（{@link DeviceUpMessageTypeEnum#HEARTBEAT}）</li>
     *   <li>301 - 传感器数据上报（{@link DeviceUpMessageTypeEnum#SENSOR_DATA}）</li>
     *   <li>302 - 设备状态上报（{@link DeviceUpMessageTypeEnum#DEVICE_STATUS}）</li>
     *   <li>303 - 事件上报（{@link DeviceUpMessageTypeEnum#EVENT}）</li>
     *   <li>304 - 告警上报（{@link DeviceUpMessageTypeEnum#ALARM}）</li>
     *   <li>501 - 命令执行响应（{@link DeviceUpMessageTypeEnum#COMMAND_RESPONSE}）</li>
     *   <li>502 - 配置查询响应（{@link DeviceUpMessageTypeEnum#CONFIG_RESPONSE}）</li>
     *   <li>599 - 通用响应（{@link DeviceUpMessageTypeEnum#GENERAL_RESPONSE}）</li>
     * </ul>
     *
     * <p>使用场景：
     * <ul>
     *   <li>{@link com.sandbox.services.living.netty.websocket.handler.BaseBusinessHandler}
     *       根据此字段进行消息路由</li>
     *   <li>平台根据此字段选择合适的业务处理器</li>
     *   <li>日志记录时可通过此字段输出消息类型描述</li>
     * </ul>
     */
    private int messageType;
}