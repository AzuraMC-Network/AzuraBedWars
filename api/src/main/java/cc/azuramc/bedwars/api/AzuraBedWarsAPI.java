package cc.azuramc.bedwars.api;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Static entry point, usable once AzuraBedWars is enabled.
 *
 * @author an5w1r@163.com
 */
public final class AzuraBedWarsAPI {

    private static volatile Provider provider;

    private AzuraBedWarsAPI() {
    }

    @ApiStatus.Internal
    public static void setProvider(@Nullable Provider provider) {
        AzuraBedWarsAPI.provider = provider;
    }

    private static Provider provider() {
        Provider p = provider;
        if (p == null) {
            throw new IllegalStateException("AzuraBedWarsAPI is not ready, the plugin is disabled or not enabled yet");
        }
        return p;
    }

    /**
     * @return null when the player never joined this game
     * @throws IllegalStateException when the plugin is not enabled
     */
    @Nullable
    public static IGamePlayer getPlayer(@NotNull UUID uuid) {
        return provider().getPlayer(uuid);
    }

    public static void info(@NotNull String message) {
        provider().info(message);
    }

    public static void warn(@NotNull String message) {
        provider().warn(message);
    }

    public static void warn(@NotNull String message, @NotNull Throwable throwable) {
        provider().warn(message, throwable);
    }

    public static void error(@NotNull String message) {
        provider().error(message);
    }

    public static void error(@NotNull String message, @NotNull Throwable throwable) {
        provider().error(message, throwable);
    }

    /**
     * Only logged while debug mode is on.
     */
    public static void debug(@NotNull String message) {
        provider().debug(message);
    }

    /**
     * Implemented by the plugin core.
     */
    @ApiStatus.Internal
    public interface Provider {

        @Nullable
        IGamePlayer getPlayer(@NotNull UUID uuid);

        void info(@NotNull String message);

        void warn(@NotNull String message);

        void warn(@NotNull String message, @NotNull Throwable throwable);

        void error(@NotNull String message);

        void error(@NotNull String message, @NotNull Throwable throwable);

        void debug(@NotNull String message);
    }
}
