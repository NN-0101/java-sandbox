package com.sandbox.services.common.base.tlog;

import ch.qos.logback.core.PropertyDefinerBase;

/**
 * @description: Logback日志路径的动态属性定义类，继承自PropertyDefinerBase，
 * 用于根据操作系统类型动态确定日志文件的存储路径。
 * 实现逻辑：
 * - Mac OS系统：返回用户主目录下的logs目录（如：/Users/username/logs）
 * - Windows系统：返回相对路径logs（如：logs/）
 * - 其他系统（Linux/Unix）：返回/用户名/logs格式的路径
 * 这样设计可以实现跨平台的日志路径统一管理，在logback-spring.xml中
 * 可以通过${logPath}的方式引用这个动态路径，避免硬编码
 * @author: xp
 * @create: 2026/3/12
 */
public class LogPathProperty extends PropertyDefinerBase {
    public LogPathProperty() {
    }

    public String getPropertyValue() {
        String osName = System.getProperty("os.name");
        String userName = System.getProperty("user.name");
        if (osName.startsWith("Mac OS")) {
            return System.getProperty("user.home") + "/logs";
        } else {
            return osName.startsWith("Windows") ? "logs" : String.format("/%s/logs", userName);
        }
    }
}