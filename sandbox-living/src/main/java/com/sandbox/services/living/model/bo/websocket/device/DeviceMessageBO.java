package com.sandbox.services.living.model.bo.websocket.device;

import com.sandbox.services.living.model.bo.websocket.BaseMessageBO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 设备发送的消息对象 - 客户端→服务器 * <p>核心职责：</p>
 * <ul>
 * <li><b>消息载体</b>：承载设备发送给服务器的WebSocket消息数据</li>
 * <li><b>设备标识</b>：包含设备唯一标识{@link #macId}，用于设备认证和消息路由</li>
 * <li><b>协议定义</b>：定义设备上行消息的数据结构</li>
 * </ul>
 * @author: 0101
 * @create: 2026/3/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceMessageBO extends BaseMessageBO {

    private String macId;
}

