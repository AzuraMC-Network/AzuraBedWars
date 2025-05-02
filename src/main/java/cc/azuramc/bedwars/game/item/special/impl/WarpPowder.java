package cc.azuramc.bedwars.game.item.special.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.config.object.ItemConfig;
import cc.azuramc.bedwars.config.object.MessageConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import cc.azuramc.bedwars.game.team.GameTeam;
import cc.azuramc.bedwars.compat.wrapper.MaterialWrapper;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * 传送粉末特殊物品
 * <p>
 * 允许玩家在原地等待几秒后传送回自己的基地
 * </p>
 * @author an5w1r@163.com
 */
public class WarpPowder extends AbstractSpecialItem {

    private static final ItemConfig.WarpPowder CONFIG = AzuraBedWars.getInstance().getItemConfig().getWarpPowder();
    private static final MessageConfig.WarpPowder MESSAGE_CONFIG = AzuraBedWars.getInstance().getMessageConfig().getWarpPowder();

    /** 默认传送时间（秒） */
    private static final int DEFAULT_TELEPORT_TIME = CONFIG.getDefaultTeleportTime();
    /** 每个粒子环的粒子数量 */
    private static final int CIRCLE_ELEMENTS = CONFIG.getCircleElements();
    /** 粒子环半径 */
    private static final double PARTICLE_RADIUS = CONFIG.getParticleRadius();
    /** 粒子效果总高度 */
    private static final double PARTICLE_HEIGHT = CONFIG.getParticleHeight();
    /** 粒子环的数量 */
    private static final double CIRCLE_COUNT = CONFIG.getCircleCount();

    /** 取消传送物品名称 */
    private static final String CANCEL_ITEM_NAME = CONFIG.getCancelItemName();
    private static final String TELEPORT_START_MESSAGE = MESSAGE_CONFIG.getTeleportStartMessage();
    private static final String TELEPORT_CANCEL_MESSAGE = MESSAGE_CONFIG.getTeleportCancelMessage();
    
    /** 完整传送时间 */
    private final int fullTeleportingTime;
    /** 游戏实例 */
    @Setter
    private GameManager gameManager;
    /** 玩家 */
    private GamePlayer gamePlayer;
    /** 原始物品栈 */
    @Getter
    private ItemStack stack;
    /** 传送任务 */
    private BukkitTask teleportingTask;
    /** 剩余传送时间 */
    private double teleportingTime;
    
    /**
     * 默认构造函数
     */
    public WarpPowder() {
        super();
        this.fullTeleportingTime = DEFAULT_TELEPORT_TIME;
        this.gameManager = AzuraBedWars.getInstance().getGameManager();
        this.gamePlayer = null;
        this.stack = null;
        this.teleportingTask = null;
        this.teleportingTime = DEFAULT_TELEPORT_TIME;
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param teleportTime 传送时间（秒）
     */
    public WarpPowder(int teleportTime) {
        super();
        this.fullTeleportingTime = teleportTime > 0 ? teleportTime : DEFAULT_TELEPORT_TIME;
        this.gameManager = AzuraBedWars.getInstance().getGameManager();
        this.gamePlayer = null;
        this.stack = null;
        this.teleportingTask = null;
        this.teleportingTime = this.fullTeleportingTime;
    }

    /**
     * 取消传送
     * 
     * @param removeSpecial 是否从游戏中移除此特殊物品
     * @param showMessage 是否显示取消消息
     */
    public void cancelTeleport(boolean removeSpecial, boolean showMessage) {
        if (gamePlayer == null || teleportingTask == null) {
            return;
        }
        
        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return;
        }

        // 取消任务并重置计时
        this.teleportingTask.cancel();
        this.teleportingTime = fullTeleportingTime;

        // 清除玩家等级显示
        player.setLevel(0);

        // 如果需要，从游戏中移除特殊物品
        if (removeSpecial && gameManager != null) {
            gameManager.removeSpecialItem(this);
        }

        // 显示消息
        if (showMessage) {
            gamePlayer.sendMessage(TELEPORT_CANCEL_MESSAGE);
        }

        // 恢复物品
        restorePlayerItem(player);
    }

    /**
     * 恢复玩家的物品
     * 
     * @param player 玩家
     */
    private void restorePlayerItem(Player player) {
        if (stack == null) {
            return;
        }
        
        // 减少物品数量
        setStackAmount(this.getStack().getAmount() - 1);
        
        // 查找并替换取消物品
        int cancelItemSlot = player.getInventory().first(getCancelItemStack());
        if (cancelItemSlot >= 0) {
            player.getInventory().setItem(cancelItemSlot, stack.getAmount() > 0 ? stack : null);
            player.updateInventory();
        }
    }

    @Override
    public Material getActivatedMaterial() {
        return MaterialWrapper.getMaterial("GLOWSTONE_DUST", "GLOWSTONE_DUST");
    }

