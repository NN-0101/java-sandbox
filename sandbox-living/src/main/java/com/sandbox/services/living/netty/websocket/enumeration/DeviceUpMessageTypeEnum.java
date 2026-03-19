package com.sandbox.services.living.netty.websocket.enumeration;

import com.sandbox.services.living.netty.websocket.model.device.DeviceUpMessageBO;
import lombok.Getter;

/**
 * 设备上行消息类型枚举 - 定义设备主动上报给平台的各种消息类型
 *
 * <p>该枚举定义了设备端主动向平台发送的所有消息类型，包括连接认证、心跳保活、
 * 数据上报、事件告警等。这些消息由设备发起，平台负责接收和处理。</p>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>协议规范</b>：作为设备端与平台之间的上行通信协议标准</li>
 *   <li><b>消息路由</b>：平台根据消息类型将消息分发到对应的业务处理器</li>
 *   <li><b>类型安全</b>：提供类型安全的枚举值，替代魔法数字</li>
 * </ul>
 *
 * <p><b>消息分类：</b>
 * <ul>
 *   <li><b>连接管理类 (1xx)</b>：连接建立、认证请求</li>
 *   <li><b>心跳保活类 (2xx)</b>：心跳请求</li>
 *   <li><b>数据上报类 (3xx)</b>：各类业务数据、事件、告警上报</li>
 *   <li><b>响应反馈类 (5xx)</b>：对平台命令的响应结果</li>
 * </ul>
 *
 * @author 0101
 * @see DeviceUpMessageBO
 * @since 2026-03-19
 */
@Getter
public enum DeviceUpMessageTypeEnum {

    // ========== 连接管理类 (1xx) ==========

    /**
     * 连接认证请求 (101)
     *
     * <p>设备首次连接时发送，用于建立连接并进行身份认证。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>macId：设备MAC地址（必填）</li>
     *   <li>token：设备认证令牌（可选，根据认证方式）</li>
     *   <li>signature：签名信息（可选）</li>
     *   <li>timestamp：时间戳（可选）</li>
     * </ul>
     * </p>
     *
     * <p><b>处理流程：</b>
     * <ol>
     *   <li>设备建立WebSocket连接后立即发送CONN消息</li>
     *   <li>平台验证设备身份合法性</li>
     *   <li>验证通过后将设备标识绑定到Channel</li>
     *   <li>返回认证结果（通过下行消息）</li>
     * </ol>
     * </p>
     */
    CONN(101, "连接认证请求"),

    /**
     * 断开连接通知 (102)
     *
     * <p>设备主动断开连接前发送的通知消息。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>reason：断开原因（如：powerOff, reboot, upgrade等）</li>
     * </ul>
     * </p>
     */
    DIS_CONN(102, "断开连接通知"),


    // ========== 心跳保活类 (2xx) ==========

    /**
     * 心跳请求 (201)
     *
     * <p>设备定期发送，维持连接活性，检测网络状态。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>timestamp：当前设备时间戳（可选）</li>
     * </ul>
     * </p>
     *
     * <p><b>超时机制：</b>
     * <ul>
     *   <li>平台配置读超时时间（如60秒）</li>
     *   <li>超过超时时间未收到任何消息（包括心跳），自动断开连接</li>
     * </ul>
     * </p>
     */
    HEARTBEAT(201, "心跳请求"),


    // ========== 数据上报类 (3xx) ==========

    /**
     * 传感器数据上报 (301)
     *
     * <p>设备采集的各类传感器数据，如温度、湿度、光照、气压等。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>timestamp：数据采集时间戳</li>
     *   <li>data：具体传感器数据，可能包含多个字段</li>
     * </ul>
     * </p>
     *
     * <p><b>数据示例：</b>
     * <pre>
     * {
     *     "timestamp": 1742313600000,
     *     "data": {
     *         "temperature": 25.6,
     *         "humidity": 60.5,
     *         "illumination": 320
     *     }
     * }
     * </pre>
     * </p>
     */
    SENSOR_DATA(301, "传感器数据上报"),

