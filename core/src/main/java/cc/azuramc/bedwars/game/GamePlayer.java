package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.config.object.PlayerConfig;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.service.PlayerDataService;
import cc.azuramc.bedwars.game.item.armor.ArmorType;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import cc.azuramc.bedwars.game.spectator.SpectatorManager;
import cc.azuramc.bedwars.game.spectator.SpectatorTarget;
import cc.azuramc.bedwars.util.LoggerUtil;
import cc.azuramc.bedwars.util.MessageUtil;
import cc.azuramc.bedwars.util.packet.ArmorHider;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏玩家类
 * <p>
 * 管理玩家在游戏中的状态、装备、数据等
 * 提供玩家相关的各种操作方法
 * <b>业务层统一只用GamePlayer，Player仅用于与Bukkit API交互</b>
 * </p>
 * @author an5w1r@163.com
 */
@Data
public class GamePlayer {

    private static final PlayerConfig.GamePlayer CONFIG = AzuraBedWars.getInstance().getPlayerConfig().getGamePlayer();

    public static final ConcurrentHashMap<UUID, GamePlayer> GAME_PLAYERS = new ConcurrentHashMap<>();
    private static final int MAX_HEALTH = CONFIG.getMaxHealth();
    private static final float MAX_SATURATION = 5.0f;
    private static final int MAX_FOOD_LEVEL = 20;

    private final UUID uuid;
    private final String name;
    private final AssistsManager assistsManager;
    private final PlayerData playerData;
    private final PlayerCompass playerCompass;

    GameModeType gameModeType;
    /** 使用 HashMap 存储经验来源，键是资源名(String)，值是经验数量(Integer) */
    Map<String, Integer> experienceSources;

    private String nickName;
    private FastBoard board;
    private boolean isSpectator;
    private SpectatorTarget spectatorTarget;
    private GameTeam gameTeam;
    private boolean isAfk;
    private boolean isRespawning;
    private boolean isShoutCooldown;
    private boolean isEggBridgeCooldown;
    private boolean isViewingArrowDamage;
    private boolean isViewingAttackDamage;

    // 隐身相关
    private boolean isInvisible;
    private BukkitRunnable invisibilityTask;

    // 装备状态
    private ArmorType armorType;
    private ToolType pickaxeType;
    private ToolType axeType;
    private boolean shear;

    private boolean isReconnect;

    // 本局游戏数据
    private int currentGameKills;
    private int currentGameFinalKills;
    private int currentGameAssists;
    private int currentGameDeaths;
    private int currentGameDestroyedBeds;

    // 陷阱免疫相关
    private boolean hasTrapProtection;
    private BukkitRunnable trapProtectionTask;

    // 陷阱触发冷却相关
    private boolean isTrapTriggerCooldown;
    private BukkitRunnable trapTriggerTask;

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
        this.playerData = loadPlayerData(this);
        this.playerCompass = new PlayerCompass(this);

        // 初始化游戏状态
        this.isAfk = false;
        this.isShoutCooldown = false;
        this.isEggBridgeCooldown = false;
        this.isViewingArrowDamage = true;
        this.isViewingAttackDamage = true;
        this.isInvisible = false;

        // 初始化装备状态
        this.armorType = ArmorType.DEFAULT;
        this.pickaxeType = ToolType.NONE;
        this.axeType = ToolType.NONE;

        this.isReconnect = false;

        // 初始化本局游戏数据
        this.currentGameKills = 0;
        this.currentGameFinalKills = 0;
        this.currentGameAssists = 0;
        this.currentGameDeaths = 0;
        this.currentGameDestroyedBeds = 0;

        // 游戏模式
        this.gameModeType = playerData.getMode();
        this.experienceSources = new HashMap<>();

        this.hasTrapProtection = false;

