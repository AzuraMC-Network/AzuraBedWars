package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.utils.ActionBarUtil;
import cc.azuramc.bedwars.compat.util.ItemBuilderUtil;
import cc.azuramc.bedwars.utils.TitleUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import cc.azuramc.bedwars.spectator.SpectatorTarget;
import cc.azuramc.bedwars.types.ArmorType;
import cc.azuramc.bedwars.types.ToolType;
import cc.azuramc.bedwars.utils.Util;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.compat.enchantment.EnchantmentUtil;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.utils.board.FastBoard;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏玩家类
 * <p>
 * 管理玩家在游戏中的状态、装备、数据等
 * 提供玩家相关的各种操作方法
 * </p>
 */
public class GamePlayer {
    // 静态字段
    private static final ConcurrentHashMap<UUID, GamePlayer> gamePlayers = new ConcurrentHashMap<>();
    private static final int MAX_HEALTH = 20;
    private static final float MAX_SATURATION = 5.0f;
    private static final int MAX_FOOD_LEVEL = 20;

    // 基础属性
    @Getter private final UUID uuid;
    @Getter private final String name;
    @Getter private final AssistsManager assistsManager;
    @Getter private final PlayerData playerData;
    @Getter private final PlayerCompass playerCompass;
    
    // 游戏状态
    @Setter private String nickName;
    @Getter @Setter private FastBoard board;
    @Getter private boolean spectator;
    @Getter @Setter private SpectatorTarget spectatorTarget;
    @Getter @Setter private GameTeam gameTeam;
    
    // 战斗数据
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
        
        // 初始化装备状态
        this.armorType = ArmorType.DEFAULT;
        this.pickaxeType = ToolType.NONE;
        this.axeType = ToolType.NONE;
        
