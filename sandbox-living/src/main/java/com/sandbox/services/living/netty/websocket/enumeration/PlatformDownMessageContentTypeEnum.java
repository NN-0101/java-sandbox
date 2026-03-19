package com.sandbox.services.living.netty.websocket.enumeration;

import com.sandbox.services.living.netty.websocket.model.BaseDownMessageBO;
import com.sandbox.services.living.netty.websocket.model.device.PlatformDownDeviceMessageBO;
import lombok.Getter;

/**
 * 平台下行消息内容类型枚举
 *
 * <p>该枚举定义了平台下行消息中 {@code content} 字段的数据格式类型，
 * 用于标识消息内容的具体格式，便于设备端正确解析和处理消息内容。</p>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>格式标识</b>：标识下行消息内容的格式类型，如纯文本、JSON、二进制等</li>
 *   <li><b>解析指导</b>：设备根据此枚举值选择正确的解析方式处理消息内容</li>
 *   <li><b>协议规范</b>：统一管理内容类型值，确保设备端与服务端的解析一致性</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>文本消息</b>：简单的响应信息、状态描述等（如心跳响应、操作结果）</li>
 *   <li><b>JSON消息</b>：结构化数据，如配置信息、命令参数、复杂响应等</li>
 *   <li><b>二进制消息</b>：固件升级包、文件传输、加密数据等</li>
 * </ul>
 *
 * <p><b>在消息对象中的位置：</b>
 * <pre>
 * {
 *   "messageType": 901,                    // 消息类型
 *   "contentType": 1,                       // 内容类型（当前枚举值）
 *   "content": { ... }                       // 根据内容类型解析的内容
 * }
 * </pre>
 *
 * @author 0101
 * @see BaseDownMessageBO
 * @see PlatformDownDeviceMessageBO
 * @since 2026-03-19
 */
@Getter
public enum PlatformDownMessageContentTypeEnum {

    /**
     * 纯文本类型 (0)
     *
     * <p>消息内容为普通文本字符串，适用于简单的响应信息、状态描述等。
     * 设备端可直接将 content 字段作为字符串处理，无需额外解析。</p>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>连接认证结果： "auth success"</li>
     *   <li>心跳响应： "pong"</li>
     *   <li>操作结果： "success" / "failure"</li>
     * </ul>
     * </p>
     */
    TEXT(0, "纯文本"),

    /**
     * JSON 对象类型 (1)
     *
     * <p>消息内容为 JSON 格式的字符串，包含结构化的数据。
     * 设备端需要将 content 字段解析为 JSON 对象，再根据具体消息类型进行处理。</p>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>配置更新命令：包含多个配置参数的 JSON 对象</li>
     *   <li>设备控制命令：包含通道、状态、持续时间等参数的 JSON 对象</li>
     *   <li>历史数据同步：包含时间范围、数据类型等参数的 JSON 对象</li>
     *   <li>命令执行响应：包含状态码、返回数据等信息的 JSON 对象</li>
     * </ul>
     * </p>
     *
     * <p><b>示例：</b>
     * <pre>
     * {
     *   "messageType": 201,
     *   "contentType": 1,
     *   "content": {
     *     "parameters": {
     *       "reportInterval": 300,
     *       "threshold": 50
     *     }
     *   }
     * }
     * </pre>
     * </p>
     */
    JSON(1, "JSON对象"),

    /**
     * 二进制数据类型 (2)
     *
     * <p>消息内容为 Base64 编码的二进制数据，适用于传输非文本数据。
     * 设备端需要先对 content 字段进行 Base64 解码，得到原始二进制数据后再处理。</p>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>固件升级：固件文件的二进制内容</li>
     *   <li>文件传输：配置文件、日志文件等</li>
     *   <li>加密数据：经过加密的敏感信息</li>
     *   <li>图片/音视频：多媒体数据</li>
     * </ul>
     * </p>
     *
     * <p><b>示例：</b>
     * <pre>
     * {
     *   "messageType": 301,
     *   "contentType": 2,
     *   "content": "SGVsbG8gV29ybGQ="  // "Hello World" 的 Base64 编码
     * }
     * </pre>
     * </p>
     */
    BINARY(2, "二进制数据");

    /**
     * 内容类型值
     *
     * <p>用于在消息中标识 content 字段的格式类型：
     * <ul>
     *   <li>0 - 纯文本</li>
     *   <li>1 - JSON 对象</li>
     *   <li>2 - 二进制数据（Base64编码）</li>
     * </ul>
     * </p>
     */
    private final int code;

    /**
     * 类型描述
     *
     * <p>对人类友好的描述信息，用于日志打印、监控展示等场景。
     */
    private final String description;

    /**
     * 枚举构造函数
     *
     * @param code        内容类型值
     * @param description 类型描述
     */
    PlatformDownMessageContentTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据类型值获取枚举
     *
     * @param code 内容类型值
     * @return 对应的枚举，如果未找到返回 null
     */
    public static PlatformDownMessageContentTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PlatformDownMessageContentTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据类型值获取描述
     *
     * @param code 内容类型值
     * @return 类型描述，如果未找到返回"未知内容类型"
     */
    public static String getDescriptionByCode(Integer code) {
        PlatformDownMessageContentTypeEnum type = fromCode(code);
        return type != null ? type.description : "未知内容类型(" + code + ")";
    }

    /**
     * 判断是否为有效的内-容类型值
     *
     * @param code 内容类型值
     * @return true-有效，false-无效
     */
    public static boolean isValid(Integer code) {
        return fromCode(code) != null;
    }
}