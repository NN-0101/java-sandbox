package com.sandbox.services.living.netty.websocket.channel;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 保存用户连接信息到本地
 * @author: 0101
 * @create: 2026/3/16
 */
public class DeviceChannelGroup {

    /**
     * 外层 Key: macId, Value: Channel通道
     */
    private static final Map<String, Channel> DEVICE_CHANNEL_GROUP = new ConcurrentHashMap<>();

    /**
     * 添加通道
     *
     * @param macId   设备macId
     * @param channel Channel通道
     */
    public static void addChannel(String macId, Channel channel) {
        DEVICE_CHANNEL_GROUP.computeIfAbsent(macId, k -> channel);
    }

    /**
     * 获取用户设备所有通道
     *
     * @param macId 设备是macId
     * @return 所有通道
     */
    public static Channel getChannel(String macId) {
        return DEVICE_CHANNEL_GROUP.get(macId);
    }

    /**
     * 移除通道
     *
     * @param macId 设备是macId
     */
    public static void removeChannel(String macId) {
        DEVICE_CHANNEL_GROUP.remove(macId);
    }
}
