package cc.azuramc.bedwars.jedis.data;

/**
 * @author an5w1r@163.com
 */

public enum ServerType {
    /**
     * 启动中
     */
    STARTUP,
    /**
     * 等待中
     */
    WAITING,
    /**
     * 运行中
     */
    RUNNING,
    /**
     * 结束中
     */
    END,
    /**
     * 异常的未知状态
     */
    UNKNOWN
}
