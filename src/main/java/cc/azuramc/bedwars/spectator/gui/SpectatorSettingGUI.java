package cc.azuramc.bedwars.spectator.gui;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.GamePlayer;
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

    private static final MessageConfig.Spectator.SettingGUI MESSAGE_CONFIG = AzuraBedWars.getInstance().getMessageConfig().getSpectator().getSettingGUI();
    private static final PlayerConfig.Spectator.SettingGUI CONFIG = AzuraBedWars.getInstance().getPlayerConfig().getSpectator().getSettingGUI();

    private static final String GUI_TITLE = MESSAGE_CONFIG.getGuiTitle();
    private static final int INVENTORY_SIZE = CONFIG.getInventorySize();
    private static final int MAX_POTION_DURATION = Integer.MAX_VALUE;

    private static final int SPEED_NONE_SLOT = CONFIG.getSpeedNoneSlot();
    private static final int SPEED_I_SLOT = CONFIG.getSpeedISlot();
    private static final int SPEED_II_SLOT = CONFIG.getSpeedIISlot();
    private static final int SPEED_III_SLOT = CONFIG.getSpeedIIISlot();
    private static final int SPEED_IV_SLOT = CONFIG.getSpeedIVSlot();

    private static final int AUTO_TP_SLOT = CONFIG.getAutoTPSlot();
    private static final int NIGHT_VISION_SLOT = CONFIG.getNightVersionSlot();
    private static final int FIRST_PERSON_SLOT = CONFIG.getFirstPersonSlot();
    private static final int HIDE_OTHERS_SLOT = CONFIG.getHideOthersSlot();
    private static final int FLY_SLOT = CONFIG.getFlySlot();

    private static final String SPEED_REMOVED = MESSAGE_CONFIG.getSpeedRemoved();
    private static final String SPEED_ADDED = MESSAGE_CONFIG.getSpeedAdded();
    private static final String AUTO_TP_ENABLED = MESSAGE_CONFIG.getAutoTPEnabled();
    private static final String AUTO_TP_DISABLED = MESSAGE_CONFIG.getAutoTPDisabled();
    private static final String NIGHT_VISION_ENABLED = MESSAGE_CONFIG.getNightVersionEnabled();
    private static final String NIGHT_VISION_DISABLED = MESSAGE_CONFIG.getNightVersionDisabled();
    private static final String FIRST_PERSON_ENABLED = MESSAGE_CONFIG.getFirstPersonEnabled();
    private static final String FIRST_PERSON_DISABLED = MESSAGE_CONFIG.getFirstPersonDisabled();
    private static final String HIDE_OTHERS_ENABLED = MESSAGE_CONFIG.getHideOthersEnabled();
    private static final String HIDE_OTHERS_DISABLED = MESSAGE_CONFIG.getHideOthersDisabled();
    private static final String FLY_ENABLED = MESSAGE_CONFIG.getFlyEnabled();
    private static final String FLY_DISABLED = MESSAGE_CONFIG.getFlyDisabled();

    /**
     * 构造函数
     * 
     * @param player 打开GUI的玩家
     */
    public SpectatorSettingGUI(Player player) {
        super(player, GUI_TITLE, INVENTORY_SIZE);
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        SpectatorSettings spectatorSettings = SpectatorSettings.get(gamePlayer);
        
        initializeSpeedItems(player, spectatorSettings);
        initializeOptionItems(player, gamePlayer, spectatorSettings);
    }

    /**
     * 初始化速度效果选项
     * 
     * @param player 玩家
     * @param settings 旁观者设置
     */
    private void initializeSpeedItems(Player player, SpectatorSettings settings) {
        // 无速度效果
        setItem(SPEED_NONE_SLOT, createSpeedItem(XMaterial.LEATHER_BOOTS.get(), "§a没有速度效果"),
            createSpeedAction(player, settings, 0));

        // 速度 I
        setItem(SPEED_I_SLOT, createSpeedItem(XMaterial.CHAINMAIL_BOOTS.get(), "§a速度 I"),
            createSpeedAction(player, settings, 1));
        
        // 速度 II
        setItem(SPEED_II_SLOT, createSpeedItem(XMaterial.IRON_BOOTS.get(), "§a速度 II"),
            createSpeedAction(player, settings, 2));
        
        // 速度 III
        setItem(SPEED_III_SLOT, createSpeedItem(XMaterial.GOLDEN_BOOTS.get(), "§a速度 III"),
            createSpeedAction(player, settings, 3));
        
        // 速度 IV
        setItem(SPEED_IV_SLOT, createSpeedItem(XMaterial.DIAMOND_BOOTS.get(), "§a速度 IV"),
            createSpeedAction(player, settings, 4));
    }

    /**
     * 创建速度效果物品
     * 
     * @param material 材质
     * @param name 显示名称
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
     * @param player 玩家
     * @param settings 旁观者设置
     * @param level 速度等级
     * @return GUI动作
     */
    private GUIAction createSpeedAction(Player player, SpectatorSettings settings, int level) {
        return new GUIAction(0, () -> {
            if (settings.getSpeed() == level) {
                removeSpeedEffect(player);
                return;
            }
            
            removeSpeedEffect(player);
            if (level > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, MAX_POTION_DURATION, level - 1));
                player.sendMessage(String.format(SPEED_ADDED, level));
            } else {
                player.sendMessage(SPEED_REMOVED);
            }
            settings.setSpeed(level);
        }, true);
    }

    /**
     * 移除速度效果
     * 
     * @param player 玩家
     */
    private void removeSpeedEffect(Player player) {
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
    }

    /**
     * 初始化功能选项
     * 
     * @param player 玩家
     * @param gamePlayer 游戏玩家
     * @param settings 旁观者设置
     */
    private void initializeOptionItems(Player player, GamePlayer gamePlayer, SpectatorSettings settings) {
        // 自动传送
        setItem(AUTO_TP_SLOT, createOptionItem(
            XMaterial.COMPASS.get(),
            settings.getOption(SpectatorSettings.Option.AUTO_TP) ? "§c停用自动传送" : "§a启动自动传送",
            settings.getOption(SpectatorSettings.Option.AUTO_TP) ? "§7点击停用自动传送" : "§7点击启用自动传送"
        ), createAutoTpAction(player, settings));

        // 夜视
        setItem(NIGHT_VISION_SLOT, createOptionItem(
            XMaterial.ENDER_EYE.get(),
            settings.getOption(SpectatorSettings.Option.NIGHT_VISION) ? "§c停用夜视" : "§a启动夜视",
            settings.getOption(SpectatorSettings.Option.NIGHT_VISION) ? "§7点击停用夜视" : "§7点击启用夜视"
        ), createNightVisionAction(player, settings));

        // 第一人称
        setItem(FIRST_PERSON_SLOT, createOptionItem(
            XMaterial.CLOCK.get(),
            settings.getOption(SpectatorSettings.Option.FIRST_PERSON) ? "§c停用第一人称旁观" : "§a启动第一人称旁观",
            settings.getOption(SpectatorSettings.Option.FIRST_PERSON) ?
                "§7点击停用第一人称旁观" : 
                "§7点击确认使用指南针时\n§7自动沿用第一人称旁观！\n§7你也可以右键点击一位玩家\n§7来启用第一人称旁观"
        ), createFirstPersonAction(player, gamePlayer, settings));

        // 隐藏其他旁观者
        setItem(HIDE_OTHERS_SLOT, createOptionItem(
            settings.getOption(SpectatorSettings.Option.HIDE_OTHER) ?
                XMaterial.REDSTONE.get() :
                XMaterial.GLOWSTONE_DUST.get(),
            settings.getOption(SpectatorSettings.Option.HIDE_OTHER) ? "§c隐藏旁观者" : "§a查看旁观者",
            settings.getOption(SpectatorSettings.Option.HIDE_OTHER) ?
                "§7点击来隐藏其他旁观者" : "§7点击以显示其他旁观者"
        ), createHideOthersAction(player, settings));

        // 飞行
        setItem(FLY_SLOT, createOptionItem(
            XMaterial.FEATHER.get(),
            settings.getOption(SpectatorSettings.Option.FLY) ? "§c停用持续飞行" : "§a启动持续飞行",
            settings.getOption(SpectatorSettings.Option.FLY) ? "§7点击停用飞行" : "§7点击启用飞行"
        ), createFlyAction(player, settings));
    }

    /**
     * 创建选项物品
     * 
     * @param material 材质
     * @param name 显示名称
     * @param lore 描述
     * @return 物品栈
     */
    private ItemStack createOptionItem(Material material, String name, String lore) {
        return new ItemBuilder()
            .setType(material)
            .setDisplayName(name)
            .setLores(lore)
            .getItem();
    }

    /**
     * 创建自动传送动作
     * 
     * @param player 玩家
     * @param settings 旁观者设置
     * @return GUI动作
     */
    private GUIAction createAutoTpAction(Player player, SpectatorSettings settings) {
        return new GUIAction(0, () -> {
            boolean newValue = !settings.getOption(SpectatorSettings.Option.AUTO_TP);
            settings.setOption(SpectatorSettings.Option.AUTO_TP, newValue);
            player.sendMessage(newValue ? AUTO_TP_ENABLED : AUTO_TP_DISABLED);
        }, true);
    }

    /**
     * 创建夜视动作
     * 
     * @param player 玩家
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
            player.sendMessage(newValue ? NIGHT_VISION_ENABLED : NIGHT_VISION_DISABLED);
        }, true);
    }

    /**
     * 创建第一人称动作
     * 
     * @param player 玩家
     * @param gamePlayer 游戏玩家
     * @param settings 旁观者设置
     * @return GUI动作
     */
    private GUIAction createFirstPersonAction(Player player, GamePlayer gamePlayer, SpectatorSettings settings) {
        return new GUIAction(0, () -> {
            boolean newValue = !settings.getOption(SpectatorSettings.Option.FIRST_PERSON);
            settings.setOption(SpectatorSettings.Option.FIRST_PERSON, newValue);
            player.sendMessage(newValue ? FIRST_PERSON_ENABLED : FIRST_PERSON_DISABLED);
            
            if (!newValue && gamePlayer.isSpectator() && player.getGameMode() == GameMode.SPECTATOR) {
                gamePlayer.sendTitle(0, 20, 0, "§e退出旁观模式", null);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }, true);
    }

    /**
     * 创建隐藏其他旁观者动作
     * 
     * @param player 玩家
     * @param settings 旁观者设置
     * @return GUI动作
     */
    private GUIAction createHideOthersAction(Player player, SpectatorSettings settings) {
        return new GUIAction(0, () -> {
            boolean newValue = !settings.getOption(SpectatorSettings.Option.HIDE_OTHER);
            settings.setOption(SpectatorSettings.Option.HIDE_OTHER, newValue);
            player.sendMessage(newValue ? HIDE_OTHERS_ENABLED : HIDE_OTHERS_DISABLED);
        }, true);
    }

    /**
     * 创建飞行动作
     * 
     * @param player 玩家
     * @param settings 旁观者设置
     * @return GUI动作
     */
    private GUIAction createFlyAction(Player player, SpectatorSettings settings) {
        return new GUIAction(0, () -> {
            boolean newValue = !settings.getOption(SpectatorSettings.Option.FLY);
            settings.setOption(SpectatorSettings.Option.FLY, newValue);
            player.sendMessage(newValue ? FLY_ENABLED : FLY_DISABLED);
            
            if (newValue) {
                if (player.isOnGround()) {
                    player.getLocation().setY(player.getLocation().getY() + 0.1D);
                }
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }, true);
    }
}
