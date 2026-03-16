package com.sandbox.services.living.model.bo.websocket.device;

import lombok.Data;

/**
 * 服务端推送消息对象 - 服务器→设备
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>消息推送</b>：承载服务器主动推送给设备的WebSocket消息数据</li>
 *   <li><b>指令下发</b>：用于向设备发送控制指令、配置更新等</li>
 *   <li><b>响应返回</b>：作为设备上行消息的响应</li>
 * </ul>
 *
 * @author xp
 * @create 2025/5/1
 */
@Data
public class PushDeviceMessageBO {

    private Integer messageType;

    private String messageTime;

    private String content;

    private Integer contentType;
}