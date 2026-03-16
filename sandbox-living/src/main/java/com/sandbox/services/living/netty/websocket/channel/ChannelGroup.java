package com.sandbox.services.living.netty.websocket.channel;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 保存用户连接信息到本地
 * @author: 0101
 * @create: 2026/3/16
 */
public class ChannelGroup {

    /**
     * 外层 Key: 对于用户uid，对于设备是macId, 内层 Key: 对于用户是使用的设备客户端id，对于设备是写死的device字符串, Value: Channel通道
     */
    private static final Map<String, Map<String, Channel>> CHANNEL_GROUP = new ConcurrentHashMap<>();

    /**
     * 添加通道
     *
     * @param key       对于用户uid，对于设备是macId
     * @param clientKey 对于用户是使用的设备客户端id，对于设备是写死的device字符串
     * @param channel   Channel通道
     */
    public static void addChannel(String key, String clientKey, Channel channel) {
        CHANNEL_GROUP.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(clientKey, channel);
    }

    /**
     * 获取通道
     *
     * @param key       对于用户uid，对于设备是macId
     * @param clientKey 对于用户是使用的设备客户端id，对于设备是写死的device字符串
     * @return Channel通道
     */
    public static Channel getChannel(String key, String clientKey) {
        Map<String, Channel> clientChannels = CHANNEL_GROUP.get(key);
        return clientChannels != null ? clientChannels.get(clientKey) : null;
    }

    /**
     * 获取用户设备所有通道
     *
     * @param key 对于用户uid，对于设备是macId
     * @return 所有通道
     */
    public static Map<String, Channel> getChannel(String key) {
        return CHANNEL_GROUP.get(key);
    }

    /**
     * 移除通道
     *
     * @param key       对于用户uid，对于设备是macId
     * @param clientKey 对于用户是使用的设备客户端id，对于设备是写死的device字符串
     */
    public static void removeChannel(String key, String clientKey) {
        Map<String, Channel> clientChannels = CHANNEL_GROUP.get(key);
        if (clientChannels != null) {
            clientChannels.remove(clientKey);
            // 如果该用户无其他连接，移除外层键
            if (clientChannels.isEmpty()) {
                CHANNEL_GROUP.remove(key);
            }
        }
    }
}
