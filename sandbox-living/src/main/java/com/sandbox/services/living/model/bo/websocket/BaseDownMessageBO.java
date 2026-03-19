package com.sandbox.services.living.model.bo.websocket;

import com.sandbox.services.living.enumeration.websocket.PlatformDownDeviceMessageTypeEnum;
import com.sandbox.services.living.enumeration.websocket.PlatformDownMessageContentTypeEnum;
import lombok.Data;

/**
 * WebSocket 下行消息基类 - 所有平台发送给设备/客户端的消息对象的父类
 *
 * <p>该类是所有下行消息的抽象基类，定义了平台主动下发消息的基础结构。
 * 所有从平台发送到设备或客户端的消息都应继承此类，通过 {@link #messageType}
 * 标识具体的消息类型，通过 {@link #contentType} 标识内容的格式类型，
 * 实现下行消息的统一规范和多态处理。</p>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>类型标识</b>：通过 {@link #messageType} 字段（对应 {@link PlatformDownDeviceMessageTypeEnum}）
 *       标识消息的具体业务含义，设备据此执行相应操作</li>
 *   <li><b>内容格式标识</b>：通过 {@link #contentType} 字段（对应 {@link PlatformDownMessageContentTypeEnum}）
 *       标识消息内容的格式，指导设备正确解析 {@link #content} 字段</li>
 *   <li><b>时间戳</b>：通过 {@link #messageTime} 字段记录消息发送时间，便于设备端时间同步和消息时序管理</li>
 *   <li><b>协议版本管理</b>：通过 {@link #version} 字段标识消息协议版本，为未来的协议升级提供兼容性支持</li>
 *   <li><b>统一基类</b>：作为所有下行消息的父类，为消息推送框架提供统一的数据格式</li>
 * </ul>
 *
 * <p><b>设计理念：</b>
 * <ul>
 *   <li><b>类型驱动</b>：设备根据 messageType 字段判断消息类型，执行对应操作</li>
 *   <li><b>格式分离</b>：将消息类型与内容格式分离，contentType 指导 content 的解析方式，
 *       提高协议的灵活性和可扩展性</li>
 *   <li><b>版本兼容</b>：通过 version 字段支持协议升级，旧版本设备可忽略不理解的字段或按旧协议处理</li>
 *   <li><b>统一规范</b>：所有下行消息遵循相同的基础结构，便于客户端实现统一的解析逻辑</li>
 * </ul>
 *
 * <p><b>继承体系示例：</b>
 * <pre>
 * BaseDownMessageBO（基础下行消息，包含 messageType、contentType、messageTime、content、version）
 * ├── {@link com.sandbox.services.living.model.bo.websocket.device.PlatformDownDeviceMessageBO}
 * │   └── 设备下行消息，可扩展设备特有字段
 * └── UserDownMessageBO（用户下行消息，可扩展用户特有字段）
 *     └── 后续可根据需要扩展更多业务类型
 * </pre>
 *
 * <p><b>消息格式示例：</b>
 * <pre>
 * // 连接认证响应（文本类型）
 * {
 *   "messageType": 901,                    // PlatformDownDeviceMessageTypeEnum.CONN_RESPONSE
 *   "contentType": 0,                       // PlatformDownMessageContentTypeEnum.TEXT
 *   "content": "auth success",
 *   "messageTime": "2026-03-19 10:30:00",
 *   "version": 1
 * }
 *
 * // 配置更新命令（JSON类型）
 * {
 *   "messageType": 201,                    // PlatformDownDeviceMessageTypeEnum.CONFIG_UPDATE
 *   "contentType": 1,                       // PlatformDownMessageContentTypeEnum.JSON
 *   "content": {
 *     "parameters": {
 *       "reportInterval": 300,
 *       "threshold": 50
 *     }
 *   },
 *   "messageTime": "2026-03-19 10:30:00",
 *   "version": 1
 * }
 *
 * // 固件升级命令（二进制类型）
 * {
 *   "messageType": 301,                    // PlatformDownDeviceMessageTypeEnum.FIRMWARE_UPGRADE
 *   "contentType": 2,                       // PlatformDownMessageContentTypeEnum.BINARY
 *   "content": "SGVsbG8gV29ybGQ=",          // Base64编码的二进制数据
 *   "messageTime": "2026-03-19 10:30:00",
 *   "version": 1
 * }
 * </pre>
 *
 * <p><b>使用方式：</b>
 * <pre>
 * // 构造下行消息
 * BaseDownMessageBO response = new BaseDownMessageBO();
 * response.setMessageType(PlatformDownDeviceMessageTypeEnum.CONN_RESPONSE.getCode());
 * response.setContentType(PlatformDownMessageContentTypeEnum.TEXT.getCode());
 * response.setContent("auth success");
 * response.setMessageTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
 * // version 使用默认值 1，也可手动设置
 *
 * // 发送消息
 * ctx.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(response)));
 * </pre>
 *
 * @author 0101
 * @see PlatformDownDeviceMessageTypeEnum
 * @see PlatformDownMessageContentTypeEnum
 * @see com.sandbox.services.living.model.bo.websocket.device.PlatformDownDeviceMessageBO
 * @since 2026-03-19
 */