        this.isTrapTriggerCooldown = false;
    }

    /**
     * 从数据库加载玩家数据
     *
     * @return PlayerData实例
     */
    private PlayerData loadPlayerData(GamePlayer gamePlayer) {
        PlayerDataService playerDataService = AzuraBedWars.getInstance().getPlayerDataService();
        return playerDataService.selectPlayerData(gamePlayer);
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
        return GAME_PLAYERS.getOrDefault(uuid, null);
    }

    /**
     * 通过Bukkit Player对象获取GamePlayer实例
     * @param player Bukkit Player对象
     * @return GamePlayer实例
     */
    public static GamePlayer get(Player player) {
        if (player == null) {
            return null;
        }
        return get(player.getUniqueId());
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
     * 开始隐身任务
     */
    public void startInvisibilityTask() {
        endInvisibility();

        ArmorHider.hideArmor(this, GamePlayer.getGamePlayers());
        this.setInvisible(true);
        invisibilityTask = new BukkitRunnable() {
            @Override
            public void run() {
                sendMessage("&c隐身效果结束");
                endInvisibility();
            }
        };
        invisibilityTask.runTaskLater(AzuraBedWars.getInstance(), 30 * 20);
    }

    /**
     * 取消隐身任务
     */
    public void endInvisibility() {
        ArmorHider.showArmor(this, GamePlayer.getGamePlayers());
        if (this.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            this.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        }

        this.setInvisible(false);
        if (invisibilityTask != null) {
            invisibilityTask.cancel();
        }
    }

    /**
     * 开始陷阱免疫任务
     */
    public void startTrapProtectionTask() {
        endTrapProtection();

        this.setHasTrapProtection(true);
        trapProtectionTask = new BukkitRunnable() {
            @Override
            public void run() {
                sendMessage("&c魔法牛奶效果结束");
                endTrapProtection();
            }
        };
        trapProtectionTask.runTaskLater(AzuraBedWars.getInstance(), 30 * 20);
    }

    /**
     * 取消陷阱免疫任务
     */
    public void endTrapProtection() {
        this.setHasTrapProtection(false);
        if (trapProtectionTask != null) {
            trapProtectionTask.cancel();
        }
    }

    /**
     * 开始陷阱触发冷却任务
     */
    public void startTrapTriggerCooldownTask() {
        endTrapTriggerCooldownTask();

        this.setTrapTriggerCooldown(true);
        trapProtectionTask = new BukkitRunnable() {
            @Override
            public void run() {
                endTrapTriggerCooldownTask();
            }
        };
        trapProtectionTask.runTaskLater(AzuraBedWars.getInstance(), 8 * 20);
    }

    /**
     * 取消陷阱触发冷却
     */
    public void endTrapTriggerCooldownTask() {
        this.setTrapTriggerCooldown(false);
        if (trapProtectionTask != null) {
            trapProtectionTask.cancel();
        }
    }

    /**
     * 增加指定资源来源的经验值
     * @param resourceType 资源类型 (e.g., "iron")
     * @param amount       增加的数量
     */
    public void addResourceExperience(String resourceType, int amount) {
        if (amount <= 0) {
            LoggerUtil.warn("addResourceExperience 应该输入大于0的数值");
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
    public boolean spendResourceExperience(String resourceType, int amount) {
        if (amount <= 0) {
            LoggerUtil.warn("spendResourceExperience 应该输入大于0的数值");
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
     * 按最终击杀数排序玩家 (总数居)
     *
     * @return 排序后的玩家列表
     */
    public static List<GamePlayer> sortFinalKills() {
        List<GamePlayer> list = new ArrayList<>(getOnlinePlayers());
        list.sort((player1, player2) -> Integer.compare(player2.getPlayerData().getFinalKills(), player1.getPlayerData().getFinalKills()));
        return list;
    }

    /**
     * 按本局最终击杀数排序玩家
     *
     * @return 排序后的玩家列表
     */
    public static List<GamePlayer> sortCurrentGameFinalKills() {
        List<GamePlayer> list = new ArrayList<>(getOnlinePlayers());
        list.sort((player1, player2) -> Integer.compare(player2.getCurrentGameFinalKills(), player1.getCurrentGameFinalKills()));
        return list;
    }

    /**
     * 增加玩家本局击杀数据 (不建议直接调用它增加，playerData类的addKills等方法会触发一次这个方法)
     */
    public void addCurrentGameKills() {
        this.currentGameKills++;
    }

    /**
     * 增加玩家本局最终击杀数据 (不建议直接调用它增加，playerData类的addFinalKills等方法会触发一次这个方法)
     */
    public void addCurrentGameFinalKills() {
        this.currentGameFinalKills++;
    }

    /**
     * 增加玩家本局助攻数据 (不建议直接调用它增加，playerData类的addAssists等方法会触发一次这个方法)
     */
    public void addCurrentGameAssists() {
        this.currentGameAssists++;
    }

    /**
     * 增加玩家本局死亡数据 (不建议直接调用它增加，playerData类的addDeaths等方法会触发一次这个方法)
     */
    public void addCurrentGameDeaths() {
        this.currentGameDeaths++;
    }

    /**
     * 增加玩家本局拆床数据 (不建议直接调用它增加，playerData类的addDestroyedBeds等方法会触发一次这个方法)
     */
    public void addCurrentGameDestroyedBeds() {
        this.currentGameDestroyedBeds++;
    }

    /**
     * 增加玩家本局击杀数据 (不建议直接调用它增加，playerData类的addKills等方法会触发一次这个方法)
     */
    public void addCurrentGameKills(int currentGameKills) {
        this.currentGameKills += currentGameKills;
    }

    /**
     * 增加玩家本局最终击杀数据 (不建议直接调用它增加，playerData类的addFinalKills等方法会触发一次这个方法)
     */
    public void addCurrentGameFinalKills(int currentGameFinalKills) {
        this.currentGameFinalKills += currentGameFinalKills;
    }

    /**
     * 增加玩家本局助攻数据 (不建议直接调用它增加，playerData类的addAssists等方法会触发一次这个方法)
     */
    public void addCurrentGameAssists(int currentGameAssists) {
        this.currentGameAssists += currentGameAssists;
    }

    /**
     * 增加玩家本局死亡数据 (不建议直接调用它增加，playerData类的addDeaths等方法会触发一次这个方法)
     */
    public void addCurrentGameDeaths(int currentGameDeaths) {
        this.currentGameDeaths += currentGameDeaths;
    }

    /**
     * 增加玩家本局破坏床数据 (不建议直接调用它增加，playerData类的addDestroyedBeds等方法会触发一次这个方法)
     */
    public void addCurrentGameDestroyedBeds(int currentGameDestroyedBeds) {
        this.currentGameDestroyedBeds += currentGameDestroyedBeds;
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
     * 获取所有观察者
     *
     * @return 观察者列表
     */
    public static List<GamePlayer> getSpectators() {
        List<GamePlayer> spectators = new ArrayList<>();
        for (GamePlayer gamePlayer : GAME_PLAYERS.values()) {
            if (gamePlayer.isSpectator()) {
                spectators.add(gamePlayer);
            }
        }
        return spectators;
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
        MessageUtil.sendActionBar(getPlayer(), message);
    }

    /**
     * 发送标题消息
     *
     * @param title    主标题
     * @param subTitle 副标题
     * @param fadeIn   淡入时间
     * @param stay     停留时间
     * @param fadeOut  淡出时间
     */
    public void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (!isOnline()) {
            return;
        }
        MessageUtil.sendTitle(getPlayer(), title, subTitle, fadeIn, stay, fadeOut);
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
        getPlayer().sendMessage(MessageUtil.color(message));
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
        SpectatorManager.add(this);
    }

    /**
     * 设置最后伤害来源玩家
     *
     * @param damager 伤害来源玩家
     */
    public void setLastDamage(GamePlayer damager) {
        assistsManager.setLastDamage(damager, System.currentTimeMillis());
    }


    /**
     * 设置最后伤害来源玩家
     *
     * @param damager 伤害来源玩家
     * @param time 时间戳
     */
    public void setLastDamage(GamePlayer damager, long time) {
        assistsManager.setLastDamage(damager, time);
    }

    /**
     * 获取最后伤害来源玩家
     *
     * @return 伤害来源玩家
     */
    public GamePlayer getLastDamager() {
        List<GamePlayer> gamePlayers = assistsManager.getAssists();
        if (gamePlayers.isEmpty()) {
            return null;
        }
        return gamePlayers.get(gamePlayers.size() - 1);
    }

    /**
     * 获取最后伤害来源玩家列表
     *
     * @return 伤害来源玩家列表
     */
    public List<GamePlayer> getLastDamagerList() {
        return assistsManager.getAssists();
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

        if (gameTeam.getUpgradeManager().getProtectionUpgrade() > 0) {
            for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
                ItemStack armor = player.getInventory().getArmorContents()[i];
                if (armor != null) {
                    armor.addEnchantment(XEnchantment.PROTECTION.get(), gameTeam.getUpgradeManager().getProtectionUpgrade());
                    player.updateInventory();
                }
            }
        }

        if (gameTeam.getUpgradeManager().getFallingProtectionUpgrade() > 0) {
            ItemStack boots = player.getInventory().getArmorContents()[0];
            if (boots != null) {
                boots.addEnchantment(XEnchantment.FEATHER_FALLING.get(), gameTeam.getUpgradeManager().getFallingProtectionUpgrade());
                player.updateInventory();
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
        if (gameTeam.getUpgradeManager().hasSharpnessUpgrade()) {
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
        this.endInvisibility();
        this.endTrapProtection();
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
     * 刷新背包
     */
    public void updateInventory() {
        this.getPlayer().updateInventory();
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
    public class PlayerCompass {
        private final GamePlayer gamePlayer;

        public PlayerCompass(GamePlayer gamePlayer) {
            this.gamePlayer = gamePlayer;
        }

        /**
         * 发送最近玩家信息
         */
        public void sendClosestPlayer() {
            GamePlayer closestPlayer = AzuraBedWars.getInstance().getGameManager().findTargetPlayer(gamePlayer);

            if (closestPlayer != null) {
                LoggerUtil.debug("GamePlayer.PlayerCompass$sendClosestPlayer | closest player is " + closestPlayer);

                // 动态获取 Player 对象
                Player player = gamePlayer.getPlayer();
                if (player == null) {
                    LoggerUtil.debug("GamePlayer.PlayerCompass$sendClosestPlayer | player is null");
                    return;
                }

                // 获取目标玩家的 Player 对象
                Player targetPlayer = closestPlayer.getPlayer();
                if (targetPlayer == null) {
                    LoggerUtil.debug("GamePlayer.PlayerCompass$sendClosestPlayer | target player is null");
                    return;
                }

                int distance = (int) targetPlayer.getLocation().distance(player.getLocation());
                gamePlayer.sendActionBar(String.format("§f玩家 %s%s §f距离您 %dm",
                    closestPlayer.getGameTeam().getChatColor(),
                    closestPlayer.getNickName(),
                    distance));
                player.setCompassTarget(targetPlayer.getLocation());
            } else {
                gamePlayer.sendActionBar("§c没有目标");
            }
        }
    }
}
