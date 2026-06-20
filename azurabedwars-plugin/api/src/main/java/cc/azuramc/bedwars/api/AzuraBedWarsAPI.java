package cc.azuramc.bedwars.api;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
public final class AzuraBedWarsAPI {

    private static volatile Provider provider;

    private AzuraBedWarsAPI() {
    }

    public static void setProvider(Provider provider) {
        AzuraBedWarsAPI.provider = provider;
    }

    private static Provider provider() {
        Provider p = provider;
        if (p == null) {
            throw new IllegalStateException("AzuraBedWarsAPI 尚未初始化（插件未启用或调用过早）");
        }
        return p;
    }

    /**
     * 根据 UUID 查找游戏玩家
     *
     * @param uuid 玩家 UUID
     * @return 对应的游戏玩家，不存在时为 null
     */
    @Nullable
    public static IGamePlayer getPlayer(UUID uuid) {
        return provider().getPlayer(uuid);
    }

    /**
     * 输出 info 级别日志
     *
     * @param message 日志内容
     */
    public static void info(String message) {
        provider().info(message);
    }

    /**
     * 输出 warn 级别日志
     *
     * @param message 日志内容
     */
    public static void warn(String message) {
        provider().warn(message);
    }

    /**
     * 输出 error 级别日志
     *
     * @param message 日志内容
     */
    public static void error(String message) {
        provider().error(message);
    }

    /**
     * 输出 debug 级别日志（受运行时调试开关控制）
     *
     * @param message 日志内容
     */
    public static void debug(String message) {
        provider().debug(message);
    }

    /**
     * 运行时实现契约，由 core 提供。
     */
    public interface Provider {

        /**
         * 根据 UUID 查找游戏玩家
         *
         * @param uuid 玩家 UUID
         * @return 游戏玩家，不存在时为 null
         */
        @Nullable
        IGamePlayer getPlayer(UUID uuid);

        void info(String message);

        void warn(String message);

        void error(String message);

        void debug(String message);
    }
}
