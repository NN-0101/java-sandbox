package com.sandbox.services.living.model.bo.websocket.device;

import com.sandbox.services.living.enumeration.websocket.PlatformDownDeviceMessageTypeEnum;
import com.sandbox.services.living.enumeration.websocket.PlatformDownMessageContentTypeEnum;
import com.sandbox.services.living.model.bo.websocket.BaseDownMessageBO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 平台下行消息对象 - 平台→设备
 *
 * <p>该对象是平台主动下发给设备的所有下行消息的统一数据载体，继承自 {@link BaseDownMessageBO}，
 * 承载平台对设备的各种响应和指令。通过 {@link #getMessageType()} 字段区分不同的下行消息类型
 * （对应 {@link PlatformDownDeviceMessageTypeEnum}），设备根据该字段执行相应的操作。</p>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>消息推送</b>：承载平台主动推送给设备的 WebSocket 消息数据，是所有平台下行消息的
 *       统一数据格式</li>
 *   <li><b>指令下发</b>：用于向设备发送控制指令、配置更新、数据同步请求等
 *       （如 {@link PlatformDownDeviceMessageTypeEnum#DEVICE_REBOOT}、
 *       {@link PlatformDownDeviceMessageTypeEnum#CONFIG_UPDATE}、
 *       {@link PlatformDownDeviceMessageTypeEnum#IMMEDIATE_REPORT}）</li>
 *   <li><b>响应返回</b>：作为设备上行消息的确认响应，告知设备请求处理结果
 *       （如 {@link PlatformDownDeviceMessageTypeEnum#CONN_RESPONSE}、
 *       {@link PlatformDownDeviceMessageTypeEnum#HEARTBEAT_RESPONSE}、
 *       {@link PlatformDownDeviceMessageTypeEnum#DATA_REPORT_RESPONSE}）</li>
 *   <li><b>类型定义</b>：通过 {@link #getMessageType()} 字段（对应 {@link PlatformDownDeviceMessageTypeEnum}）
 *       明确消息的业务含义，设备据此执行相应逻辑</li>
 *   <li><b>内容格式定义</b>：通过 {@link #getContentType()} 字段（对应 {@link PlatformDownMessageContentTypeEnum}）
 *       明确消息内容的格式，指导设备正确解析 {@link #getContent()} 字段</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>控制命令类</b>：设备重启、复位、开关控制、模式切换等
 *       （{@link PlatformDownDeviceMessageTypeEnum#DEVICE_REBOOT}、
 *       {@link PlatformDownDeviceMessageTypeEnum#SWITCH_CONTROL}）</li>
 *   <li><b>配置管理类</b>：配置更新、配置查询
 *       （{@link PlatformDownDeviceMessageTypeEnum#CONFIG_UPDATE}、
 *       {@link PlatformDownDeviceMessageTypeEnum#CONFIG_QUERY}）</li>
 *   <li><b>固件升级类</b>：固件升级、升级状态查询
 *       （{@link PlatformDownDeviceMessageTypeEnum#FIRMWARE_UPGRADE}、
 *       {@link PlatformDownDeviceMessageTypeEnum#UPGRADE_STATUS_QUERY}）</li>
 *   <li><b>数据同步类</b>：立即上报命令、历史数据同步
 *       （{@link PlatformDownDeviceMessageTypeEnum#IMMEDIATE_REPORT}、
 *       {@link PlatformDownDeviceMessageTypeEnum#HISTORY_SYNC}）</li>
 *   <li><b>系统管理类</b>：时间同步、日志上传
 *       （{@link PlatformDownDeviceMessageTypeEnum#TIME_SYNC}、
 *       {@link PlatformDownDeviceMessageTypeEnum#LOG_UPLOAD}）</li>
 *   <li><b>通用响应类</b>：对设备上行消息的确认响应
 *       （{@link PlatformDownDeviceMessageTypeEnum#CONN_RESPONSE}、
 *       {@link PlatformDownDeviceMessageTypeEnum#HEARTBEAT_RESPONSE}、
 *       {@link PlatformDownDeviceMessageTypeEnum#GENERAL_RESPONSE}）</li>
 * </ul>
 *
 * <p><b>消息格式示例：</b>
 * <pre>
 * // 连接认证响应（文本类型）
 * {
 *   "messageType": 901,                    // PlatformDownMessageTypeEnum.CONN_RESPONSE
 *   "contentType": 0,
 *   "content": "auth success",
 *   "messageTime": "2026-03-19 10:30:00"
 * }
 *
 * // 设备重启命令（JSON类型）
 * {
 *   "messageType": 101,                    // PlatformDownMessageTypeEnum.DEVICE_REBOOT
 *   "contentType": 1,                       // PlatformDownMessageContentTypeEnum.JSON
 *   "content": {
 *     "delay": 5,
 *     "force": false
 *   },
 *   "messageTime": "2026-03-19 10:30:00"
 * }
 *
 * // 固件升级命令（二进制类型）
 * {
 *   "messageType": 301,                    // PlatformDownMessageTypeEnum.FIRMWARE_UPGRADE
 *   "contentType": 2,
 *   "content": "SGVsbG8gV29ybGQ=",          // Base64编码的二进制数据
 *   "messageTime": "2026-03-19 10:30:00"
 * }
 * </pre>
 *
 * <p><b>在业务处理器中的使用：</b>
 * <pre>
 * // 在设备上行消息处理器中构造下行响应
 * PlatformDownDeviceMessageBO response = new PlatformDownDeviceMessageBO();
 * response.setMessageType(PlatformDownMessageTypeEnum.CONN_RESPONSE.getCode());
 * response.setContentType(PlatformDownMessageContentTypeEnum.TEXT.getCode());
 * response.setContent("auth success");
 * response.setMessageTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
 *
 * ctx.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(response)));
 * </pre>
 *
 * @author xp
 * @see BaseDownMessageBO
 * @see PlatformDownDeviceMessageTypeEnum
 * @see PlatformDownMessageContentTypeEnum
 * @see com.sandbox.services.living.netty.websocket.handler.device.DeviceConnHandler
 * @see com.sandbox.services.living.netty.websocket.handler.device.DeviceHeardHandler
 * @since 2025-05-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PlatformDownDeviceMessageBO extends BaseDownMessageBO {

    // 继承自 BaseDownMessageBO 的所有字段：
    // - messageType: 下行消息类型（对应 PlatformDownMessageTypeEnum）
    // - messageTime: 消息时间
    // - content: 消息内容
    // - contentType: 内容类型（对应 PlatformDownMessageContentTypeEnum）

    // 此处可以扩展设备下行消息特有的字段，例如：
    // private String targetMacId;  // 目标设备MAC地址（广播消息时使用）
    // private Integer messageId;   // 消息ID，用于跟踪响应
    // 根据业务需要添加
}