    /**
     * 设备状态上报 (302)
     *
     * <p>设备自身运行状态信息，包括工作模式、运行时长、资源使用情况等。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>status：运行状态（running/standby/fault）</li>
     *   <li>uptime：已运行时间（秒）</li>
     *   <li>cpuUsage：CPU使用率（%）</li>
     *   <li>memoryUsage：内存使用率（%）</li>
     *   <li>battery：电量（%）</li>
     * </ul>
     * </p>
     */
    DEVICE_STATUS(302, "设备状态上报"),

    /**
     * 事件上报 (303)
     *
     * <p>设备检测到的事件信息，通常需要及时处理。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>eventType：事件类型（如：door_open, motion_detected等）</li>
     *   <li>timestamp：事件发生时间戳</li>
     *   <li>data：事件相关数据</li>
     * </ul>
     * </p>
     */
    EVENT(303, "事件上报"),

    /**
     * 告警上报 (304)
     *
     * <p>设备检测到的异常或故障信息，需要立即关注和处理。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>alarmCode：告警码</li>
     *   <li>level：告警级别（1-紧急，2-重要，3-一般）</li>
     *   <li>message：告警内容</li>
     *   <li>timestamp：告警发生时间</li>
     *   <li>data：附加数据</li>
     * </ul>
     * </p>
     */
    ALARM(304, "告警上报"),


    // ========== 响应反馈类 (5xx) ==========

    /**
     * 命令执行响应 (501)
     *
     * <p>对平台下发命令的执行结果反馈。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>commandId：对应命令的唯一ID</li>
     *   <li>code：执行结果码（200-成功，其他-失败）</li>
     *   <li>message：结果描述</li>
     *   <li>data：返回数据（可选）</li>
     * </ul>
     * </p>
     */
    COMMAND_RESPONSE(501, "命令执行响应"),

    /**
     * 配置查询响应 (502)
     *
     * <p>对平台配置查询命令的响应，返回设备当前配置。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>config：配置信息JSON对象</li>
     * </ul>
     * </p>
     */
    CONFIG_RESPONSE(502, "配置查询响应"),

    /**
     * 通用响应 (599)
     *
     * <p>对其他上行消息的通用确认响应。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>code：状态码（200-成功，其他-失败）</li>
     *   <li>message：状态描述</li>
     *   <li>data：附加数据（可选）</li>
     * </ul>
     * </p>
     */
    GENERAL_RESPONSE(599, "通用响应");


    /**
     * 消息类型值
     *
     * <p>用于消息传输和路由的整型值，必须保证唯一性。
     * 取值范围划分：
     * <ul>
     *   <li>101-199：连接管理类</li>
     *   <li>201-299：心跳保活类</li>
     *   <li>301-399：数据上报类</li>
     *   <li>501-599：响应反馈类</li>
     * </ul>
     * </p>
     */
    private final int code;

    /**
     * 类型描述
     *
     * <p>对人类友好的描述信息，用于日志打印、监控展示等场景。</p>
     */
    private final String description;

    /**
     * 枚举构造函数
     *
     * @param code       消息类型值
     * @param description 类型描述
     */
    DeviceUpMessageTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据类型值获取枚举
     *
     * @param code 类型值
     * @return 对应的枚举，如果未找到返回null
     */
    public static DeviceUpMessageTypeEnum fromValue(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeviceUpMessageTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据类型值获取描述
     *
     * @param code 类型值
     * @return 类型描述，如果未找到返回"未知上行消息类型"
     */
    public static String getDescriptionByValue(Integer code) {
        DeviceUpMessageTypeEnum type = fromValue(code);
        return type != null ? type.description : "未知上行消息类型(" + code + ")";
    }

    /**
     * 判断是否为有效的上行消息类型
     *
     * @param code 类型值
     * @return true-有效，false-无效
     */
    public static boolean isValid(Integer code) {
        return fromValue(code) != null;
    }

    /**
     * 判断是否为连接类消息
     */
    public boolean isConnectionType() {
        return code >= 100 && code < 200;
    }

    /**
     * 判断是否为心跳类消息
     */
    public boolean isHeartbeatType() {
        return code >= 200 && code < 300;
    }

    /**
     * 判断是否为数据上报类消息
     */
    public boolean isReportType() {
        return code >= 300 && code < 400;
    }

    /**
     * 判断是否为响应类消息
     */
    public boolean isResponseType() {
        return code >= 500 && code < 600;
    }
}