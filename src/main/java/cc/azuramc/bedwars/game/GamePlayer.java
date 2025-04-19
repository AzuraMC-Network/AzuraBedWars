package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.compat.util.ActionBarUtil;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.util.TitleUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.profile.PlayerProfile;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import cc.azuramc.bedwars.spectator.SpectatorTarget;
import cc.azuramc.bedwars.game.item.armor.ArmorType;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import cc.azuramc.bedwars.compat.wrapper.EnchantmentWrapper;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.scoreboard.base.FastBoard;
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
    @Getter private final PlayerProfile playerProfile;
    @Getter private final PlayerCompass playerCompass;
    
    // 游戏状态
    @Setter private String nickName;
    @Getter @Setter private FastBoard board;
    @Getter private boolean spectator;
    @Getter @Setter private SpectatorTarget spectatorTarget;
    @Getter @Setter private GameTeam gameTeam;
    @Getter private boolean afk;
    
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

        // 初始化 AFK 状态
        this.afk = false;

        // 初始化装备状态
        this.armorType = ArmorType.DEFAULT;
        this.pickaxeType = ToolType.NONE;
        this.axeType = ToolType.NONE;
        
        // 初始化管理器
        this.assistsManager = new AssistsManager(this);
        this.playerProfile = new PlayerProfile(this);
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
     * 设置挂机状态
     *
     * @param state 目标状态
     * */
    public void setAFK(boolean state) {
        this.afk = state;
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
        player.teleport(AzuraBedWars.getInstance().getGameManager().getMapData().getReSpawn().toLocation());
    }

    /**
     * 设置观察者物品栏
     */
    private void setupSpectatorInventory(Player player) {
        player.getInventory().setItem(0, createSpectatorItem(MaterialWrapper.COMPASS(), "§a§l传送器 §7(右键打开)"));
        player.getInventory().setItem(4, createSpectatorItem(MaterialWrapper.COMPARATOR(), "§c§l旁观者设置 §7(右键打开)"));
        player.getInventory().setItem(7, createSpectatorItem(Material.PAPER, "§b§l快速加入 §7(右键加入)"));
        player.getInventory().setItem(8, createSpectatorItem(MaterialWrapper.SLIME_BALL(), "§c§l离开游戏 §7(右键离开)"));
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
        player.getInventory().setHelmet(new ItemBuilder().setType(Material.LEATHER_HELMET).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
        player.getInventory().setChestplate(new ItemBuilder().setType(Material.LEATHER_CHESTPLATE).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
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
                player.getInventory().setLeggings(new ItemBuilder().setType(Material.CHAINMAIL_LEGGINGS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilder().setType(Material.CHAINMAIL_BOOTS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
            case IRON:
                player.getInventory().setLeggings(new ItemBuilder().setType(Material.IRON_LEGGINGS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilder().setType(Material.IRON_BOOTS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
            case DIAMOND:
                player.getInventory().setLeggings(new ItemBuilder().setType(Material.DIAMOND_LEGGINGS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilder().setType(Material.DIAMOND_BOOTS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
            default:
                player.getInventory().setLeggings(new ItemBuilder().setType(Material.LEATHER_LEGGINGS).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilder().setType(Material.LEATHER_BOOTS).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
        }

        if (gameTeam.getReinforcedArmor() > 0) {
            for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
                player.getInventory().getArmorContents()[i].addEnchantment(EnchantmentWrapper.PROTECTION_ENVIRONMENTAL(), gameTeam.getReinforcedArmor());
            }
        }
    }

    public void giveSword(boolean remove) {
        Player player = getPlayer();

        if (remove) {
            player.getInventory().remove(MaterialWrapper.WOODEN_SWORD());
        }
        if (gameTeam.isHasSharpenedEnchant()) {
            player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.WOODEN_SWORD()).addEnchant(EnchantmentWrapper.DAMAGE_ALL(), 1).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setUnbreakable(true, true).getItem());
        } else {
            player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.WOODEN_SWORD()).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).setUnbreakable(true, true).getItem());
        }
    }

    public void givePickaxe(boolean remove) {
        Player player = getPlayer();

        switch (pickaxeType) {
            case WOOD:
                player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.WOODEN_PICKAXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem());
                break;
            case STONE:
                if (remove) player.getInventory().remove(MaterialWrapper.WOODEN_PICKAXE());
                player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.STONE_PICKAXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem());
                break;
            case IRON:
                if (remove) player.getInventory().remove(MaterialWrapper.STONE_PICKAXE());
                player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.IRON_PICKAXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem());
                break;
            case DIAMOND:
                if (remove) player.getInventory().remove(MaterialWrapper.IRON_PICKAXE());
                player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.DIAMOND_PICKAXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem());
                break;
            default:
                break;
        }
    }

    public void giveAxe(boolean remove) {
        Player player = getPlayer();

        switch (axeType) {
            case WOOD:
                player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.WOODEN_AXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem());
                break;
            case STONE:
                if (remove) player.getInventory().remove(MaterialWrapper.WOODEN_AXE());
                player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.STONE_AXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem());
                break;
            case IRON:
                if (remove) player.getInventory().remove(MaterialWrapper.STONE_AXE());
                player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.IRON_AXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem());
                break;
            case DIAMOND:
                if (remove) player.getInventory().remove(MaterialWrapper.IRON_AXE());
                player.getInventory().addItem(new ItemBuilder().setType(MaterialWrapper.DIAMOND_AXE()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).addEnchant(EnchantmentWrapper.DIG_SPEED(), 1).getItem());
                break;
            default:
                break;
        }
    }

    public void giveShear() {
        Player player = getPlayer();

        if (shear) {
            player.getInventory().addItem(new ItemBuilder().setType(Material.SHEARS).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
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
