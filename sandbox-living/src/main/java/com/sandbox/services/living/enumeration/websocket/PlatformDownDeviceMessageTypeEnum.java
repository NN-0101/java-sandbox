package com.sandbox.services.living.enumeration.websocket;

import com.sandbox.services.living.model.bo.websocket.device.PlatformDownDeviceMessageBO;
import lombok.Getter;

/**
 * 平台下行设备消息类型枚举 - 定义平台主动下发给设备的各种消息类型
 *
 * <p>该枚举定义了平台端主动向设备发送的所有消息类型，包括命令下发、配置更新、
 * 数据同步请求等。这些消息由平台发起，设备负责接收和执行。</p>
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>协议规范</b>：作为平台与设备之间的下行通信协议标准</li>
 *   <li><b>命令分发</b>：设备根据消息类型执行对应的操作</li>
 *   <li><b>类型安全</b>：提供类型安全的枚举值，替代魔法数字</li>
 * </ul>
 *
 * <p><b>消息分类：</b>
 * <ul>
 *   <li><b>控制命令类 (1xx)</b>：设备控制指令（开关、重启、复位等）</li>
 *   <li><b>配置管理类 (2xx)</b>：配置更新、配置查询</li>
 *   <li><b>固件升级类 (3xx)</b>：OTA升级相关指令</li>
 *   <li><b>数据同步类 (4xx)</b>：数据上报请求、历史数据同步</li>
 *   <li><b>系统管理类 (5xx)</b>：时间同步、日志上传等</li>
 *   <li><b>通用响应类 (9xx)</b>：对设备上行消息的确认响应</li>
 * </ul>
 *
 * @author 0101
 * @see PlatformDownDeviceMessageBO
 * @since 2026-03-19
 */
@Getter
public enum PlatformDownDeviceMessageTypeEnum {

    // ========== 控制命令类 (1xx) ==========

    /**
     * 设备重启命令 (101)
     *
     * <p>向设备下发重启指令。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>delay：延迟重启时间（秒），可选，默认立即重启</li>
     *   <li>force：是否强制重启，可选</li>
     * </ul>
     * </p>
     */
    DEVICE_REBOOT(101, "设备重启命令"),

    /**
     * 设备复位命令 (102)
     *
     * <p>将设备恢复为出厂设置。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>keepNetwork：是否保留网络配置，可选</li>
     *   <li>confirmCode：确认码，防止误操作</li>
     * </ul>
     * </p>
     */
    DEVICE_RESET(102, "设备复位命令"),

    /**
     * 开关控制命令 (103)
     *
     * <p>控制设备的开关状态，适用于继电器、灯光等开关量控制设备。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>channel：通道号（多路设备）</li>
     *   <li>state：开关状态（on/off）</li>
     *   <li>duration：持续时间（秒），用于定时开关</li>
     * </ul>
     * </p>
     */
    SWITCH_CONTROL(103, "开关控制命令"),

    /**
     * 模式切换命令 (104)
     *
     * <p>切换设备工作模式。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>mode：目标模式（如：auto/manual/sleep等）</li>
     *   <li>parameters：模式参数（可选）</li>
     * </ul>
     * </p>
     */
    MODE_SWITCH(104, "模式切换命令"),


    // ========== 配置管理类 (2xx) ==========

    /**
     * 配置更新命令 (201)
     *
     * <p>更新设备的运行参数和配置信息。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>configVersion：配置版本号</li>
     *   <li>parameters：要更新的配置参数键值对</li>
     *   <li>persist：是否持久化保存</li>
     * </ul>
     * </p>
     */
    CONFIG_UPDATE(201, "配置更新命令"),

    /**
     * 配置查询命令 (202)
     *
     * <p>查询设备当前配置信息。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>fields：需要查询的字段列表，为空表示查询所有</li>
     * </ul>
     * </p>
     */
    CONFIG_QUERY(202, "配置查询命令"),


    // ========== 固件升级类 (3xx) ==========

    /**
     * 固件升级命令 (301)
     *
     * <p>触发设备进行OTA固件升级。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>version：目标版本号</li>
     *   <li>firmwareUrl：固件下载地址</li>
     *   <li>checksum：固件校验码</li>
     *   <li>fileSize：文件大小（字节）</li>
     *   <li>upgradeMode：升级模式（background/foreground）</li>
     * </ul>
     * </p>
     */
    FIRMWARE_UPGRADE(301, "固件升级命令"),

    /**
     * 升级状态查询命令 (302)
     *
     * <p>查询设备当前的升级进度和状态。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>taskId：升级任务ID</li>
     * </ul>
     * </p>
     */
    UPGRADE_STATUS_QUERY(302, "升级状态查询命令"),


    // ========== 数据同步类 (4xx) ==========

