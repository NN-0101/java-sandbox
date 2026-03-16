package com.sandbox.services.living.model.bo.websocket;

import lombok.Data;

/**
 * @description: 客户端消息发送
 * @author: 0101
 * @create: 2026/3/16
 */
@Data
public class DeviceMessageBO {

    private int type;

    private String macId;
}
