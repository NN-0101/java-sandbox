package com.sandbox.services.living.model.bo.websocket;

import com.sandbox.services.living.enumeration.websocket.DeviceUpMessageTypeEnum;
import lombok.Data;

/**
 * WebSocket 上行消息基类 - 所有设备/客户端发送给平台的消息对象的父类
 *
 * <p>该类是所有上行消息的抽象基类，定义了上行消息的基础结构。
 * 所有从设备或客户端发送到平台的消息都应继承此类，并通过 {@link #messageType} 字段
 * 标识具体的消息类型，通过 {@link #version} 字段标识协议版本，
 * 实现消息的多态处理、路由分发和协议兼容性管理。</p>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>类型标识</b>：提供消息类型字段 {@link #messageType}，用于消息路由和分发，
 *       子类可根据具体业务定义对应的类型枚举（如 {@link DeviceUpMessageTypeEnum}）</li>
 *   <li><b>协议版本管理</b>：通过 {@link #version} 字段标识消息协议版本，为未来的协议升级
 *       提供兼容性支持，平台可根据版本号选择不同的处理逻辑</li>
 *   <li><b>统一基类</b>：作为所有上行消息对象的父类，实现多态处理，管道中可以传递
 *       基类引用，实际类型由子类决定</li>
 *   <li><b>协议定义</b>：定义消息的基础结构，确保所有上行消息都包含类型信息和版本信息，
 *       为消息处理框架提供统一入口</li>
 * </ul>
 *
 * <p><b>设计理念：</b>
 * <ul>
 *   <li><b>类型驱动</b>：通过 {@code messageType} 字段实现消息的路由，每个处理器根据 {@code messageType} 决定是否处理，
 *       形成清晰的责任链模式</li>
 *   <li><b>版本兼容</b>：通过 {@code version} 字段支持协议升级，平台可根据版本号决定使用哪种解析逻辑，
 *       同时支持多个版本的设备</li>
 *   <li><b>开闭原则</b>：新增消息类型时，只需扩展子类并添加对应的处理器，无需修改现有代码</li>
 *   <li><b>多态支持</b>：管道中可以传递 BaseUpMessageBO 引用，实际类型由子类决定，
 *       处理器可通过 instanceof 或类型字段进行判断</li>
 * </ul>
 *
 * <p><b>继承体系示例：</b>
 * <pre>
 * BaseUpMessageBO（基础上行消息，包含 messageType、version）
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
 *     int version = msg.getVersion();
 *
 *     // 根据版本号选择不同的处理逻辑
 *     if (version == 1) {
 *         // 处理 v1 版本的消息
 *     } else if (version == 2) {
 *         // 处理 v2 版本的消息
 *     }
 *
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

    /**
     * 协议版本号
     *
     * <p>用于标识消息所使用的协议版本，为未来的协议升级提供兼容性支持。
     * 默认值为 1，表示初始版本。当协议发生不兼容变更时，可通过递增版本号
     * 让平台根据版本来选择不同的解析和处理逻辑。
     *
     * <p>使用场景：
     * <ul>
     *   <li><b>协议升级</b>：新增字段或修改消息结构时，递增版本号</li>
     *   <li><b>兼容性处理</b>：平台可根据版本号决定使用哪种解析逻辑，
     *       同时支持多个版本的设备</li>
     *   <li><b>灰度发布</b>：可同时支持多个版本的消息格式，逐步迁移设备</li>
     * </ul>
     */
    private int version = 1;
}
