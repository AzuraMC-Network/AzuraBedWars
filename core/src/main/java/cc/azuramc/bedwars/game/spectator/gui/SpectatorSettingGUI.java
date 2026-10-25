package cc.azuramc.bedwars.game.spectator.gui;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.LangConfig;
import cc.azuramc.bedwars.config.object.GameplayConfig;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.spectator.SpectatorSettings;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 旁观者设置GUI
 * 提供旁观者各种设置的界面，包括速度效果、自动传送、夜视等
 *
 * @author an5w1r@163.com
 */
public class SpectatorSettingGUI extends CustomGUI {

    private static final int MAX_POTION_DURATION = Integer.MAX_VALUE;

    /**
     * 构造函数
     *
     * @param gamePlayer 打开GUI的玩家
     */
    public SpectatorSettingGUI(GamePlayer gamePlayer) {
        super(gamePlayer, lang().getSettingsTitle(), config().getInventorySize());
        SpectatorSettings spectatorSettings = SpectatorSettings.get(gamePlayer);

        initializeSpeedItems(gamePlayer, spectatorSettings);
        initializeOptionItems(gamePlayer, spectatorSettings);
    }

    /**
     * 初始化速度效果选项
     *
     * @param gamePlayer 游戏玩家
     * @param settings   旁观者设置
     */
    private void initializeSpeedItems(GamePlayer gamePlayer, SpectatorSettings settings) {
        // 无速度效果
        setItem(config().getSpeedNoneSlot(), createSpeedItem(XMaterial.LEATHER_BOOTS.get(), lang().getNoSpeed()),
                createSpeedAction(gamePlayer, settings, 0));

        // 速度 I
        setItem(config().getSpeedISlot(), createSpeedItem(XMaterial.CHAINMAIL_BOOTS.get(), MessageUtil.fill(lang().getSpeed(), "level", "I")),
                createSpeedAction(gamePlayer, settings, 1));

        // 速度 II
        setItem(config().getSpeedIISlot(), createSpeedItem(XMaterial.IRON_BOOTS.get(), MessageUtil.fill(lang().getSpeed(), "level", "II")),
                createSpeedAction(gamePlayer, settings, 2));

        // 速度 III
        setItem(config().getSpeedIIISlot(), createSpeedItem(XMaterial.GOLDEN_BOOTS.get(), MessageUtil.fill(lang().getSpeed(), "level", "III")),
                createSpeedAction(gamePlayer, settings, 3));

        // 速度 IV
        setItem(config().getSpeedIVSlot(), createSpeedItem(XMaterial.DIAMOND_BOOTS.get(), MessageUtil.fill(lang().getSpeed(), "level", "IV")),
                createSpeedAction(gamePlayer, settings, 4));
    }

    /**
     * 创建速度效果物品
     *
     * @param material 材质
     * @param name     显示名称
     * @return 物品栈
     */
    private ItemStack createSpeedItem(Material material, String name) {
        return new ItemBuilder()
                .setType(material)
                .setDisplayName(name)
                .getItem();
    }

