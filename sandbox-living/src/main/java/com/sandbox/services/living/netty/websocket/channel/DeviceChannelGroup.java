package com.sandbox.services.living.netty.websocket.channel;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备连接通道组 - 本地连接管理器
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li><b>连接存储</b>：以设备MAC地址为键，存储对应的Netty Channel连接</li>
 *   <li><b>连接管理</b>：提供添加、获取、移除连接的线程安全操作</li>
 *   <li><b>本地缓存</b>：作为设备连接的本地注册表，供同一JVM内的组件使用</li>
 * </ul>
 *
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>设备认证成功后，将连接存入此组</li>
 *   <li>向设备推送消息时，根据MAC地址获取Channel</li>
 *   <li>设备断开连接时，从组中移除</li>
 * </ul>
 *
 * <p>注意事项：</p>
 * <ul>
 *   <li>仅在单节点部署时有效，分布式环境下需使用Redis等分布式存储</li>
 *   <li>连接断开时必须调用removeChannel，防止内存泄漏</li>
 * </ul>
 *
 * @author 0101
 * @create 2026/3/16
 */
public class DeviceChannelGroup {

    /**
     * 设备连接存储映射
     * Key: 设备MAC地址 (macId)
     * Value: 设备对应的Netty Channel连接
     */
    private static final Map<String, Channel> DEVICE_CHANNEL_GROUP = new ConcurrentHashMap<>();

    /**
     * 添加设备通道
     *
     * <p>使用computeIfAbsent确保原子性操作，避免重复添加。
     * 如果MAC地址已存在，不会覆盖原有连接，防止已认证设备被意外顶替。</p>
     *
     * @param macId   设备唯一标识（MAC地址）
     * @param channel 设备对应的Netty Channel
     */
    public static void addChannel(String macId, Channel channel) {
        DEVICE_CHANNEL_GROUP.computeIfAbsent(macId, k -> channel);
    }

    /**
     * 获取设备通道
     *
     * @param macId 设备唯一标识（MAC地址）
     * @return 设备对应的Netty Channel，若不存在则返回null
     */
    public static Channel getChannel(String macId) {
        return DEVICE_CHANNEL_GROUP.get(macId);
    }

    /**
     * 移除设备通道
     *
     * <p>在设备断开连接、认证失效或主动踢出设备时调用。
     * 必须与addChannel成对使用，确保资源正确释放。</p>
     *
     * @param macId 设备唯一标识（MAC地址）
     */
    public static void removeChannel(String macId) {
        DEVICE_CHANNEL_GROUP.remove(macId);
    }
}