        // 初始化管理器
        this.assistsManager = new AssistsManager(this);
        this.playerData = new PlayerData(this);
        this.playerCompass = new PlayerCompass(this);
    }

    /**
     * 创建或获取游戏玩家实例
     * 
     * @param uuid 玩家UUID
     * @param name 玩家名称
     * @return 游戏玩家实例
     */
    public static GamePlayer create(UUID uuid, String name) {
        return gamePlayers.computeIfAbsent(uuid, k -> new GamePlayer(uuid, name));
    }

    /**
     * 获取游戏玩家实例
     * 
     * @param uuid 玩家UUID
     * @return 游戏玩家实例
     */
    public static GamePlayer get(UUID uuid) {
        return gamePlayers.get(uuid);
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
        return new ArrayList<>(gamePlayers.values());
    }

    /**
     * 获取所有团队玩家
     * 
     * @return 团队玩家列表
     */
    public static List<GamePlayer> getTeamPlayers() {
        List<GamePlayer> teamPlayers = new ArrayList<>();
        for (GamePlayer player : gamePlayers.values()) {
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
        for (GamePlayer player : gamePlayers.values()) {
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
        for (GamePlayer player : gamePlayers.values()) {
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
        List<GamePlayer> sortedPlayers = new ArrayList<>(getOnlinePlayers());
        
        // 创建比较器
        Comparator<GamePlayer> finalKillsComparator = (player1, player2) -> {
            // 空值检查
            if (player1 == null && player2 == null) {
                return 0;
            }
            if (player1 == null) {
                return -1;
            }
            if (player2 == null) {
                return 1;
            }

            // 按最终击杀数降序排序
            int kills1 = player1.getFinalKills();
            int kills2 = player2.getFinalKills();
            return kills2 - kills1;
        };
        
        // 执行排序
        sortedPlayers.sort(finalKillsComparator);
        return sortedPlayers;
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
        if (!isOnline()) return;
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
        if (!isOnline()) return;
        TitleUtil.sendTitle(getPlayer(), fadeIn, stay, fadeOut, title, subTitle);
    }

    /**
     * 发送聊天消息
     * 
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        if (!isOnline()) return;
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
        if (!isOnline()) return;
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
        Util.setFlying(player);
        player.teleport(AzuraBedWars.getInstance().getGame().getMapData().getReSpawn().toLocation());
    }

    /**
     * 设置观察者物品栏
     */
    private void setupSpectatorInventory(Player player) {
        player.getInventory().setItem(0, createSpectatorItem(MaterialUtil.COMPASS(), "§a§l传送器§7(右键打开)"));
        player.getInventory().setItem(4, createSpectatorItem(MaterialUtil.COMPARATOR(), "§c§l旁观者设置§7(右键打开)"));
        player.getInventory().setItem(7, createSpectatorItem(Material.PAPER, "§b§l快速加入§7(右键加入)"));
        player.getInventory().setItem(8, createSpectatorItem(MaterialUtil.SLIME_BALL(), "§c§l离开游戏§7(右键离开)"));
    }

    /**
     * 创建观察者物品
     */
    private ItemStack createSpectatorItem(Material material, String name) {
        return new ItemBuilderUtil()
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
        if (spectatorSettings.getOption(SpectatorSettings.Option.NIGHTVISION)) {
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
        setupBaseArmor(player);
        giveArmor();
        giveSword(false);
        giveShear();
        player.updateInventory();
    }

    /**
     * 设置基础护甲
     */
    private void setupBaseArmor(Player player) {
        player.getInventory().setHelmet(createColoredArmor(MaterialUtil.LEATHER_HELMET()));
        player.getInventory().setChestplate(createColoredArmor(MaterialUtil.LEATHER_CHESTPLATE()));
    }

    /**
     * 创建染色护甲
     */
    private ItemStack createColoredArmor(Material material) {
        return new ItemBuilderUtil()
                .setType(material)
                .setColor(gameTeam.getColor())
                .setUnbreakable(true, true)
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .getItem();
    }

    /**
     * 给予护甲
     */
    public void giveArmor() {
        Player player = getPlayer();
        setupLeggingsAndBoots(player);
        applyReinforcedArmor(player);
    }

    /**
     * 设置护腿和靴子
     */
    private void setupLeggingsAndBoots(Player player) {
        switch (armorType) {
            case CHAINMAIL:
                player.getInventory().setLeggings(createUnbreakableArmor(MaterialUtil.CHAINMAIL_LEGGINGS()));
                player.getInventory().setBoots(createUnbreakableArmor(MaterialUtil.CHAINMAIL_BOOTS()));
                break;
            case IRON:
                player.getInventory().setLeggings(createUnbreakableArmor(MaterialUtil.IRON_LEGGINGS()));
                player.getInventory().setBoots(createUnbreakableArmor(MaterialUtil.IRON_BOOTS()));
                break;
            case DIAMOND:
                player.getInventory().setLeggings(createUnbreakableArmor(MaterialUtil.DIAMOND_LEGGINGS()));
                player.getInventory().setBoots(createUnbreakableArmor(MaterialUtil.DIAMOND_BOOTS()));
                break;
            default:
                player.getInventory().setLeggings(createColoredArmor(MaterialUtil.LEATHER_LEGGINGS()));
                player.getInventory().setBoots(createColoredArmor(MaterialUtil.LEATHER_BOOTS()));
                break;
        }
    }

    /**
     * 创建不可破坏的护甲
     */
    private ItemStack createUnbreakableArmor(Material material) {
        return new ItemBuilderUtil()
                .setType(material)
                .setUnbreakable(true, true)
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .getItem();
    }

    /**
     * 应用强化护甲效果
     */
    private void applyReinforcedArmor(Player player) {
        if (gameTeam.getReinforcedArmor() > 0) {
            for (ItemStack armor : player.getInventory().getArmorContents()) {
                if (armor != null) {
                    armor.addEnchantment(EnchantmentUtil.PROTECTION_ENVIRONMENTAL(), gameTeam.getReinforcedArmor());
                }
            }
        }
    }

    /**
     * 给予剑
     */
    public void giveSword(boolean remove) {
        Player player = getPlayer();
        if (remove) {
            player.getInventory().remove(MaterialUtil.WOODEN_SWORD());
        }
        
        ItemBuilderUtil builder = new ItemBuilderUtil()
                .setType(MaterialUtil.WOODEN_SWORD())
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .setUnbreakable(true, true);
                
        if (gameTeam.isSharpenedSwords()) {
            builder.addEnchant(EnchantmentUtil.DAMAGE_ALL(), 1);
        }
        
        player.getInventory().addItem(builder.getItem());
    }

    /**
     * 给予镐子
     */
    public void givePickaxe(boolean remove) {
        Player player = getPlayer();
        if (remove) {
            removePreviousPickaxe(player);
            return;
        }

        player.getInventory().addItem(createTool(Material.WOODEN_PICKAXE));
    }

    /**
     * 移除上一个镐子
     */
    private void removePreviousPickaxe(Player player) {
        switch (pickaxeType) {
            case DIAMOND:
                player.getInventory().remove(MaterialUtil.IRON_PICKAXE());
                break;
            case IRON:
                player.getInventory().remove(MaterialUtil.STONE_PICKAXE());
                break;
            case STONE:
                player.getInventory().remove(MaterialUtil.WOODEN_PICKAXE());
                break;
        }
    }

    /**
     * 给予斧头
     */
    public void giveAxe(boolean remove) {
        Player player = getPlayer();
        if (remove) {
            removePreviousAxe(player);
            return;
        }

        player.getInventory().addItem(createTool(MaterialUtil.WOODEN_AXE()));
    }

    /**
     * 移除上一个斧头
     */
    private void removePreviousAxe(Player player) {
        switch (axeType) {
            case DIAMOND:
                player.getInventory().remove(MaterialUtil.IRON_AXE());
                break;
            case IRON:
                player.getInventory().remove(MaterialUtil.STONE_AXE());
                break;
            case STONE:
                player.getInventory().remove(MaterialUtil.WOODEN_AXE());
                break;
        }
    }

    /**
     * 创建工具
     */
    private ItemStack createTool(Material material) {
        return new ItemBuilderUtil()
                .setType(material)
                .setUnbreakable(true, true)
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                .getItem();
    }

    /**
     * 给予剪刀
     */
    public void giveShear() {
        if (shear) {
            getPlayer().getInventory().addItem(new ItemBuilderUtil()
                    .setType(MaterialUtil.SHEARS())
                    .setUnbreakable(true, true)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .getItem());
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
            GamePlayer closestPlayer = AzuraBedWars.getInstance().getGame().findTargetPlayer(gamePlayer);

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