    /**
     * 创建速度效果动作
     *
     * @param gamePlayer 游戏玩家
     * @param settings   旁观者设置
     * @param level      速度等级
     * @return GUI动作
     */
    private GUIAction createSpeedAction(GamePlayer gamePlayer, SpectatorSettings settings, int level) {
        Player player = gamePlayer.getPlayer();

        return new GUIAction(0, () -> {
            if (settings.getSpeed() == level) {
                removeSpeedEffect(gamePlayer);
                return;
            }

            removeSpeedEffect(gamePlayer);
            if (level > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, MAX_POTION_DURATION, level - 1));
                player.sendMessage(MessageUtil.fill(lang().getSpeedAdded(), "level", level));
            } else {
                player.sendMessage(lang().getSpeedRemoved());
            }
            settings.setSpeed(level);
        }, true);
    }

    /**
     * 移除速度效果
     *
     * @param gamePlayer 游戏玩家
     */
    private void removeSpeedEffect(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
    }

    /**
     * 初始化功能选项
     *
     * @param gamePlayer 游戏玩家
     * @param settings   旁观者设置
     */
    private void initializeOptionItems(GamePlayer gamePlayer, SpectatorSettings settings) {
        Player player = gamePlayer.getPlayer();
        // 自动传送
        setItem(config().getAutoTpSlot(), createOptionItem(
                XMaterial.COMPASS.get(),
                settings.getOption(SpectatorSettings.Option.AUTO_TP) ? lang().getAutoTpOff() : lang().getAutoTpOn(),
                settings.getOption(SpectatorSettings.Option.AUTO_TP) ? lang().getAutoTpOffLore() : lang().getAutoTpOnLore()
        ), createAutoTpAction(gamePlayer, settings));

        // 夜视
        setItem(config().getNightVisionSlot(), createOptionItem(
                XMaterial.ENDER_EYE.get(),
                settings.getOption(SpectatorSettings.Option.NIGHT_VISION) ? lang().getNightVisionOff() : lang().getNightVisionOn(),
                settings.getOption(SpectatorSettings.Option.NIGHT_VISION) ? lang().getNightVisionOffLore() : lang().getNightVisionOnLore()
        ), createNightVisionAction(player, settings));

        // 第一人称
        setItem(config().getFirstPersonSlot(), createOptionItem(
                XMaterial.CLOCK.get(),
                settings.getOption(SpectatorSettings.Option.FIRST_PERSON) ? lang().getFirstPersonOff() : lang().getFirstPersonOn(),
                settings.getOption(SpectatorSettings.Option.FIRST_PERSON) ?
                        new String[]{lang().getFirstPersonOffLore()} :
                        lang().getFirstPersonOnLore().toArray(new String[0])
        ), createFirstPersonAction(gamePlayer, settings));

        // 隐藏其他旁观者
        setItem(config().getHideOthersSlot(), createOptionItem(
                settings.getOption(SpectatorSettings.Option.HIDE_OTHER) ?
                        XMaterial.REDSTONE.get() :
                        XMaterial.GLOWSTONE_DUST.get(),
                settings.getOption(SpectatorSettings.Option.HIDE_OTHER) ? lang().getHideOthersOn() : lang().getHideOthersOff(),
                settings.getOption(SpectatorSettings.Option.HIDE_OTHER) ? lang().getHideOthersOnLore() : lang().getHideOthersOffLore()
        ), createHideOthersAction(gamePlayer, settings));

        // 飞行
        setItem(config().getFlySlot(), createOptionItem(
                XMaterial.FEATHER.get(),
                settings.getOption(SpectatorSettings.Option.FLY) ? lang().getFlyOff() : lang().getFlyOn(),
                settings.getOption(SpectatorSettings.Option.FLY) ? lang().getFlyOffLore() : lang().getFlyOnLore()
        ), createFlyAction(gamePlayer, settings));
    }

    /**
     * 创建选项物品
     *
     * @param material 材质
     * @param name     显示名称
     * @param lore     描述
     * @return 物品栈
     */
    private ItemStack createOptionItem(Material material, String name, String... lore) {
        return new ItemBuilder()
                .setType(material)
                .setDisplayName(name)
                .setLores(lore)
                .getItem();
    }

    /**
     * 创建自动传送动作
     *
     * @param settings 旁观者设置
     * @return GUI动作
     */
    private GUIAction createAutoTpAction(GamePlayer gamePlayer, SpectatorSettings settings) {
        return new GUIAction(0, () -> {
            boolean newValue = !settings.getOption(SpectatorSettings.Option.AUTO_TP);
            settings.setOption(SpectatorSettings.Option.AUTO_TP, newValue);
            gamePlayer.sendMessage(newValue ? lang().getAutoTpEnabled() : lang().getAutoTpDisabled());
        }, true);
    }

    /**
     * 创建夜视动作
     *
     * @param player   玩家
     * @param settings 旁观者设置
     * @return GUI动作
     */
    private GUIAction createNightVisionAction(Player player, SpectatorSettings settings) {
        return new GUIAction(0, () -> {
            boolean newValue = !settings.getOption(SpectatorSettings.Option.NIGHT_VISION);
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            if (newValue) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, MAX_POTION_DURATION, 1));
            }
            settings.setOption(SpectatorSettings.Option.NIGHT_VISION, newValue);
            player.sendMessage(newValue ? lang().getNightVisionEnabled() : lang().getNightVisionDisabled());
        }, true);
    }

    /**
     * 创建第一人称Action
     *
     * @param gamePlayer 游戏玩家
     * @param settings   旁观者设置
     * @return GUI动作
     */
    private GUIAction createFirstPersonAction(GamePlayer gamePlayer, SpectatorSettings settings) {
        Player player = gamePlayer.getPlayer();

        return new GUIAction(0, () -> {
            boolean newValue = !settings.getOption(SpectatorSettings.Option.FIRST_PERSON);
            settings.setOption(SpectatorSettings.Option.FIRST_PERSON, newValue);
            gamePlayer.sendMessage(newValue ? lang().getFirstPersonEnabled() : lang().getFirstPersonDisabled());

            if (!newValue && gamePlayer.isSpectator() && player.getGameMode() == GameMode.SPECTATOR) {
                gamePlayer.sendTitle(lang().getLeftSpectating(), null, 0, 20, 0);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }, true);
    }

    /**
     * 创建隐藏其他旁观者动作
     *
     * @param settings 旁观者设置
     * @return GUI动作
     */
    private GUIAction createHideOthersAction(GamePlayer gamePlayer, SpectatorSettings settings) {
        return new GUIAction(0, () -> {
            boolean newValue = !settings.getOption(SpectatorSettings.Option.HIDE_OTHER);
            settings.setOption(SpectatorSettings.Option.HIDE_OTHER, newValue);
            gamePlayer.sendMessage(newValue ? lang().getHideOthersEnabled() : lang().getHideOthersDisabled());
        }, true);
    }

    /**
     * 创建飞行动作
     *
     * @param gamePlayer 游戏玩家
     * @param settings   旁观者设置
     * @return GUI动作
     */
    private GUIAction createFlyAction(GamePlayer gamePlayer, SpectatorSettings settings) {
        Player player = gamePlayer.getPlayer();

        return new GUIAction(0, () -> {
            boolean newValue = !settings.getOption(SpectatorSettings.Option.FLY);
            settings.setOption(SpectatorSettings.Option.FLY, newValue);
            gamePlayer.sendMessage(newValue ? lang().getFlyEnabled() : lang().getFlyDisabled());

            if (newValue) {
                if (player.isOnGround()) {
                    player.getLocation().setY(player.getLocation().getY() + 0.1D);
                }
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }, true);
    }

    private static LangConfig.Spectator lang() {
        return AzuraBedWars.getInstance().getLang().getSpectator();
    }

    private static GameplayConfig.Spectator.SettingGui config() {
        return AzuraBedWars.getInstance().getGameplayConfig().getSpectator().getSettingGui();
    }
}
