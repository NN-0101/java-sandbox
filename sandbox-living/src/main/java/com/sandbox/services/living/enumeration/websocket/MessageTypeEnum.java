package com.sandbox.services.living.enumeration.websocket;

import lombok.Getter;

/**
 * WebSocket消息类型枚举 - 定义系统中所有消息类型的标准值
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>类型定义</b>：统一管理所有WebSocket消息的类型值，避免硬编码</li>
 *   <li><b>协议规范</b>：作为客户端与服务端之间的通信协议标准</li>
 *   <li><b>类型安全</b>：提供类型安全的枚举值，替代魔法数字</li>
 *   <li><b>文档化</b>：通过枚举描述字段，清晰地说明每种消息类型的含义</li>
 * </ul>
 *
 * <p>设计理念：</p>
 * <ul>
 *   <li><b>集中管理</b>：所有消息类型集中定义，便于维护和修改</li>
 *   <li><b>双向使用</b>：同时服务于上行消息({@link com.sandbox.services.living.model.bo.websocket.device.DeviceMessageBO})和下行消息({@link com.sandbox.services.living.model.bo.websocket.device.PushDeviceMessageBO})</li>
 *   <li><b>可扩展性</b>：新增消息类型只需添加枚举项，无需修改业务代码</li>
 *   <li><b>自描述性</b>：每个枚举项都包含明确的描述信息，提高代码可读性</li>
 * </ul>
 *
 * <p>消息分类：</p>
 * <ul>
 *   <li><b>连接管理类 (1xx)</b>：连接建立、认证、断开等</li>
 *   <li><b>心跳保活类 (2xx)</b>：心跳请求、心跳响应</li>
 *   <li><b>数据上报类 (3xx)</b>：设备数据、事件上报</li>
 *   <li><b>命令控制类 (4xx)</b>：指令下发、配置更新</li>
 *   <li><b>响应反馈类 (5xx)</b>：操作结果、错误反馈</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li><b>消息解析</b>：{@link com.sandbox.services.living.netty.websocket.handler.device.DeviceFrameHandler}解析消息时，type字段对应枚举值</li>
 *   <li><b>消息路由</b>：{@link com.sandbox.services.living.netty.websocket.handler.BaseBusinessHandler}根据枚举值将消息路由到对应的处理器</li>
 *   <li><b>业务处理</b>：各业务处理器根据枚举值判断消息类型，执行相应逻辑</li>
 *   <li><b>消息构造</b>：服务端推送消息时，使用枚举值填充messageType字段</li>
 * </ul>
 *
 * @author xp
 * @create 2025/3/7
 */
@Getter
public enum MessageTypeEnum {

    /**
     * 连接/认证消息 (1)
     *
     * <p>设备首次连接时发送，用于建立连接并进行身份认证。</p>
     *
     * <p>消息内容：</p>
     * <ul>
     *   <li>上行：{@link com.sandbox.services.living.model.bo.websocket.device.DeviceMessageBO} - 包含macId等认证信息</li>
     *   <li>下行：{@link com.sandbox.services.living.model.bo.websocket.device.PushDeviceMessageBO} - 认证结果</li>
     * </ul>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>设备发送CONN消息，携带设备标识和认证凭证</li>
     *   <li>服务端{@link com.sandbox.services.living.netty.websocket.handler.device.DeviceConnHandler}处理认证</li>
     *   <li>认证成功后，将设备标识绑定到Channel Attribute</li>
     *   <li>将连接添加到{@link com.sandbox.services.living.netty.websocket.channel.DeviceChannelGroup}</li>
     * </ol>
     */
    CONN(1, "连接/认证"),

    /**
     * 心跳消息 (2)
     *
     * <p>设备定期发送，维持连接活性，检测网络状态。</p>
     *
     * <p>消息内容：</p>
     * <ul>
     *   <li>上行：{@link com.sandbox.services.living.model.bo.websocket.device.DeviceMessageBO} - 心跳请求</li>
     *   <li>下行：{@link com.sandbox.services.living.model.bo.websocket.device.PushDeviceMessageBO} - 心跳响应(content="pong")</li>
     * </ul>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>设备定期发送HEARTBEAT消息</li>
     *   <li>服务端{@link com.sandbox.services.living.netty.websocket.handler.device.DeviceHeardHandler}处理心跳</li>
     *   <li>记录最后心跳时间，更新设备在线状态</li>
     *   <li>回复心跳响应，确认连接正常</li>
     * </ol>
     *
     * <p>超时机制：</p>
     * <ul>
     *   <li>{@link io.netty.handler.timeout.ReadTimeoutHandler}配置60秒超时</li>
     *   <li>超过60秒未收到任何消息（包括心跳），自动断开连接</li>
     * </ul>
     */
    HEARTBEAT(2, "心跳"),

