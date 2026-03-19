package com.sandbox.services.living.netty.websocket.channel.group;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备连接通道组 - 本地连接管理器
 *
 * <p>该类是 Netty WebSocket 服务中用于管理设备连接的核心组件，以设备 MAC 地址为唯一标识，
 * 维护设备与其对应 Netty {@link Channel} 的映射关系。所有操作均基于 {@link ConcurrentHashMap} 实现，
 * 确保在高并发场景下的线程安全性。
 *
 * <p><b>核心职责：</b>
 * <ul>
 *   <li><b>连接存储：</b>以设备 MAC 地址为键，存储对应的 Netty Channel 连接实例</li>
 *   <li><b>连接管理：</b>提供添加、获取、移除连接的线程安全操作，支持并发读写</li>
 *   <li><b>本地缓存：</b>作为设备连接的本地注册表，供同一 JVM 内的业务组件（如消息推送服务）使用</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>设备认证成功后：</b>将设备 Channel 存入此组，标记设备为在线状态</li>
 *   <li><b>向设备推送消息时：</b>根据设备 MAC 地址从组中获取 Channel，通过 Channel 发送数据</li>
 *   <li><b>设备断开连接时：</b>从组中移除对应的 Channel，释放资源并标记设备离线</li>
 *   <li><b>设备重连时：</b>旧连接被移除，新连接被添加，确保映射关系最新</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li>使用 {@link ConcurrentHashMap} 保证线程安全，无需额外同步措施</li>
 *   <li>{@link #addChannel(String, Channel)} 方法采用 {@code computeIfAbsent} 原子操作，
 *       避免同一设备重复添加导致连接覆盖</li>
 *   <li>静态方法和成员简化调用，无需实例化即可全局访问</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li><b>单节点限制：</b>该类仅在单节点部署时有效，因为 Channel 对象不能跨 JVM 共享。
 *       分布式环境下需使用 Redis 等分布式存储 + Channel 标识（如 sessionId）来管理设备连接</li>
 *   <li><b>内存泄漏风险：</b>设备断开连接时必须调用 {@link #removeChannel(String)}，
 *       否则已关闭的 Channel 会长期占用内存，导致内存泄漏</li>
 *   <li><b>连接有效性：</b>从组中获取 Channel 后，应先通过 {@link Channel#isActive()} 检查连接是否有效，
 *       若无效则应移除并重新建立连接</li>
 *   <li><b>MAC 地址唯一性：</b>假设设备 MAC 地址全局唯一，若存在重复 MAC 的设备，可能导致连接混乱</li>
 * </ul>
 *
 * @author 0101
 * @see io.netty.channel.Channel
 * @see java.util.concurrent.ConcurrentHashMap
 * @since 2026-03-16
 */
public class DeviceChannelGroup {

    /**
     * 设备连接存储映射
     *
     * <p>使用 {@link ConcurrentHashMap} 保证线程安全，支持高并发场景下的读写操作。
     *
     * <p><b>Key 类型：</b> {@link String} - 设备 MAC 地址，作为设备的全局唯一标识<br>
     * <b>Value 类型：</b> {@link Channel} - Netty 通道，代表与设备的物理连接
     */
    private static final Map<String, Channel> DEVICE_CHANNEL_GROUP = new ConcurrentHashMap<>();

    /**
     * 添加设备通道
     *
     * <p>将设备 MAC 地址与对应的 Netty Channel 建立映射关系。
     * 使用 {@link ConcurrentHashMap#computeIfAbsent(Object, java.util.function.Function)} 方法
     * 确保操作的原子性，避免在多线程环境下重复添加同一设备导致连接覆盖。
     *
     * <p><b>处理逻辑：</b>
     * <ul>
     *   <li>如果 MAC 地址不存在，则添加新的映射</li>
     *   <li>如果 MAC 地址已存在，不会覆盖原有连接，防止已认证设备被意外顶替</li>
     * </ul>
     *
     * <p><b>使用场景：</b>设备 WebSocket 握手成功且认证通过后调用。
     *
     * @param macId   设备唯一标识（MAC 地址）
     * @param channel 设备对应的 Netty Channel
     */
    public static void addChannel(String macId, Channel channel) {
        DEVICE_CHANNEL_GROUP.computeIfAbsent(macId, k -> channel);
    }

    /**
     * 获取设备通道
     *
     * <p>根据设备 MAC 地址获取对应的 Netty Channel。
     * 获取后建议调用 {@link Channel#isActive()} 检查连接是否仍然有效。
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>向设备推送消息前，获取 Channel 用于发送数据</li>
     *   <li>检查设备是否在线（是否存在对应的 Channel）</li>
     * </ul>
     *
     * @param macId 设备唯一标识（MAC 地址）
     * @return 设备对应的 Netty Channel，若不存在则返回 null
     */
    public static Channel getChannel(String macId) {
        return DEVICE_CHANNEL_GROUP.get(macId);
    }

    /**
     * 移除设备通道
     *
     * <p>根据设备 MAC 地址移除对应的 Channel 映射。
     * 必须在设备断开连接、认证失效或主动踢出设备时调用，以确保资源正确释放。
     *
     * <p><b>注意事项：</b>
     * <ul>
     *   <li>必须与 {@link #addChannel(String, Channel)} 成对使用，避免内存泄漏</li>
     *   <li>移除操作后，Channel 本身不会被自动关闭，调用方应根据需要调用 {@link Channel#close()}</li>
     * </ul>
     *
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>WebSocket 连接关闭时（主动断开或异常断开）</li>
     *   <li>设备认证失效（如 token 过期）需要强制下线时</li>
     *   <li>设备重连时，先移除旧连接再添加新连接</li>
     * </ul>
     *
     * @param macId 设备唯一标识（MAC 地址）
     */
    public static void removeChannel(String macId) {
        DEVICE_CHANNEL_GROUP.remove(macId);
    }
}