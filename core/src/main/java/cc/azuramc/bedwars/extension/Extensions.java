package cc.azuramc.bedwars.extension;

import cc.azuramc.bedwars.api.extension.CustomItem;
import cc.azuramc.bedwars.api.extension.ExtensionRegistry;
import cc.azuramc.bedwars.api.extension.ItemUseContext;
import cc.azuramc.bedwars.api.extension.Placeholder;
import cc.azuramc.bedwars.api.extension.PurchaseAction;
import cc.azuramc.bedwars.api.extension.PurchaseCondition;
import cc.azuramc.bedwars.api.extension.TimelineAction;
import cc.azuramc.bedwars.api.extension.TrapEffect;
import cc.azuramc.bedwars.api.extension.UpgradeEffect;
import cc.azuramc.bedwars.api.game.IGameManager;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import cc.azuramc.bedwars.config.yaml.ConfigKeys;
import cc.azuramc.bedwars.config.yaml.YamlBinder;
import cc.azuramc.bedwars.util.LoggerUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.nodes.Node;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Extensions registered by other plugins, looked up by the {@code <plugin>:<id>} references of config files.
 * Every call into another plugin is caught, so a broken extension never stops a game
 *
 * @author an5w1r@163.com
 */
public final class Extensions implements ExtensionRegistry, Listener {

    private static final Pattern ID = Pattern.compile("[a-z0-9_-]+");

    public enum Status {
        AVAILABLE,
        /** Before the first tick, other plugins may still register */
        PENDING,
        NO_PLUGIN,
        UNKNOWN
    }

    private final Map<ExtensionKind, Map<String, Registered>> registered = new EnumMap<>(ExtensionKind.class);
    /** Bound settings by the node they came from, a reload brings new nodes */
    private final Map<Node, Object> settings = new WeakHashMap<>();
    /** Enabled state of a plugin by its lower case name */
    private final Predicate<String> pluginEnabled;
    @Getter
    private boolean ready;
    /** Replaced on every change, so caches built from the registry notice it */
    @Getter
    private Object version = new Object();
    @Setter
    private Runnable onChange = () -> {
    };

    public Extensions(Predicate<String> pluginEnabled) {
        this.pluginEnabled = pluginEnabled;
    }

    public void markReady() {
        ready = true;
        version = new Object();
    }

    @Override
    public <S> void registerItem(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull CustomItem<S> item) {
        register(ExtensionKind.ITEM, plugin, id, settings, item);
    }

    @Override
    public <S> void registerPurchaseAction(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull PurchaseAction<S> action) {
        register(ExtensionKind.PURCHASE_ACTION, plugin, id, settings, action);
    }

    @Override
    public <S> void registerCondition(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull PurchaseCondition<S> condition) {
        register(ExtensionKind.CONDITION, plugin, id, settings, condition);
    }

    @Override
    public <S> void registerUpgradeEffect(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull UpgradeEffect<S> effect) {
        register(ExtensionKind.UPGRADE_EFFECT, plugin, id, settings, effect);
    }

    @Override
    public <S> void registerTrapEffect(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull TrapEffect<S> effect) {
        register(ExtensionKind.TRAP_EFFECT, plugin, id, settings, effect);
    }

    @Override
    public <S> void registerTimelineAction(@NotNull Plugin plugin, @NotNull String id, @NotNull Class<S> settings, @NotNull TimelineAction<S> action) {
        register(ExtensionKind.TIMELINE_ACTION, plugin, id, settings, action);
    }

    @Override
    public void registerPlaceholder(@NotNull Plugin plugin, @NotNull String id, @NotNull Placeholder placeholder) {
        register(ExtensionKind.PLACEHOLDER, plugin, id, Void.class, placeholder);
    }