    /**
     * 获取取消传送的物品
     * 
     * @return 取消传送物品
     */
    private ItemStack getCancelItemStack() {
        ItemStack glowstone = new ItemStack(this.getActivatedMaterial(), 1);
        ItemMeta meta = glowstone.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(CANCEL_ITEM_NAME);
            glowstone.setItemMeta(meta);
        }
        return glowstone;
    }

    @Override
    public Material getItemMaterial() {
        return MaterialWrapper.GUNPOWDER();
    }

    /**
     * 获取使用此物品的玩家
     * 
     * @return 玩家
     */
    public GamePlayer getPlayer() {
        return gamePlayer;
    }

    /**
     * 设置使用此物品的玩家
     * 
     * @param gamePlayer 玩家
     */
    public void setPlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    /**
     * 启动传送任务
     * 
     * @return 是否成功启动
     */
    public boolean runTask() {
        if (gamePlayer == null || gameManager == null) {
            return false;
        }
        
        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return false;
        }
        
        GameTeam gameTeam = gamePlayer.getGameTeam();
        if (gameTeam == null || gameTeam.getSpawnLocation() == null) {
            gamePlayer.sendMessage("§c无法找到你的队伍出生点!");
            return false;
        }

        // 保存原始物品并替换为取消物品
        stack = PlayerUtil.getItemInHand(player);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), this.getCancelItemStack());
        player.updateInventory();

        // 重置传送时间
        teleportingTime = fullTeleportingTime;
        
        // 发送开始传送消息
        gamePlayer.sendMessage(String.format(TELEPORT_START_MESSAGE, this.fullTeleportingTime));
        
        // 播放开始传送音效
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        // 计算每个tick减少的时间
        final double perThrough = (Math.ceil((PARTICLE_HEIGHT / CIRCLE_COUNT) * ((fullTeleportingTime * 20) / CIRCLE_COUNT)) / 20);
        
        // 创建传送任务
        this.teleportingTask = new BukkitRunnable() {
            public double through = 0.0;

            @Override
            public void run() {
                try {
                    // 减少剩余传送时间
                    WarpPowder.this.teleportingTime = teleportingTime - perThrough;
                    
                    // 获取目标位置
                    GameTeam team = gamePlayer.getGameTeam();
                    if (team == null) {
                        cancel();
                        WarpPowder.this.cancelTeleport(true, true);
                        return;
                    }
                    
                    Location targetLoc = team.getSpawnLocation();
                    if (targetLoc == null) {
                        cancel();
                        WarpPowder.this.cancelTeleport(true, true);
                        return;
                    }

                    // 传送完成
                    if (WarpPowder.this.teleportingTime <= 1.0) {
                        player.teleport(targetLoc);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                        WarpPowder.this.cancelTeleport(true, false);
                        return;
                    }

                    // 更新玩家等级显示剩余时间
                    player.setLevel((int) WarpPowder.this.teleportingTime);

                    // 生成粒子效果
                    createTeleportParticles(player.getLocation(), targetLoc, through);
                    
                    // 增加计数器
                    this.through += 1.0;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.cancel();
                    WarpPowder.this.cancelTeleport(true, true);
                }
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), 0L,
                (long) Math.ceil((PARTICLE_HEIGHT / CIRCLE_COUNT) * ((this.fullTeleportingTime * 20) / CIRCLE_COUNT)));
        
        // 添加到游戏中的特殊物品列表
        this.gameManager.addSpecialItem(this);
        return true;
    }
    
    /**
     * 创建传送粒子效果
     * 
     * @param fromLoc 起始位置
     * @param toLoc 目标位置
     * @param through 当前高度计数
     */
    private void createTeleportParticles(Location fromLoc, Location toLoc, double through) {
        double y = (PARTICLE_HEIGHT / CIRCLE_COUNT) * through;
        
        for (int i = 0; i < CIRCLE_ELEMENTS; i++) {
            double alpha = (360.0 / CIRCLE_ELEMENTS) * i;
            double x = PARTICLE_RADIUS * Math.sin(Math.toRadians(alpha));
            double z = PARTICLE_RADIUS * Math.cos(Math.toRadians(alpha));

            // 在玩家位置生成粒子
            Location particleFrom = new Location(fromLoc.getWorld(), fromLoc.getX() + x, fromLoc.getY() + y, fromLoc.getZ() + z);
            spawnParticle(GamePlayer.getOnlinePlayers(), particleFrom);

            // 在目标位置生成粒子
            if (toLoc.getWorld() != null) {
                Location particleTo = new Location(toLoc.getWorld(), toLoc.getX() + x, toLoc.getY() + y, toLoc.getZ() + z);
                spawnParticle(GamePlayer.getOnlinePlayers(), particleTo);
            }
        }
    }

    /**
     * 设置物品堆叠数量
     * 
     * @param amount 数量
     */
    public void setStackAmount(int amount) {
        if (this.stack != null) {
            this.stack.setAmount(Math.max(0, amount));
        }
    }
    
    /**
     * 检查玩家是否正在传送中
     * 
     * @return 是否传送中
     */
    public boolean isTeleporting() {
        return this.teleportingTask != null && !this.teleportingTask.isCancelled();
    }
    
    /**
     * 获取剩余传送时间
     * 
     * @return 剩余时间（秒）
     */
    public double getRemainingTime() {
        return this.teleportingTime;
    }

    public void spawnParticle(List<GamePlayer> gamePlayers, Location loc) {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getModifier().writeDefaults();
        packet.getParticles().write(0, EnumWrappers.Particle.FIREWORKS_SPARK);
        packet.getBooleans().write(0, false);
        packet.getFloat().write(0, (float) loc.getX());
        packet.getFloat().write(1, (float) loc.getY());
        packet.getFloat().write(2, (float) loc.getZ());
        packet.getFloat().write(3, 0.0F);
        packet.getFloat().write(4, 0.0F);
        packet.getFloat().write(5, 0.0F);
        packet.getFloat().write(6, 0.0F);
        packet.getIntegers().write(0, 1);
        gamePlayers.forEach(gamePlayer -> protocolManager.sendServerPacket(gamePlayer.getPlayer(), packet));
    }
}