    /**
     * 业务数据消息 (3)
     *
     * <p>设备上报采集的数据、状态信息等业务数据。</p>
     *
     * <p>消息内容：</p>
     * <ul>
     *   <li>上行：{@link com.sandbox.services.living.model.bo.websocket.device.DeviceMessageBO} - 可扩展data字段承载具体业务数据</li>
     *   <li>下行：{@link com.sandbox.services.living.model.bo.websocket.device.PushDeviceMessageBO} - 数据接收确认</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>传感器数据上报（温度、湿度、电量等）</li>
     *   <li>设备状态变更通知（在线、离线、故障等）</li>
     *   <li>事件上报（告警、异常等）</li>
     * </ul>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>设备上报DATA消息，携带业务数据</li>
     *   <li>服务端对应业务处理器解析数据</li>
     *   <li>数据存储、分析或转发</li>
     *   <li>可选返回处理结果</li>
     * </ol>
     */
    DATA(3, "业务数据"),

    /**
     * 命令下发消息 (4)
     *
     * <p>服务端主动向设备下发控制指令、配置参数等。</p>
     *
     * <p>消息内容：</p>
     * <ul>
     *   <li>下行：{@link com.sandbox.services.living.model.bo.websocket.device.PushDeviceMessageBO} - 包含具体命令内容</li>
     *   <li>上行：{@link com.sandbox.services.living.model.bo.websocket.device.DeviceMessageBO} - 命令执行结果（通常用RESPONSE类型）</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>远程控制（开关、调节参数等）</li>
     *   <li>配置更新（修改设备配置）</li>
     *   <li>固件升级（触发升级流程）</li>
     *   <li>数据同步（请求设备上报数据）</li>
     * </ul>
     *
     * <p>处理流程：</p>
     * <ol>
     *   <li>业务系统触发命令下发</li>
     *   <li>根据设备macId从DeviceChannelGroup获取Channel</li>
     *   <li>构造COMMAND消息推送给设备</li>
     *   <li>设备执行命令并返回执行结果</li>
     * </ol>
     */
    COMMAND(4, "命令下发"),

    /**
     * 响应消息 (5)
     *
     * <p>对上行消息的确认响应，或命令执行结果的反馈。</p>
     *
     * <p>消息内容：</p>
     * <ul>
     *   <li>上行：对服务端命令的响应</li>
     *   <li>下行：对设备上行消息的确认</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>命令执行结果返回</li>
     *   <li>数据上报确认</li>
     *   <li>认证结果返回</li>
     *   <li>错误信息反馈</li>
     * </ul>
     *
     * <p>响应格式建议：</p>
     * <pre>
     * {
     *     "messageType": 5,
     *     "content": {
     *         "code": 200,           // 状态码
     *         "message": "success",  // 状态描述
     *         "data": {...}          // 响应数据
     *     }
     * }
     * </pre>
     */
    RESPONSE(5, "响应");

    /**
     * 消息类型值
     *
     * <p>用于消息传输和路由的整型值，必须保证唯一性。</p>
     * <ul>
     *   <li>1-99：连接管理类</li>
     *   <li>100-199：心跳保活类</li>
     *   <li>200-299：数据上报类</li>
     *   <li>300-399：命令控制类</li>
     *   <li>400-499：响应反馈类</li>
     * </ul>
     */
    private final Integer value;

    /**
     * 类型描述
     *
     * <p>对人类友好的描述信息，用于日志打印、监控展示等场景。</p>
     */
    private final String description;

    /**
     * 枚举构造函数
     *
     * @param value       消息类型值
     * @param description 类型描述
     */
    MessageTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据类型值获取枚举
     *
     * <p>用于从消息的type字段解析对应的枚举类型。</p>
     *
     * @param value 类型值
     * @return 对应的枚举，如果未找到返回null
     */
    public static MessageTypeEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (MessageTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据类型值获取描述
     *
     * <p>用于日志打印，避免魔法数字。</p>
     *
     * @param value 类型值
     * @return 类型描述，如果未找到返回"未知类型"
     */
    public static String getDescriptionByValue(Integer value) {
        MessageTypeEnum type = fromValue(value);
        return type != null ? type.description : "未知类型(" + value + ")";
    }

    /**
     * 判断是否为有效的消息类型
     *
     * @param value 类型值
     * @return true-有效，false-无效
     */
    public static boolean isValid(Integer value) {
        return fromValue(value) != null;
    }

    /**
     * 判断是否为连接类消息
     */
    public boolean isConnectionType() {
        return this == CONN;
    }

    /**
     * 判断是否为心跳类消息
     */
    public boolean isHeartbeatType() {
        return this == HEARTBEAT;
    }

    /**
     * 判断是否为业务数据类消息
     */
    public boolean isDataType() {
        return this == DATA;
    }

    /**
     * 判断是否为命令类消息
     */
    public boolean isCommandType() {
        return this == COMMAND;
    }

    /**
     * 判断是否为响应类消息
     */
    public boolean isResponseType() {
        return this == RESPONSE;
    }
}