    private void register(ExtensionKind kind, Plugin plugin, String id, Class<?> settingsType, Object extension) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(settingsType, "settings");
        Objects.requireNonNull(extension, kind.getLabel());
        if (!ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Extension id " + id + " may only use a-z, 0-9, _ and -");
        }
        if (settingsType != Void.class) {
            ConfigKeys.newInstance(settingsType);
        }
        String ref = plugin.getName().toLowerCase(Locale.ROOT) + ":" + id;
        Map<String, Registered> ofKind = registered.computeIfAbsent(kind, k -> new LinkedHashMap<>());
        if (ofKind.containsKey(ref)) {
            throw new IllegalArgumentException("The " + kind.getLabel() + " " + ref + " is already registered");
        }
        ofKind.put(ref, new Registered(plugin, settingsType, extension));
        changed();
    }

    @Override
    public void unregisterAll(@NotNull Plugin plugin) {
        boolean removed = false;
        for (Map<String, Registered> ofKind : registered.values()) {
            removed |= ofKind.values().removeIf(entry -> entry.plugin == plugin);
        }
        if (removed) {
            changed();
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        unregisterAll(event.getPlugin());
    }

    private void changed() {
        version = new Object();
        if (ready) {
            onChange.run();
        }
    }

    public Status status(ExtensionKind kind, String ref) {
        if (registered.getOrDefault(kind, Collections.<String, Registered>emptyMap()).containsKey(ref)) {
            return Status.AVAILABLE;
        }
        if (!ready) {
            return Status.PENDING;
        }
        return pluginEnabled.test(ref.substring(0, Math.max(ref.indexOf(':'), 0))) ? Status.UNKNOWN : Status.NO_PLUGIN;
    }

    public boolean isAvailable(ExtensionKind kind, @Nullable String ref) {
        return ref != null && status(kind, ref) == Status.AVAILABLE;
    }

    /**
     * Settings types of every registered extension of a kind, by reference
     */
    public Map<String, Class<?>> settingsTypes(ExtensionKind kind) {
        Map<String, Class<?>> types = new LinkedHashMap<>();
        for (Map.Entry<String, Registered> entry : registered.getOrDefault(kind, Collections.<String, Registered>emptyMap()).entrySet()) {
            types.put(entry.getKey(), entry.getValue().settingsType);
        }
        return types;
    }

    @NotNull
    public ItemStack createItem(String ref, @Nullable Node node, IGamePlayer player, ItemStack item) {
        ItemStack created = this.<CustomItem<Object>, ItemStack>call(ExtensionKind.ITEM, ref, node, item,
                (custom, settings) -> custom.create(player, item, settings));
        return created != null ? created : item;
    }

    public void useItem(String ref, @Nullable Node node, ItemUseContext context) {
        this.<CustomItem<Object>, Void>call(ExtensionKind.ITEM, ref, node, null, (custom, settings) -> {
            custom.onUse(context, settings);
            return null;
        });
    }

    public void purchase(String ref, @Nullable Node node, IGamePlayer player) {
        this.<PurchaseAction<Object>, Void>call(ExtensionKind.PURCHASE_ACTION, ref, node, null, (action, settings) -> {
            action.purchase(player, settings);
            return null;
        });
    }

    /**
     * @return the message for a denied purchase, null when allowed. A condition that fails denies
     */
    @Nullable
    public String check(String ref, @Nullable Node node, IGamePlayer player) {
        return this.<PurchaseCondition<Object>, String>call(ExtensionKind.CONDITION, ref, node, "",
                (condition, settings) -> condition.check(player, settings));
    }

    public void applyUpgrade(String ref, @Nullable Node node, IGameTeam team) {
        this.<UpgradeEffect<Object>, Void>call(ExtensionKind.UPGRADE_EFFECT, ref, node, null, (effect, settings) -> {
            effect.apply(team, settings);
            return null;
        });
    }

    public void spawnWithUpgrade(String ref, @Nullable Node node, IGamePlayer player) {
        this.<UpgradeEffect<Object>, Void>call(ExtensionKind.UPGRADE_EFFECT, ref, node, null, (effect, settings) -> {
            effect.onSpawn(player, settings);
            return null;
        });
    }

    public void triggerTrap(String ref, @Nullable Node node, IGameTeam owner, IGamePlayer intruder) {
        this.<TrapEffect<Object>, Void>call(ExtensionKind.TRAP_EFFECT, ref, node, null, (effect, settings) -> {
            effect.trigger(owner, intruder, settings);
            return null;
        });
    }

    public void runTimeline(String ref, @Nullable Node node, IGameManager game) {
        this.<TimelineAction<Object>, Void>call(ExtensionKind.TIMELINE_ACTION, ref, node, null, (action, settings) -> {
            action.run(game, settings);
            return null;
        });
    }

    /**
     * Fills {@code {<plugin>:<id>}} placeholders
     */
    @NotNull
    public String replace(@NotNull String text, @Nullable Player viewer) {
        Map<String, Registered> placeholders = registered.get(ExtensionKind.PLACEHOLDER);
        if (placeholders == null || placeholders.isEmpty() || text.indexOf('{') < 0) {
            return text;
        }
        for (String ref : placeholders.keySet()) {
            String token = "{" + ref + "}";
            if (text.contains(token)) {
                String value = this.<Placeholder, String>call(ExtensionKind.PLACEHOLDER, ref, null, "",
                        (placeholder, settings) -> placeholder.apply(viewer));
                text = text.replace(token, value != null ? value : "");
            }
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <E, R> R call(ExtensionKind kind, String ref, @Nullable Node node, @Nullable R fallback, Call<E, R> call) {
        Registered entry = registered.getOrDefault(kind, Collections.<String, Registered>emptyMap()).get(ref);
        if (entry == null) {
            return fallback;
        }
        try {
            return call.apply((E) entry.extension, kind == ExtensionKind.PLACEHOLDER ? null : settings(entry.settingsType, node));
        } catch (RuntimeException | LinkageError e) {
            LoggerUtil.error("The " + kind.getLabel() + " " + ref + " of " + entry.plugin.getName() + " failed", e);
            return fallback;
        }
    }

    private Object settings(Class<?> type, @Nullable Node node) {
        if (node == null) {
            return new YamlBinder("", true).bind(null, type);
        }
        Object bound = settings.get(node);
        if (!type.isInstance(bound)) {
            bound = new YamlBinder("", true).bind(node, type, "");
            settings.put(node, bound);
        }
        return bound;
    }

    @FunctionalInterface
    private interface Call<E, R> {
        R apply(E extension, Object settings);
    }

    private static final class Registered {
        final Plugin plugin;
        final Class<?> settingsType;
        final Object extension;

        Registered(Plugin plugin, Class<?> settingsType, Object extension) {
            this.plugin = plugin;
            this.settingsType = settingsType;
            this.extension = extension;
        }
    }
}