@Data
public class BaseDownMessageBO {

    /**
     * 下行消息类型
     *
     * <p>对应 {@link PlatformDownDeviceMessageTypeEnum} 中的枚举值，
     * 用于标识下行消息的具体业务含义，设备根据此字段执行相应操作。
     *
     * <p>例如：
     * <ul>
     *   <li>901 - 连接认证响应（{@link PlatformDownDeviceMessageTypeEnum#CONN_RESPONSE}）</li>
     *   <li>902 - 心跳响应（{@link PlatformDownDeviceMessageTypeEnum#HEARTBEAT_RESPONSE}）</li>
     *   <li>101 - 设备重启命令（{@link PlatformDownDeviceMessageTypeEnum#DEVICE_REBOOT}）</li>
     *   <li>201 - 配置更新命令（{@link PlatformDownDeviceMessageTypeEnum#CONFIG_UPDATE}）</li>
     *   <li>301 - 固件升级命令（{@link PlatformDownDeviceMessageTypeEnum#FIRMWARE_UPGRADE}）</li>
     *   <li>401 - 立即上报命令（{@link PlatformDownDeviceMessageTypeEnum#IMMEDIATE_REPORT}）</li>
     *   <li>501 - 时间同步命令（{@link PlatformDownDeviceMessageTypeEnum#TIME_SYNC}）</li>
     *   <li>999 - 通用响应（{@link PlatformDownDeviceMessageTypeEnum#GENERAL_RESPONSE}）</li>
     * </ul>
     */
    private Integer messageType;

    /**
     * 消息时间
     *
     * <p>消息的发送时间，格式为字符串，建议使用 "yyyy-MM-dd HH:mm:ss" 格式。
     * 可用于设备端的时间同步和消息时序管理，便于设备判断消息的新旧程度。
     */
    private String messageTime;

    /**
     * 消息内容
     *
     * <p>具体的消息数据，可以是字符串、JSON对象或其他格式。
     * 具体的数据结构由 {@link #messageType} 决定，解析方式由 {@link #contentType} 决定。
     *
     * <p>根据 {@link #contentType} 的不同：
     * <ul>
     *   <li>{@link PlatformDownMessageContentTypeEnum#TEXT} (0) - 纯文本字符串</li>
     *   <li>{@link PlatformDownMessageContentTypeEnum#JSON} (1) - JSON格式的字符串，需要解析为JSON对象</li>
     *   <li>{@link PlatformDownMessageContentTypeEnum#BINARY} (2) - Base64编码的二进制数据，需要解码后使用</li>
     * </ul>
     */
    private String content;

    /**
     * 内容类型
     *
     * <p>标识 {@link #content} 字段的数据格式，对应 {@link PlatformDownMessageContentTypeEnum} 中的枚举值：
     * <ul>
     *   <li>0 - 纯文本（{@link PlatformDownMessageContentTypeEnum#TEXT}）</li>
     *   <li>1 - JSON 对象（{@link PlatformDownMessageContentTypeEnum#JSON}）</li>
     *   <li>2 - 二进制数据（{@link PlatformDownMessageContentTypeEnum#BINARY}，Base64编码）</li>
     * </ul>
     *
     * <p>设备端收到消息后，首先检查此字段，然后根据字段值选择正确的解析方式处理 content。
     */
    private Integer contentType;

    /**
     * 协议版本号
     *
     * <p>用于标识消息所使用的协议版本，为未来的协议升级提供兼容性支持。
     * 默认值为 1，表示初始版本。当协议发生不兼容变更时，可通过递增版本号
     * 让设备端根据版本来选择不同的解析逻辑。
     *
     * <p>使用场景：
     * <ul>
     *   <li><b>协议升级</b>：新增字段或修改消息结构时，递增版本号</li>
     *   <li><b>兼容性处理</b>：设备可根据版本号决定是否支持新特性</li>
     *   <li><b>灰度发布</b>：可同时支持多个版本的消息格式</li>
     * </ul>
     */
    private int version = 1;
}
