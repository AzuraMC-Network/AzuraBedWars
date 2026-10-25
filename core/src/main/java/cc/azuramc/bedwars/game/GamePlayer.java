package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.api.event.player.BedwarsPlayerStateChangeEvent;
import cc.azuramc.bedwars.api.event.player.BedwarsPlayerStateChangeEvent.StateChangeType;
import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.SettingsConfig;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.item.armor.ArmorType;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import cc.azuramc.bedwars.game.level.PlayerLevelManager;
import cc.azuramc.bedwars.game.spectator.SpectatorTarget;
import cc.azuramc.bedwars.upgrade.UpgradeEffects;
import cc.azuramc.bedwars.util.MessageUtil;
import cc.azuramc.bedwars.util.packet.ArmorHider;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import fr.mrmicky.fastboard.FastBoard;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A player who is online on this server. Created on join and dropped on quit, so {@link #getPlayer()} is never stale.
 * State that must survive a reconnect lives in {@link ReconnectState}
 *
 * @author an5w1r@163.com
 */
@Data
public class GamePlayer implements IGamePlayer {

    private static final SettingsConfig settingsConfig = AzuraBedWars.getInstance().getSettingsConfig();

    private static final ConcurrentHashMap<UUID, GamePlayer> GAME_PLAYERS = new ConcurrentHashMap<>();
    private static final int MAX_HEALTH = settingsConfig.getMaxHealth();
    private static final float MAX_SATURATION = 5.0f;
    private static final int MAX_FOOD_LEVEL = 20;
    private static final long INVISIBILITY_TICKS = 30 * 20L;
    private static final long TRAP_PROTECTION_TICKS = 30 * 20L;
    private static final long TRAP_TRIGGER_COOLDOWN_TICKS = 8 * 20L;

    /** Indexed by {@link ToolType#ordinal()} */
    private static final XMaterial[] PICKAXES = {null, XMaterial.WOODEN_PICKAXE, XMaterial.STONE_PICKAXE, XMaterial.IRON_PICKAXE, XMaterial.DIAMOND_PICKAXE};
    private static final XMaterial[] AXES = {null, XMaterial.WOODEN_AXE, XMaterial.STONE_AXE, XMaterial.IRON_AXE, XMaterial.DIAMOND_AXE};

    private final Player player;
    private final UUID uuid;
    private final String name;
    private final AssistsManager assistsManager;
    private final PlayerData playerData;
    private final PlayerCompass playerCompass;

    private String nickName;
    private FastBoard board;
    @Setter(AccessLevel.NONE)
    private boolean isSpectator;
    private SpectatorTarget spectatorTarget;
    private GameTeam gameTeam;
    private boolean isAfk;
    @Setter(AccessLevel.NONE)
    private boolean isRespawning;
    private boolean isShoutCooldown;
    private boolean isEggBridgeCooldown;
    private boolean isViewingArrowDamage = true;
    private boolean isViewingAttackDamage = true;

    @Setter(AccessLevel.NONE)
    private boolean isInvisible;
    private BukkitRunnable invisibilityWaitingTask;
    private BukkitRunnable invisibilityFootStepTask;

    private ArmorType armorType = ArmorType.DEFAULT;
    private ToolType pickaxeType = ToolType.NONE;
    private ToolType axeType = ToolType.NONE;
    private boolean shear;

    @Setter(AccessLevel.NONE)
    private boolean isReconnect;

    private int currentGameKills;
    private int currentGameFinalKills;
    private int currentGameAssists;
    private int currentGameDeaths;
    private int currentGameDestroyedBeds;

    private boolean hasTrapProtection;
    private BukkitRunnable trapProtectionTask;

    private boolean isTrapTriggerCooldown;
    private BukkitRunnable trapTriggerTask;

    @Setter(AccessLevel.NONE)
    private long respawnProtectionEnd;

    private GamePlayer(@NotNull Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.assistsManager = new AssistsManager(this);
        this.playerData = AzuraBedWars.getInstance().getDatabaseProviderFactory().getPlayerDataService().selectPlayerData(this);
        this.playerCompass = new PlayerCompass(this);
    }

    @NotNull
    public static GamePlayer create(@NotNull Player player) {
        return GAME_PLAYERS.computeIfAbsent(player.getUniqueId(), k -> new GamePlayer(player));
    }

    @Nullable
    public static GamePlayer get(@NotNull UUID uuid) {
        return GAME_PLAYERS.get(uuid);
    }

    @Nullable
    public static GamePlayer get(@Nullable Player player) {
        return player == null ? null : get(player.getUniqueId());
    }

    public static void remove(@NotNull UUID uuid) {
        GAME_PLAYERS.remove(uuid);
    }

    @NotNull
    public static List<GamePlayer> getGamePlayers() {
        return new ArrayList<>(GAME_PLAYERS.values());
    }

    @NotNull
    public static List<GamePlayer> getOnlinePlayers() {
        List<GamePlayer> onlinePlayers = new ArrayList<>();
        for (GamePlayer player : GAME_PLAYERS.values()) {
            if (player.isOnline()) {
                onlinePlayers.add(player);
            }
        }
        return onlinePlayers;
    }

    @NotNull
    public static List<GamePlayer> getSpectators() {
        List<GamePlayer> spectators = new ArrayList<>();
        for (GamePlayer gamePlayer : GAME_PLAYERS.values()) {
            if (gamePlayer.isSpectator()) {
                spectators.add(gamePlayer);
            }
        }
        return spectators;
    }

    public void startInvisibilityTask() {
        endInvisibility();

        ArmorHider.hideArmor(this, GamePlayer.getGamePlayers());
        setInvisible(true);

        if (invisibilityFootStepTask == null) {
            invisibilityFootStepTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isInvisible) {
                        cancel();
                        invisibilityFootStepTask = null;
                        return;
                    }
                    if (VersionUtil.isVersion1_8()) {
                        player.getWorld().playEffect(player.getLocation().add(0, 0.1, 0), Effect.valueOf("FOOTSTEP"), 0);
                    } else if (VersionUtil.isLessThan1_13()) {
                        player.getWorld().spawnParticle(Particle.valueOf("FOOTSTEP"), player.getLocation().add(0, 0.1, 0), 1);
                    }
                }
            };
            invisibilityFootStepTask.runTaskTimer(AzuraBedWars.getInstance().getPlugin(), 1L, 5L);
        }

        invisibilityWaitingTask = new BukkitRunnable() {
            @Override
            public void run() {
                sendMessage("&c隐身效果结束");
                endInvisibility();
            }
        };
        invisibilityWaitingTask.runTaskLater(AzuraBedWars.getInstance().getPlugin(), INVISIBILITY_TICKS);
    }

    public void endInvisibility() {
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }

        setInvisible(false);

        if (invisibilityWaitingTask != null) {
            invisibilityWaitingTask.cancel();
            invisibilityWaitingTask = null;
        }

        ArmorHider.showArmor(this, GamePlayer.getGamePlayers());
    }

    public void startTrapProtectionTask() {
        endTrapProtection();

        hasTrapProtection = true;
        trapProtectionTask = new BukkitRunnable() {
            @Override
            public void run() {
                sendMessage("&c魔法牛奶效果结束");
                endTrapProtection();
            }
        };
        trapProtectionTask.runTaskLater(AzuraBedWars.getInstance().getPlugin(), TRAP_PROTECTION_TICKS);
    }

    public void endTrapProtection() {
        hasTrapProtection = false;
        if (trapProtectionTask != null) {
            trapProtectionTask.cancel();
        }
    }

    public void startTrapTriggerCooldownTask() {
        endTrapTriggerCooldownTask();

        isTrapTriggerCooldown = true;
        trapTriggerTask = new BukkitRunnable() {
            @Override
            public void run() {
                endTrapTriggerCooldownTask();
            }
        };
        trapTriggerTask.runTaskLater(AzuraBedWars.getInstance().getPlugin(), TRAP_TRIGGER_COOLDOWN_TICKS);
    }

    public void endTrapTriggerCooldownTask() {
        isTrapTriggerCooldown = false;
        if (trapTriggerTask != null) {
            trapTriggerTask.cancel();
        }
    }

    public void protectAfterRespawn(long ticks) {
        respawnProtectionEnd = System.currentTimeMillis() + ticks * 50;
    }

    public boolean isRespawnProtected() {
        return System.currentTimeMillis() < respawnProtectionEnd;
    }

    public void endRespawnProtection() {
        respawnProtectionEnd = 0;
    }

    public void recordKill(boolean finalKill) {
        if (finalKill) {
            currentGameFinalKills++;
            playerData.addFinalKills();
            PlayerLevelManager.addExperience(this, 2);
        } else {
            currentGameKills++;
            playerData.addKills();
            PlayerLevelManager.addExperience(this, 1);
        }
    }

    public void recordDeath(boolean finalDeath) {
        currentGameDeaths++;
        if (finalDeath) {
            playerData.addFinalDeaths();
        } else {
            playerData.addDeaths();
        }
    }

    public void recordAssist() {
        currentGameAssists++;
        playerData.addAssists();
        PlayerLevelManager.addExperience(this, 0.5);
    }

    public void recordBedBreak() {
        currentGameDestroyedBeds++;
        playerData.addDestroyedBeds();
        PlayerLevelManager.addExperience(this, 3);
    }

    public void recordWin() {
        playerData.addWins();
        PlayerLevelManager.addExperience(this, 5);
    }

    public void recordGamePlayed() {
        currentGameKills = 0;
        currentGameFinalKills = 0;
        currentGameAssists = 0;
        currentGameDeaths = 0;
        currentGameDestroyedBeds = 0;
        playerData.addGames();
        PlayerLevelManager.addExperience(this, 0.1);
    }

    @NotNull
    public String getNickName() {
        return nickName != null ? nickName : name;
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    public void sendActionBar(@NotNull String message) {
        if (isOnline()) {
            MessageUtil.sendActionBar(player, message);
        }
    }

    public void sendTitle(@Nullable String title, @Nullable String subTitle, int fadeIn, int stay, int fadeOut) {
        if (isOnline()) {
            MessageUtil.sendTitle(player, title, subTitle, fadeIn, stay, fadeOut);
        }
    }

    public void sendMessage(@NotNull String message) {
        if (isOnline()) {
            player.sendMessage(MessageUtil.color(message));
        }
    }

    public void playSound(@NotNull Sound sound, float volume, float pitch) {
        if (isOnline()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public void setLastDamage(@NotNull GamePlayer damager) {
        assistsManager.setLastDamage(damager, System.currentTimeMillis());
    }

    @Nullable
    public GamePlayer getLastDamager() {
        return assistsManager.getLastDamager(System.currentTimeMillis());
    }

    public void giveInventory() {
        PlayerInventory inventory = player.getInventory();
        inventory.setHelmet(armorPiece(XMaterial.LEATHER_HELMET, true));
        inventory.setChestplate(armorPiece(XMaterial.LEATHER_CHESTPLATE, true));
        giveArmor();
        giveSword(false);
        givePickaxe(false);
        giveAxe(false);
        giveShear();
        player.updateInventory();
    }

    public void giveArmor() {
        PlayerInventory inventory = player.getInventory();
        switch (armorType) {
            case CHAINMAIL:
                inventory.setLeggings(armorPiece(XMaterial.CHAINMAIL_LEGGINGS, false));
                inventory.setBoots(armorPiece(XMaterial.CHAINMAIL_BOOTS, false));
                break;
            case IRON:
                inventory.setLeggings(armorPiece(XMaterial.IRON_LEGGINGS, false));
                inventory.setBoots(armorPiece(XMaterial.IRON_BOOTS, false));
                break;
            case DIAMOND:
                inventory.setLeggings(armorPiece(XMaterial.DIAMOND_LEGGINGS, false));
                inventory.setBoots(armorPiece(XMaterial.DIAMOND_BOOTS, false));
                break;
            default:
                inventory.setLeggings(armorPiece(XMaterial.LEATHER_LEGGINGS, true));
                inventory.setBoots(armorPiece(XMaterial.LEATHER_BOOTS, true));
                break;
        }
        UpgradeEffects.enchantArmor(this);
    }

    private ItemStack armorPiece(XMaterial material, boolean teamColored) {
        ItemBuilder builder = new ItemBuilder().setType(material.get());
        if (teamColored) {
            builder.setColor(gameTeam.getColor());
        }
        return builder.setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem();
    }

    public void giveSword(boolean replace) {
        Material woodenSword = XMaterial.WOODEN_SWORD.get();
        if (replace) {
            player.getInventory().remove(woodenSword);
        }
        ItemStack sword = new ItemBuilder().setType(woodenSword).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setUnbreakable(true, true).getItem();
        UpgradeEffects.enchantSword(gameTeam, sword);
        player.getInventory().addItem(sword);
    }

    public void givePickaxe(boolean replacePrevious) {
        giveTool(PICKAXES, pickaxeType, replacePrevious);
    }

    public void giveAxe(boolean replacePrevious) {
        giveTool(AXES, axeType, replacePrevious);
    }

    private void giveTool(XMaterial[] tiers, ToolType type, boolean replacePrevious) {
        if (type == ToolType.NONE) {
            return;
        }
        XMaterial previous = tiers[type.ordinal() - 1];
        if (replacePrevious && previous != null) {
            player.getInventory().remove(previous.get());
        }
        player.getInventory().addItem(new ItemBuilder()
                .setType(tiers[type.ordinal()].get())
                .setUnbreakable(true, true)
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .addEnchant(XEnchantment.EFFICIENCY.get(), 1)
                .getItem());
    }

    public void giveShear() {
        if (shear) {
            player.getInventory().addItem(new ItemBuilder().setType(XMaterial.SHEARS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
        }
    }

    public void cleanState() {
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setExp(0.0F);
        player.setLevel(0);
        player.setSneaking(false);
        player.setSprinting(false);
        player.setFoodLevel(MAX_FOOD_LEVEL);
        player.setSaturation(MAX_SATURATION);
        player.setExhaustion(0.0f);
        player.setHealth(MAX_HEALTH);
        player.setFireTicks(0);
        if (gameTeam != null) {
            endInvisibility();
            endTrapProtection();
        }

        PlayerInventory inventory = player.getInventory();
        inventory.setArmorContents(new ItemStack[4]);
        inventory.clear();

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void updateInventory() {
        player.updateInventory();
    }

    public void setSpectator(boolean spectator) {
        isSpectator = spectator;
        callStateChange(StateChangeType.SPECTATOR, spectator);
    }

    public void setRespawning(boolean respawning) {
        isRespawning = respawning;
        callStateChange(StateChangeType.RESPAWNING, respawning);
    }

    public void setInvisible(boolean invisible) {
        isInvisible = invisible;
        callStateChange(StateChangeType.INVISIBLE, invisible);
    }

    public void setReconnect(boolean reconnect) {
        isReconnect = reconnect;
        callStateChange(StateChangeType.RECONNECT, reconnect);
    }

    private void callStateChange(StateChangeType type, boolean enabled) {
        GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
        Bukkit.getPluginManager().callEvent(new BedwarsPlayerStateChangeEvent(gameManager, this, type, enabled));
    }

    public static class PlayerCompass {
        private final GamePlayer gamePlayer;

        public PlayerCompass(GamePlayer gamePlayer) {
            this.gamePlayer = gamePlayer;
        }

        public void sendClosestPlayer() {
            GamePlayer closestPlayer = AzuraBedWars.getInstance().getGameManager().findTargetPlayer(gamePlayer);
            if (closestPlayer == null || closestPlayer.getGameTeam() == null) {
                gamePlayer.sendActionBar("§c没有目标");
                return;
            }

            Player player = gamePlayer.getPlayer();
            Player targetPlayer = closestPlayer.getPlayer();
            int distance = (int) targetPlayer.getLocation().distance(player.getLocation());
            gamePlayer.sendActionBar(String.format("§f玩家 %s%s §f距离您 %dm",
                    closestPlayer.getGameTeam().getChatColor(), closestPlayer.getNickName(), distance));
            player.setCompassTarget(targetPlayer.getLocation());
        }
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GamePlayer && uuid.equals(((GamePlayer) obj).getUuid());
    }

    @Override
    public String toString() {
        return "GamePlayer(" + name + ")";
    }
}
