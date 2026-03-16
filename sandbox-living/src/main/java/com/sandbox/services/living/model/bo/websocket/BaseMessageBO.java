package com.sandbox.services.living.model.bo.websocket;

import lombok.Data;

/**
 * WebSocket消息基类 - 所有消息对象的父类
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>类型标识</b>：提供消息类型字段{@link #type}，用于消息路由和分发</li>
 *   <li><b>统一基类</b>：作为所有WebSocket消息对象的父类，实现多态处理</li>
 *   <li><b>协议定义</b>：定义消息的基础结构，确保所有消息都包含类型信息</li>
 * </ul>
 *
 * <p>设计理念：</p>
 * <ul>
 *   <li><b>类型驱动</b>：通过type字段实现消息的路由，每个处理器根据type决定是否处理</li>
 *   <li><b>开闭原则</b>：新增消息类型时，只需扩展子类，无需修改现有代码</li>
 *   <li><b>多态支持</b>：管道中可以传递BaseMessageBO，实际类型由子类决定</li>
 * </ul>
 *
 * <p>消息类型枚举示例：</p>
 * <pre>
 * public enum MessageTypeEnum {
 *     CONN(1, "连接/认证"),
 *     HEARTBEAT(2, "心跳"),
 *     DATA(3, "业务数据"),
 *     COMMAND(4, "命令下发"),
 *     RESPONSE(5, "响应");
 *
 *     private int value;
 *     private String desc;
 * }
 * </pre>
 *
 * <p>继承体系：</p>
 * <ul>
 *   <li>BaseMessageBO - 基础消息，包含type字段</li>
 *   <li>├── DeviceMessageBO - 设备发送的消息，包含macId</li>
 *   <li>├── UserMessageBO - 用户发送的消息，包含userId/token等</li>
 *   <li>└── 其他业务消息类型...</li>
 * </ul>
 *
 * @author 0101
 * @create 2026/3/16
 */
@Data
public class BaseMessageBO {

    /**
     * 消息类型
     *
     * <p>用于标识消息的具体业务含义，决定了由哪个处理器处理此消息。</p>
     *
     * <p>取值说明：</p>
     * <ul>
     *   <li>1 - 连接/认证消息 (CONN)</li>
     *   <li>2 - 心跳消息 (HEARTBEAT)</li>
     *   <li>3 - 业务数据消息 (DATA)</li>
     *   <li>4 - 命令响应消息 (RESPONSE)</li>
     *   <li>其他值由具体业务定义</li>
     * </ul>
     *
     * <p>使用场景：</p>
     * <ul>
     *   <li>{@link com.sandbox.services.living.netty.websocket.handler.BaseBusinessHandler} 根据此字段进行消息路由</li>
     *   <li>设备根据此字段判断消息类型，执行不同业务逻辑</li>
     *   <li>服务端根据此字段选择合适的处理器</li>
     * </ul>
     */
    private int type;
}