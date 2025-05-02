package cc.azuramc.bedwars.game.item.special.impl;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.config.object.ItemConfig;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.item.special.AbstractSpecialItem;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 救援平台特殊物品
 * 创建一个临时平台帮助玩家防止坠落
 *
 * @author an5w1r@163.com
 */
public class RescuePlatform extends AbstractSpecialItem {

    private static final ItemConfig CONFIG = AzuraBedWars.getInstance().getItemConfig();

    /**
     * 默认平台存在时间（秒）
     */
    private static final int DEFAULT_BREAK_TIME = CONFIG.getRescuePlatform().getDefaultBreakTime();
    /**
     * 默认使用冷却时间（秒）
     */
    private static final int DEFAULT_WAIT_TIME = CONFIG.getRescuePlatform().getDefaultWaitTime();
    /**
     * 跳跃提升力度
     */
    private static final double JUMP_BOOST = CONFIG.getRescuePlatform().getJumpBoost();
    /**
     * 平台方块材质
     */
    private static final Material PLATFORM_MATERIAL = Material.SLIME_BLOCK;


    @Getter private GameManager gameManager;
    @Getter private int livingTime = 0;
    private GamePlayer ownerPlayer;
    private final List<Block> platformBlocks;

    private int breakTime = DEFAULT_BREAK_TIME;
    private int waitTime = DEFAULT_WAIT_TIME;

    /**
     * 构造函数
     */
    public RescuePlatform() {
        super();
        this.platformBlocks = new ArrayList<>();
        this.gameManager = null;
        this.ownerPlayer = null;
    }

    /**
     * 添加平台方块到列表中
     * 
     * @param block 方块
     */
    public void addPlatformBlock(Block block) {
        this.platformBlocks.add(block);
    }

    /**
     * 创建救援平台
     * 
     * @param gamePlayer 游戏玩家
     * @param gameManager 游戏实例
     * @return 是否成功创建
     */
    public boolean create(GamePlayer gamePlayer, GameManager gameManager) {
        this.gameManager = gameManager;
        this.ownerPlayer = gamePlayer;
        Player player = ownerPlayer.getPlayer();
        
        if (player == null) {
            return false;
        }

        // 检查冷却时间
        if (!checkCooldown(gamePlayer)) {
            return false;
        }
        
        // 检查玩家是否在空中
        if (!isPlayerInAir(player)) {
            gamePlayer.sendMessage("§c你不在空气中!");
            return false;
        }

        // 创建平台
        if (!createPlatform(player)) {
            return false;
        }
        
        // 消耗物品
        consumeItem(player);
        
        // 提升玩家向上的速度
        boostPlayerUp(player);
        
        // 启动任务
        this.runTask(breakTime, waitTime);
        gameManager.addSpecialItem(this);
        return true;
    }
    