    /**
     * 立即上报命令 (401)
     *
     * <p>命令设备立即上报当前数据，不受上报间隔限制。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>dataTypes：需要上报的数据类型列表</li>
     *   <li>syncAll：是否上报所有数据</li>
     * </ul>
     * </p>
     */
    IMMEDIATE_REPORT(401, "立即上报命令"),

    /**
     * 历史数据同步命令 (402)
     *
     * <p>请求设备同步指定时间段的历史数据。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>startTime：开始时间戳</li>
     *   <li>endTime：结束时间戳</li>
     *   <li>dataTypes：数据类型列表</li>
     *   <li>maxCount：最大返回条数</li>
     * </ul>
     * </p>
     */
    HISTORY_SYNC(402, "历史数据同步命令"),


    // ========== 系统管理类 (5xx) ==========

    /**
     * 时间同步命令 (501)
     *
     * <p>向设备同步服务器时间。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>timestamp：当前时间戳</li>
     *   <li>timezone：时区</li>
     * </ul>
     * </p>
     */
    TIME_SYNC(501, "时间同步命令"),

    /**
     * 日志上传命令 (502)
     *
     * <p>命令设备上传运行日志。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>logLevel：日志级别（DEBUG/INFO/WARN/ERROR）</li>
     *   <li>startTime：开始时间</li>
     *   <li>endTime：结束时间</li>
     *   <li>maxLines：最大行数</li>
     * </ul>
     * </p>
     */
    LOG_UPLOAD(502, "日志上传命令"),


    // ========== 通用响应类 (9xx) ==========

    /**
     * 连接认证响应 (901)
     *
     * <p>对设备连接认证请求的响应。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>code：状态码（200-成功，其他-失败）</li>
     *   <li>message：状态描述</li>
     *   <li>token：认证令牌（成功后返回）</li>
     * </ul>
     * </p>
     */
    CONN_RESPONSE(901, "连接认证响应"),

    /**
     * 心跳响应 (902)
     *
     * <p>对设备心跳请求的响应。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>timestamp：服务器时间戳</li>
     * </ul>
     * </p>
     */
    HEARTBEAT_RESPONSE(902, "心跳响应"),

    /**
     * 数据上报响应 (903)
     *
     * <p>对设备数据上报的确认响应。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>code：状态码</li>
     *   <li>message：状态描述</li>
     * </ul>
     * </p>
     */
    DATA_REPORT_RESPONSE(903, "数据上报响应"),

    /**
     * 通用响应 (999)
     *
     * <p>对其他消息的通用确认响应。</p>
     *
     * <p><b>消息内容：</b>
     * <ul>
     *   <li>code：状态码</li>
     *   <li>message：状态描述</li>
     *   <li>data：附加数据</li>
     * </ul>
     * </p>
     */
    GENERAL_RESPONSE(999, "通用响应");


    /**
     * 消息类型值
     *
     * <p>用于消息传输和路由的整型值，必须保证唯一性。
     * 取值范围划分：
     * <ul>
     *   <li>101-199：控制命令类</li>
     *   <li>201-299：配置管理类</li>
     *   <li>301-399：固件升级类</li>
     *   <li>401-499：数据同步类</li>
     *   <li>501-599：系统管理类</li>
     *   <li>901-999：通用响应类</li>
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
    PlatformDownDeviceMessageTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据类型值获取枚举
     *
     * @param code 类型值
     * @return 对应的枚举，如果未找到返回null
     */
    public static PlatformDownDeviceMessageTypeEnum fromValue(Integer code) {
        if (code == null) {
            return null;
        }
        for (PlatformDownDeviceMessageTypeEnum type : values()) {
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
     * @return 类型描述，如果未找到返回"未知下行消息类型"
     */
    public static String getDescriptionByValue(Integer code) {
        PlatformDownDeviceMessageTypeEnum type = fromValue(code);
        return type != null ? type.description : "未知下行消息类型(" + code + ")";
    }

    /**
     * 判断是否为有效的下行消息类型
     *
     * @param value 类型值
     * @return true-有效，false-无效
     */
    public static boolean isValid(Integer value) {
        return fromValue(value) != null;
    }

    /**
     * 判断是否为控制命令类消息
     */
    public boolean isControlType() {
        return code >= 100 && code < 200;
    }

    /**
     * 判断是否为配置管理类消息
     */
    public boolean isConfigType() {
        return code >= 200 && code < 300;
    }

    /**
     * 判断是否为固件升级类消息
     */
    public boolean isUpgradeType() {
        return code >= 300 && code < 400;
    }

    /**
     * 判断是否为数据同步类消息
     */
    public boolean isSyncType() {
        return code >= 400 && code < 500;
    }

    /**
     * 判断是否为系统管理类消息
     */
    public boolean isSystemType() {
        return code >= 500 && code < 600;
    }

    /**
     * 判断是否为响应类消息
     */
    public boolean isResponseType() {
        return code >= 900 && code < 1000;
    }
}