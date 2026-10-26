package cc.azuramc.bedwars.api.extension;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Extensions are referenced from config files as {@code <plugin>:<id>}, with the plugin name in lower case.
 * Register them in {@code onEnable}, references are checked on the first tick after every plugin is enabled.
 * Everything a plugin registered is removed when it is disabled
 * <p>
 * A settings type is a class with a public no argument constructor, bound like the config classes of AzuraBedWars:
 * fields in kebab case, {@link cc.azuramc.bedwars.api.config.Comment} for editor hints and
 * {@link cc.azuramc.bedwars.api.config.Range} for checked bounds. Use {@link NoSettings} when nothing is read
 *
 * @author an5w1r@163.com
 */
@ApiStatus.NonExtendable
public interface ExtensionRegistry {

    /**
     * @throws IllegalArgumentException when the id is taken or not made of a-z, 0-9, _ and -
     */
    <S> void registerItem(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull CustomItem<S> item);

    <S> void registerPurchaseAction(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull PurchaseAction<S> action);

    <S> void registerCondition(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull PurchaseCondition<S> condition);

    <S> void registerUpgradeEffect(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull UpgradeEffect<S> effect);

    <S> void registerTrapEffect(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull TrapEffect<S> effect);

    <S> void registerTimelineAction(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull TimelineAction<S> action);

    void registerPlaceholder(@NotNull Plugin plugin, @NotNull String id, @NotNull Placeholder placeholder);

    void unregisterAll(@NotNull Plugin plugin);
}
