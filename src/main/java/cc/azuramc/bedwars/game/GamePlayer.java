package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ActionBarUtil;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.compat.util.TitleUtil;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.database.profile.PlayerProfile;
import cc.azuramc.bedwars.game.item.armor.ArmorType;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.scoreboard.base.FastBoard;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import cc.azuramc.bedwars.spectator.SpectatorTarget;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏玩家类
 * <p>
 * 管理玩家在游戏中的状态、装备、数据等
 * 提供玩家相关的各种操作方法
 * </p>
 * @author an5w1r@163.com
 */
public class GamePlayer {

    private static final PlayerConfig.GamePlayer CONFIG = AzuraBedWars.getInstance().getPlayerConfig().getGamePlayer();

    private static final ConcurrentHashMap<UUID, GamePlayer> GAME_PLAYERS = new ConcurrentHashMap<>();
    private static final int MAX_HEALTH = CONFIG.getMaxHealth();
    private static final float MAX_SATURATION = 5.0f;
    private static final int MAX_FOOD_LEVEL = 20;

    @Getter private final UUID uuid;
    @Getter private final String name;
    @Getter private final AssistsManager assistsManager;
    @Getter private final PlayerProfile playerProfile;
    @Getter private final PlayerCompass playerCompass;

    @Getter @Setter GameModeType gameModeType;
    /** 使用 HashMap 存储经验来源，键是资源名(String)，值是经验数量(Integer) */
    @Getter Map<String, Integer> experienceSources;

    @Setter private String nickName;
    @Getter @Setter private FastBoard board;
    @Getter private boolean spectator;
    @Getter @Setter private SpectatorTarget spectatorTarget;
    @Getter @Setter private GameTeam gameTeam;
    @Setter @Getter private boolean isAfk;
    @Setter @Getter private boolean isShoutCooldown;
    @Getter @Setter private boolean isEggBridgeCooldown;
    @Getter @Setter private boolean isViewingArrowDamage;
    @Getter @Setter private boolean isViewingAttackDamage;

    @Getter private int kills;
    @Getter private int finalKills;
    
    // 装备状态
    @Getter @Setter private ArmorType armorType;
    @Getter @Setter private ToolType pickaxeType;
    @Getter @Setter private ToolType axeType;
    @Getter @Setter private boolean shear;

    /**
     * 构造方法
     * 
     * @param uuid 玩家UUID
     * @param name 玩家名称
     */
    public GamePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        // 初始化管理器
        this.assistsManager = new AssistsManager(this);
        this.playerProfile = new PlayerProfile(this);
        this.playerCompass = new PlayerCompass(this);

        // 初始化游戏状态
        this.isAfk = false;
        this.isShoutCooldown = false;
        this.isEggBridgeCooldown = false;
        this.isViewingArrowDamage = true;
        this.isViewingAttackDamage = true;

        // 初始化装备状态
        this.armorType = ArmorType.DEFAULT;
        this.pickaxeType = ToolType.NONE;
        this.axeType = ToolType.NONE;

