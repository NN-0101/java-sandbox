package com.sandbox.services.common.base.tlog;

import ch.qos.logback.core.PropertyDefinerBase;

/**
 * Logback 日志路径动态属性定义类
 *
 * <p>该类继承自 Logback 的 {@link PropertyDefinerBase}，用于根据操作系统类型
 * 动态确定日志文件的存储路径。通过此类的动态属性定义，可以在 logback-spring.xml
 * 配置文件中使用 {@code ${logPath}} 的方式引用计算出的路径，实现跨平台日志路径的
 * 统一管理和动态适配。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>跨平台适配：</b>自动识别操作系统类型，返回适合该平台的日志存储路径</li>
 *   <li><b>动态计算：</b>路径值在 Logback 初始化时计算，避免了硬编码</li>
 *   <li><b>路径规范化：</b>为不同操作系统提供符合其文件系统规范的路径格式</li>
 * </ul>
 *
 * <p><b>路径计算规则：</b>
 * <table border="1">
 *   <tr>
 *     <th>操作系统</th>
 *     <th>返回路径</th>
 *     <th>示例</th>
 *     <th>说明</th>
 *   </tr>
 *   <tr>
 *     <td>Mac OS</td>
 *     <td>{user.home}/logs</td>
 *     <td>/Users/zhangsan/logs</td>
 *     <td>使用用户主目录下的 logs 文件夹</td>
 *   </tr>
 *   <tr>
 *     <td>Windows</td>
 *     <td>logs</td>
 *     <td>logs\</td>
 *     <td>使用相对路径 logs，相对于应用工作目录</td>
 *   </tr>
 *   <tr>
 *     <td>Linux/Unix</td>
 *     <td>/{user.name}/logs</td>
 *     <td>/zhangsan/logs</td>
 *     <td>使用根目录下以用户名命名的 logs 文件夹</td>
 *   </tr>
 * </table>
 *
 * <p><b>使用方式（logback-spring.xml）：</b>
 * <pre>
 * &lt;!-- 定义动态路径属性 --&gt;
 * &lt;define name="logPath" class="com.sandbox.services.common.base.tlog.LogPathProperty"/&gt;
 *
 * &lt;!-- 在 appender 中使用动态路径 --&gt;
 * &lt;appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender"&gt;
 *     &lt;file&gt;${logPath}/application.log&lt;/file&gt;
 *     &lt;!-- 其他配置 --&gt;
 * &lt;/appender&gt;
 * </pre>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>继承 Logback 基类：</b>通过继承 {@link PropertyDefinerBase} 实现自定义属性定义器</li>
 *   <li><b>系统属性获取：</b>使用 {@code System.getProperty("os.name")} 获取操作系统类型，
 *       {@code System.getProperty("user.name")} 获取当前用户名</li>
 *   <li><b>字符串匹配：</b>通过 {@code startsWith} 进行操作系统类型判断，兼容不同版本</li>
 *   <li><b>路径兼容性：</b>返回的路径使用正斜杠 "/"，Logback 会自动转换为当前系统的路径分隔符</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>多环境部署：</b>同一应用需要在不同操作系统上部署时，自动适配日志路径</li>
 *   <li><b>本地开发：</b>开发人员在 Mac/Windows/Linux 上使用统一的日志配置</li>
 *   <li><b>容器化部署：</b>在 Docker 容器中（通常是 Linux），路径会自动适配为 /用户名/logs</li>
 *   <li><b>避免硬编码：</b>防止在日志配置中硬编码绝对路径导致的环境不兼容</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>Linux/Unix 下返回的路径如 {@code /username/logs} 需要确保应用有权限在根目录下创建目录，
 *       建议根据实际情况调整为更合适的路径（如 {@code /var/log/appname}）</li>
 *   <li>Windows 下使用相对路径 {@code logs}，日志会输出到应用启动目录，需要注意权限问题</li>
 *   <li>此属性仅在 Logback 初始化时计算一次，运行时不会重新计算</li>
 *   <li>如果需要对路径进行更复杂的动态计算，可以扩展此类的逻辑</li>
 * </ul>
 *
 * @author 0101
 * @see PropertyDefinerBase
 * @see ch.qos.logback.core.rolling.RollingFileAppender
 * @since 2026-03-12
 */
public class LogPathProperty extends PropertyDefinerBase {

    /**
     * 默认构造方法
     */
    public LogPathProperty() {
        // 默认构造方法，供 Logback 实例化使用
    }

    /**
     * 获取动态计算的日志路径属性值
     *
     * <p>根据当前操作系统类型动态计算日志存储路径：
     * <ul>
     *   <li><b>Mac OS：</b>返回 {@code user.home}/logs，如 /Users/username/logs</li>
     *   <li><b>Windows：</b>返回 {@code logs}，相对路径</li>
     *   <li><b>其他（Linux/Unix）：</b>返回 {@code /username/logs}</li>
     * </ul>
     *
     * @return 计算出的日志路径字符串
     */
    @Override
    public String getPropertyValue() {
        // 获取操作系统名称和当前用户名
        String osName = System.getProperty("os.name");
        String userName = System.getProperty("user.name");

        // 判断操作系统类型并返回相应路径
        if (osName.startsWith("Mac OS")) {
            // Mac 系统：使用用户主目录下的 logs 文件夹
            return System.getProperty("user.home") + "/logs";
        } else if (osName.startsWith("Windows")) {
            // Windows 系统：使用相对路径 logs
            return "logs";
        } else {
            // Linux/Unix 系统：使用 /用户名/logs 格式
            return String.format("/%s/logs", userName);
        }
    }
}