    /**
     * 检查使用冷却
     * 
     * @param gamePlayer 游戏玩家
     * @return 是否可以使用
     */
    private boolean checkCooldown(GamePlayer gamePlayer) {
        List<RescuePlatform> livingPlatforms = this.getLivingPlatforms();
        if (!livingPlatforms.isEmpty()) {
            for (RescuePlatform livingPlatform : livingPlatforms) {
                int waitLeft = waitTime - livingPlatform.getLivingTime();
                if (waitLeft > 0) {
                    gamePlayer.sendMessage("§c需要 §e" + waitLeft + "秒§c 你才能使用下一个救援平台!");
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 检查玩家是否在空中
     * 
     * @param player 玩家
     * @return 是否在空中
     */
    private boolean isPlayerInAir(Player player) {
        return player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR;
    }
    
    /**
     * 创建平台
     * 
     * @param player 玩家
     * @return 是否成功创建
     */
    private boolean createPlatform(Player player) {
        Location mid = player.getLocation().clone();
        mid.setY(mid.getY() - 1.0D);
        
        // 获取水平方向的方块面
        BlockFace[] horizontalFaces = {
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST,
            BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_WEST
        };
        
        boolean anyBlockPlaced = false;
        
        // 放置周围的方块
        for (BlockFace face : horizontalFaces) {
            Block placed = mid.getBlock().getRelative(face);
            if (placed.getType() != Material.AIR) {
                continue;
            }
            
            placed.setType(PLATFORM_MATERIAL);
            this.addPlatformBlock(placed);
            anyBlockPlaced = true;
        }
        
        // 放置中间的方块
        if (mid.getBlock().getType() == Material.AIR) {
            mid.getBlock().setType(PLATFORM_MATERIAL);
            this.addPlatformBlock(mid.getBlock());
            anyBlockPlaced = true;
        }
        
        return anyBlockPlaced;
    }
    
    /**
     * 消耗玩家手中的物品
     * 
     * @param player 玩家
     */
    private void consumeItem(Player player) {
        ItemStack usedStack = PlayerUtil.getItemInHand(player);
        if (usedStack.getAmount() > 1) {
            usedStack.setAmount(usedStack.getAmount() - 1);
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
        } else {
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), null);
        }
        player.updateInventory();
    }
    
    /**
     * 提升玩家向上的速度
     * 
     * @param player 玩家
     */
    private void boostPlayerUp(Player player) {
        Vector vector = player.getLocation().getDirection();
        vector.setY(vector.getY() + JUMP_BOOST);
        player.getLocation().setDirection(vector);
    }

    @Override
    public Material getActivatedMaterial() {
        return null;
    }

    @Override
    public Material getItemMaterial() {
        return Material.BLAZE_ROD;
    }

    /**
     * 获取当前存活的救援平台
     * 
     * @return 救援平台列表
     */
    private List<RescuePlatform> getLivingPlatforms() {
        if (gameManager == null) {
            return new ArrayList<>();
        }
        
        return gameManager.getAbstractSpecialItems().stream()
                .filter(item -> item instanceof RescuePlatform)
                .map(item -> (RescuePlatform) item)
                .filter(platform -> platform.getOwner() != null && platform.getOwner().equals(this.getOwner()))
                .collect(Collectors.toList());
    }

    /**
     * 设置平台存在时间
     * 
     * @param breakTime 存在时间（秒）
     */
    public void setBreakTime(int breakTime) {
        if (breakTime > 0) {
            this.breakTime = breakTime;
        }
    }
    
    /**
     * 设置使用冷却时间
     * 
     * @param waitTime 冷却时间（秒）
     */
    public void setWaitTime(int waitTime) {
        if (waitTime > 0) {
            this.waitTime = waitTime;
        }
    }

    /**
     * 获取平台所有者
     * 
     * @return 所有者
     */
    public GamePlayer getOwner() {
        return this.ownerPlayer;
    }

    /**
     * 启动平台管理任务
     * 
     * @param breakTime 存在时间
     * @param waitTime 冷却时间
     */
    public void runTask(final int breakTime, final int waitTime) {
        new BukkitRunnable() {
            @Override
            public void run() {
                RescuePlatform.this.livingTime++;

                // 到达销毁时间时移除平台方块
                if (breakTime > 0 && RescuePlatform.this.livingTime == breakTime) {
                    removePlatform();
                }

                // 达到总生命周期后移除特殊物品
                if (RescuePlatform.this.livingTime >= waitTime
                        && RescuePlatform.this.livingTime >= breakTime) {
                    if (RescuePlatform.this.gameManager != null) {
                        RescuePlatform.this.gameManager.removeSpecialItem(RescuePlatform.this);
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), 20L, 20L);
    }
    
    /**
     * 移除平台方块
     */
    public void removePlatform() {
        for (Block block : this.platformBlocks) {
            if (block.getType() == PLATFORM_MATERIAL) {
                block.setType(Material.AIR);
            }
        }
        this.platformBlocks.clear();
    }
    
    /**
     * 强制移除平台
     */
    public void forceRemove() {
        removePlatform();
        if (this.gameManager != null) {
            this.gameManager.removeSpecialItem(this);
        }
    }
}