        // 游戏模式
        this.gameModeType = getPlayerProfile().getGameModeType();
        this.experienceSources = new HashMap<>();
    }

    /**
     * 创建或获取游戏玩家实例
     * 
     * @param uuid 玩家UUID
     * @param name 玩家名称
     * @return 游戏玩家实例
     */
    public static GamePlayer create(UUID uuid, String name) {
        return GAME_PLAYERS.computeIfAbsent(uuid, k -> new GamePlayer(uuid, name));
    }

    /**
     * 获取游戏玩家实例
     * 
     * @param uuid 玩家UUID
     * @return 游戏玩家实例
     */
    public static GamePlayer get(UUID uuid) {
        return GAME_PLAYERS.get(uuid);
    }

    /**
     * 获取玩家显示名称
     * 
     * @return 玩家显示名称
     */
    public String getNickName() {
        return this.nickName != null ? this.nickName : this.name;
    }

    /**
     * 获取所有游戏玩家
     * 
     * @return 游戏玩家列表
     */
    public static List<GamePlayer> getGamePlayers() {
        return new ArrayList<>(GAME_PLAYERS.values());
    }

    /**
     * 获取所有团队玩家
     * 
     * @return 团队玩家列表
     */
    public static List<GamePlayer> getTeamPlayers() {
        List<GamePlayer> teamPlayers = new ArrayList<>();
        for (GamePlayer player : GAME_PLAYERS.values()) {
            if (player.getGameTeam() != null) {
                teamPlayers.add(player);
            }
        }
        return teamPlayers;
    }

    /**
     * 增加指定资源来源的经验值
     * @param resourceType 资源类型 (e.g., "iron")
     * @param amount       增加的数量
     */
    public void addExperience(String resourceType, int amount) {
        if (amount <= 0) {
            Bukkit.getLogger().warning("addExperience 应该输入大于0的数值");
            return;
        }

        int currentXp = this.experienceSources.getOrDefault(resourceType, 0);
        this.experienceSources.put(resourceType, currentXp + amount);
    }

    /**
     * 消耗指定资源来源的经验值
     * @param resourceType 资源类型
     * @param amount       消耗的数量
     * @return true 如果消耗成功，否则返回 false
     */
    public boolean spendExperience(String resourceType, int amount) {
        if (amount <= 0) {
            Bukkit.getLogger().warning("spendExperience 应该输入大于0的数值");
            return false;
        }

        int currentXp = this.experienceSources.getOrDefault(resourceType, 0);

        // 检查扣除的经验是否大于本有的经验
        if (currentXp >= amount) {
            this.experienceSources.put(resourceType, currentXp - amount);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取指定资源来源的经验值
     * @param resourceType 资源类型
     * @return 经验数量，如果该类型不存在则返回 -1
     */
    public int getExperience(String resourceType) {
        return this.experienceSources.getOrDefault(resourceType, -1);
    }


    /**
     * 获取所有在线玩家
     * 
     * @return 在线玩家列表
     */
    public static List<GamePlayer> getOnlinePlayers() {
        List<GamePlayer> onlinePlayers = new ArrayList<>();
        for (GamePlayer player : GAME_PLAYERS.values()) {
            if (player.isOnline()) {
                onlinePlayers.add(player);
            }
        }
        return onlinePlayers;
    }

    /**
     * 获取所有观察者
     * 
     * @return 观察者列表
     */
    public static List<GamePlayer> getSpectators() {
        List<GamePlayer> spectators = new ArrayList<>();
        for (GamePlayer player : GAME_PLAYERS.values()) {
            if (player.isSpectator()) {
                spectators.add(player);
            }
        }
        return spectators;
    }

    /**
     * 按最终击杀数排序玩家
     * 
     * @return 排序后的玩家列表
     */
    public static List<GamePlayer> sortFinalKills() {
        List<GamePlayer> list = new ArrayList<>(getOnlinePlayers());
        list.sort((player1, player2) -> player2.getFinalKills() - player1.getFinalKills());
        return list;
    }

    /**
     * 获取Bukkit玩家实例
     * 
     * @return Bukkit玩家实例
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * 检查玩家是否在线
     * 
     * @return 是否在线
     */
    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    /**
     * 发送动作栏消息
     * 
     * @param message 消息内容
     */
    public void sendActionBar(String message) {
        if (!isOnline()) {
            return;
        }
        ActionBarUtil.sendBar(getPlayer(), message);
    }

    /**
     * 发送标题消息
     * 
     * @param fadeIn 淡入时间
     * @param stay 停留时间
     * @param fadeOut 淡出时间
     * @param title 主标题
     * @param subTitle 副标题
     */
    public void sendTitle(int fadeIn, int stay, int fadeOut, String title, String subTitle) {
        if (!isOnline()) {
            return;
        }
        TitleUtil.sendTitle(getPlayer(), fadeIn, stay, fadeOut, title, subTitle);
    }

    /**
     * 发送聊天消息
     * 
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        if (!isOnline()) {
            return;
        }
        getPlayer().sendMessage(message);
    }

    /**
     * 播放音效
     * 
     * @param sound 音效类型
     * @param volume 音量
     * @param pitch 音调
     */
    public void playSound(Sound sound, float volume, float pitch) {
        if (!isOnline()) {
            return;
        }
        getPlayer().playSound(getPlayer().getLocation(), sound, volume, pitch);
    }

    /**
     * 设置玩家为观察者
     */
    public void setSpectator() {
        spectator = true;
    }

    /**
     * 将玩家转换为观察者
     * 
     * @param title 主标题
     * @param subTitle 副标题
     */
    public void toSpectator(String title, String subTitle) {
        spectator = true;
        spectatorTarget = new SpectatorTarget(this, null);

        Player player = getPlayer();
        setupSpectatorPlayer(player, title, subTitle);
        setupSpectatorInventory(player);
        setupSpectatorEffects(player);
        setupSpectatorTarget();
    }

    /**
     * 设置观察者玩家状态
     */
    private void setupSpectatorPlayer(Player player, String title, String subTitle) {
        sendTitle(10, 20, 10, title, subTitle);
        for (GamePlayer gamePlayer1 : getOnlinePlayers()) {
            PlayerUtil.hidePlayer(gamePlayer1.getPlayer(), player);
        }
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        PlayerUtil.setFlying(player);
        player.teleport(AzuraBedWars.getInstance().getGameManager().getMapData().getRespawnLocation().toLocation());
    }

    /**
     * 设置观察者物品栏
     */
    private void setupSpectatorInventory(Player player) {
        player.getInventory().setItem(0, createSpectatorItem(XMaterial.COMPASS.get(), "§a§l传送器 §7(右键打开)"));
        player.getInventory().setItem(4, createSpectatorItem(XMaterial.COMPARATOR.get(), "§c§l旁观者设置 §7(右键打开)"));
        player.getInventory().setItem(7, createSpectatorItem(XMaterial.PAPER.get(), "§b§l快速加入 §7(右键加入)"));
        player.getInventory().setItem(8, createSpectatorItem(XMaterial.SLIME_BALL.get(), "§c§l离开游戏 §7(右键离开)"));
    }

    /**
     * 创建观察者物品
     */
    private ItemStack createSpectatorItem(Material material, String name) {
        return new ItemBuilder()
                .setType(material)
                .setDisplayName(name)
                .getItem();
    }

    /**
     * 设置观察者效果
     */
    private void setupSpectatorEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        SpectatorSettings spectatorSettings = SpectatorSettings.get(this);
        if (spectatorSettings.getOption(SpectatorSettings.Option.NIGHT_VISION)) {
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
        }
    }

    /**
     * 设置观察者目标
     */
    private void setupSpectatorTarget() {
        if (gameTeam != null && !gameTeam.getAlivePlayers().isEmpty()) {
            spectatorTarget.setTarget(gameTeam.getAlivePlayers().getFirst());
        }
    }

    /**
     * 增加击杀数
     */
    public void addKills() {
        kills++;
    }

    /**
     * 增加最终击杀数
     */
    public void addFinalKills() {
        finalKills++;
    }

    /**
     * 设置最后伤害来源
     * 
     * @param damager 伤害来源玩家
     * @param time 时间戳
     */
    public void setLastDamage(GamePlayer damager, long time) {
        assistsManager.setLastDamage(damager, time);
    }

    /**
     * 给予玩家初始装备
     */
    public void giveInventory() {
        Player player = getPlayer();
        player.getInventory().setHelmet(new ItemBuilder().setType(XMaterial.LEATHER_HELMET.get()).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
        player.getInventory().setChestplate(new ItemBuilder().setType(XMaterial.LEATHER_CHESTPLATE.get()).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
        giveArmor();
        giveSword(false);
        givePickaxe(false);
        giveAxe(false);
        giveShear();

        player.updateInventory();
    }

    public void giveArmor() {
        Player player = getPlayer();

        switch (armorType) {
            case CHAINMAIL:
                player.getInventory().setLeggings(new ItemBuilder().setType(XMaterial.CHAINMAIL_LEGGINGS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilder().setType(XMaterial.CHAINMAIL_BOOTS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
            case IRON:
                player.getInventory().setLeggings(new ItemBuilder().setType(XMaterial.IRON_LEGGINGS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilder().setType(XMaterial.IRON_BOOTS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
            case DIAMOND:
                player.getInventory().setLeggings(new ItemBuilder().setType(XMaterial.DIAMOND_LEGGINGS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilder().setType(XMaterial.DIAMOND_BOOTS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
            default:
                player.getInventory().setLeggings(new ItemBuilder().setType(XMaterial.LEATHER_LEGGINGS.get()).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilder().setType(XMaterial.LEATHER_BOOTS.get()).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
        }

        if (gameTeam.getReinforcedArmor() > 0) {
            for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
                player.getInventory().getArmorContents()[i].addEnchantment(XEnchantment.PROTECTION.get(), gameTeam.getReinforcedArmor());
            }
        }
    }

    public void giveSword(boolean remove) {
        Player player = getPlayer();

        if (remove) {
            if (XMaterial.WOODEN_SWORD.get() != null) {
                player.getInventory().remove(XMaterial.WOODEN_SWORD.get());
            }
        }
        if (gameTeam.isHasSharpenedEnchant()) {
            player.getInventory().addItem(new ItemBuilder().setType(XMaterial.WOODEN_SWORD.get()).addEnchant(XEnchantment.SHARPNESS.get(), 1).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setUnbreakable(true, true).getItem());
        } else {
            player.getInventory().addItem(new ItemBuilder().setType(XMaterial.WOODEN_SWORD.get()).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setUnbreakable(true, true).getItem());
        }
    }

    public void givePickaxe(boolean remove) {
        Player player = getPlayer();

        switch (pickaxeType) {
            case WOOD:
                player.getInventory().addItem(new ItemBuilder().setType(XMaterial.WOODEN_PICKAXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem());
                break;
            case STONE:
                if (remove) {
                    if (XMaterial.WOODEN_PICKAXE.get() != null) {
                        player.getInventory().remove(XMaterial.WOODEN_PICKAXE.get());
                    }
                }
                player.getInventory().addItem(new ItemBuilder().setType(XMaterial.STONE_PICKAXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem());
                break;
            case IRON:
                if (remove) {
                    if (XMaterial.STONE_PICKAXE.get() != null) {
                        player.getInventory().remove(XMaterial.STONE_PICKAXE.get());
                    }
                }
                player.getInventory().addItem(new ItemBuilder().setType(XMaterial.IRON_PICKAXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem());
                break;
            case DIAMOND:
                if (remove) {
                    if (XMaterial.IRON_PICKAXE.get() != null) {
                        player.getInventory().remove(XMaterial.IRON_PICKAXE.get());
                    }
                }
                player.getInventory().addItem(new ItemBuilder().setType(XMaterial.DIAMOND_PICKAXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem());
                break;
            default:
                break;
        }
    }

    public void giveAxe(boolean remove) {
        Player player = getPlayer();

        switch (axeType) {
            case WOOD:
                player.getInventory().addItem(new ItemBuilder().setType(XMaterial.WOODEN_AXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem());
                break;
            case STONE:
                if (remove) {
                    if (XMaterial.WOODEN_AXE.get() != null) {
                        player.getInventory().remove(XMaterial.WOODEN_AXE.get());
                    }
                }
                player.getInventory().addItem(new ItemBuilder().setType(XMaterial.STONE_AXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem());
                break;
            case IRON:
                if (remove) {
                    if (XMaterial.STONE_AXE.get() != null) {
                        player.getInventory().remove(XMaterial.STONE_AXE.get());
                    }
                }
                player.getInventory().addItem(new ItemBuilder().setType(XMaterial.IRON_AXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem());
                break;
            case DIAMOND:
                if (remove) {
                    if (XMaterial.IRON_AXE.get() != null) {
                        player.getInventory().remove(XMaterial.IRON_AXE.get());
                    }
                }
                player.getInventory().addItem(new ItemBuilder().setType(XMaterial.DIAMOND_AXE.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(XEnchantment.EFFICIENCY.get(), 1).getItem());
                break;
            default:
                break;
        }
    }

    public void giveShear() {
        Player player = getPlayer();

        if (shear) {
            player.getInventory().addItem(new ItemBuilder().setType(XMaterial.SHEARS.get()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
        }
    }
    /**
     * 清理玩家状态
     */
    public void cleanState() {
        Player player = getPlayer();
        resetPlayerState(player);
        clearInventory(player);
        clearEffects(player);
    }

    /**
     * 重置玩家状态
     */
    private void resetPlayerState(Player player) {
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
    }

    /**
     * 清空物品栏
     */
    private void clearInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.setArmorContents(new ItemStack[4]);
        inv.setContents(new ItemStack[]{});
    }

    /**
     * 清除效果
     */
    private void clearEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GamePlayer)) {
            return false;
        }
        return uuid.equals(((GamePlayer) obj).getUuid());
    }

    /**
     * 玩家指南针内部类
     */
    @Getter
    public static class PlayerCompass {
        private final GamePlayer gamePlayer;
        private final Player player;

        public PlayerCompass(GamePlayer gamePlayer) {
            this.gamePlayer = gamePlayer;
            this.player = gamePlayer.getPlayer();
        }

        /**
         * 发送最近玩家信息
         */
        public void sendClosestPlayer() {
            GamePlayer closestPlayer = AzuraBedWars.getInstance().getGameManager().findTargetPlayer(gamePlayer);

            if (closestPlayer != null) {
                int distance = (int) closestPlayer.getPlayer().getLocation().distance(player.getLocation());
                gamePlayer.sendActionBar(String.format("§f玩家 %s%s §f距离您 %dm",
                    closestPlayer.getGameTeam().getChatColor(),
                    closestPlayer.getNickName(),
                    distance));
                player.setCompassTarget(closestPlayer.getPlayer().getLocation());
            } else {
                gamePlayer.sendActionBar("§c没有目标");
            }
        }
    }
}
