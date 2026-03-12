package com.sandbox.services.device.websocket.channel;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 保存用户连接信息到本地
 * @author: xp
 * @create: 2025/5/1
 */
public class ChannelGroup {

    /**
     * 外层 Key: 设备号, Value: Channel
     */
    private static final Map<String, Channel> DEVICE_CHANNEL_GROUP = new ConcurrentHashMap<>();

    public static void addChannel(String uid, Channel channel) {
        DEVICE_CHANNEL_GROUP.computeIfAbsent(uid, k -> channel);
    }

    public static Channel getChannel(String deviceId) {
        return DEVICE_CHANNEL_GROUP.get(deviceId);
    }

    public static void removeChannel(String deviceId) {
        DEVICE_CHANNEL_GROUP.remove(deviceId);
    }
}
