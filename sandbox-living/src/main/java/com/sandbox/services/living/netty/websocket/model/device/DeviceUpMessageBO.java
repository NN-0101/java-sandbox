package com.sandbox.services.living.netty.websocket.model.device;

import com.sandbox.services.living.netty.websocket.enumeration.DeviceUpMessageTypeEnum;
import com.sandbox.services.living.netty.websocket.model.BaseUpMessageBO;
import com.sandbox.services.living.netty.websocket.channel.group.DeviceChannelGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备上行消息对象 - 设备→平台
 *
 * <p>该对象是设备主动发送给平台的所有上行消息的统一数据载体，继承自 {@link BaseUpMessageBO}，
 * 承载设备的各种请求和数据。通过 type 字段区分不同的上行消息类型
 * （对应 {@link DeviceUpMessageTypeEnum}），平台根据该字段将消息路由到对应的业务处理器进行处理。</p>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>消息载体</b>：承载设备发送给平台的 WebSocket 消息数据，是所有设备上行消息的
 *       统一数据格式</li>
 *   <li><b>设备标识</b>：包含设备唯一标识 {@link #macId}，用于设备认证、消息路由和
 *       后续的平台下发消息推送</li>
 *   <li><b>协议定义</b>：定义设备上行消息的标准数据结构，确保设备与服务端之间的通信协议一致</li>
 *   <li><b>类型路由</b>：通过 type 字段（对应 {@link DeviceUpMessageTypeEnum}）
 *       实现消息的分类和路由，平台据此将消息分发到不同的业务处理器</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>连接管理类</b>：设备首次连接时发送 CONN 类型消息（{@link DeviceUpMessageTypeEnum#CONN}），
 *       断开时发送 DIS_CONN 通知（{@link DeviceUpMessageTypeEnum#DIS_CONN}）</li>
 *   <li><b>心跳保活类</b>：设备定期发送 HEARTBEAT 类型消息（{@link DeviceUpMessageTypeEnum#HEARTBEAT}）</li>
 *   <li><b>数据上报类</b>：设备上报传感器数据、设备状态、事件、告警等
 *       （{@link DeviceUpMessageTypeEnum#SENSOR_DATA}、{@link DeviceUpMessageTypeEnum#DEVICE_STATUS}、
 *        {@link DeviceUpMessageTypeEnum#EVENT}、{@link DeviceUpMessageTypeEnum#ALARM}）</li>
 *   <li><b>响应反馈类</b>：设备对平台下发命令的执行结果响应
 *       （{@link DeviceUpMessageTypeEnum#COMMAND_RESPONSE}、{@link DeviceUpMessageTypeEnum#CONFIG_RESPONSE}、
 *        {@link DeviceUpMessageTypeEnum#GENERAL_RESPONSE}）</li>
 * </ul>
 *
 * <p><b>消息格式示例：</b>
 * <pre>
 * // 连接认证消息
 * {
 *   "type": 101,                          // DeviceUpMessageTypeEnum.CONN.getCode()
 *   "macId": "AA:BB:CC:DD:EE:FF",
 *   "timestamp": 1742313600000,
 *   "data": {                              // 扩展字段，可包含认证令牌、签名等
 *     "token": "device-auth-token",
 *     "signature": "a1b2c3..."
 *   }
 * }
 *
 * // 心跳请求消息
 * {
 *   "type": 201,                          // DeviceUpMessageTypeEnum.HEARTBEAT.getCode()
 *   "macId": "AA:BB:CC:DD:EE:FF",
 *   "timestamp": 1742313600000
 * }
 *
 * // 传感器数据上报
 * {
 *   "type": 301,                          // DeviceUpMessageTypeEnum.SENSOR_DATA.getCode()
 *   "macId": "AA:BB:CC:DD:EE:FF",
 *   "timestamp": 1742313600000,
 *   "data": {
 *     "temperature": 25.6,
 *     "humidity": 60.5
 *   }
 * }
 *
 * // 命令执行响应
 * {
 *   "type": 501,                          // DeviceUpMessageTypeEnum.COMMAND_RESPONSE.getCode()
 *   "macId": "AA:BB:CC:DD:EE:FF",
 *   "commandId": "cmd_123456",
 *   "code": 200,
 *   "message": "success",
 *   "data": {
 *     "result": "rebooting"
 *   }
 * }
 * </pre>
 *
 * <p><b>在管道中的处理流程：</b>
 * <ol>
 *   <li>设备发送 WebSocket 文本帧，格式为上述 JSON 结构</li>
 *   <li>{@link com.sandbox.services.living.netty.websocket.handler.device.DeviceFrameHandler}
 *       将文本帧解析为此对象</li>
 *   <li>解析后的对象通过管道传递给下游的 {@link com.sandbox.services.living.netty.websocket.handler.BaseBusinessHandler}
 *       子类处理器</li>
 *   <li>各处理器根据 type 字段（即 {@link DeviceUpMessageTypeEnum} 枚举值）
 *       决定是否处理该消息</li>
 *   <li>处理完成后，可通过 {@link PlatformDownDeviceMessageBO}
 *       构造下行响应返回给设备</li>
 * </ol>
 *
 * @author 0101
 * @see BaseUpMessageBO
 * @see DeviceUpMessageTypeEnum
 * @see com.sandbox.services.living.netty.websocket.handler.device.DeviceFrameHandler
 * @see com.sandbox.services.living.netty.websocket.handler.BaseBusinessHandler
 * @see PlatformDownDeviceMessageBO
 * @since 2026-03-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceUpMessageBO extends BaseUpMessageBO {

    /**
     * 设备唯一标识（MAC地址）
     *
     * <p>用于标识设备的唯一身份，在连接认证时验证设备合法性，
     * 在后续消息中用于路由和平台下发消息时的连接查找。
     *
     * <p>格式通常为：AA:BB:CC:DD:EE:FF（冒号分隔的十六进制表示）</p>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li><b>连接认证</b>：验证设备是否在白名单中，是否有权限接入</li>
     *   <li><b>消息路由</b>：确定消息属于哪个设备，用于数据存储和业务处理</li>
     *   <li><b>消息推送</b>：平台下发消息时，根据 macId 从 {@link DeviceChannelGroup}
     *       获取对应的 Channel 进行推送</li>
     *   <li><b>资源清理</b>：设备断开连接时，根据 macId 清理相关资源</li>
     * </ul>
     */
    private String macId;

}