package cc.azuramc.bedwars.game.team;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Data;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 游戏团队管理类
 * <p>
 * 负责管理游戏中的队伍，包括队伍颜色、出生点、床位置和队伍升级等信息
 * </p>
 */
@Data
public class GameTeam {
    // 搜索床的范围
    private static final int BED_SEARCH_RADIUS = 18;
    // 默认床朝向
    private static final BlockFace DEFAULT_BED_FACE = BlockFace.NORTH;
    
    // 基本团队信息
    private final TeamColor teamColor;
    private final Location spawnLocation;
    private int maxPlayers;
    
    // 床相关属性
    private Block bedFeet;
    private Block bedHead;
    private BlockFace bedFace;
    private boolean hasBed;
    private boolean isDestroyed;
    private GamePlayer destroyPlayer;

    // 团队升级属性
    private int forge;
    private int manicMiner;
    private boolean hasSharpenedEnchant;
    private int reinforcedArmor;
    private boolean hasHealPool;
    private boolean hasTrap;
    private boolean hasMiner;
    private boolean hasAlarmTrap;

    /**
     * 创建一个游戏团队
     *
     * @param teamColor 团队颜色
     * @param location 出生点位置
     * @param maxPlayers 最大玩家数
     */
    public GameTeam(TeamColor teamColor, Location location, int maxPlayers) {
        this.teamColor = Objects.requireNonNull(teamColor, "团队颜色不能为空");
        this.spawnLocation = Objects.requireNonNull(location, "出生点位置不能为空");
        this.maxPlayers = maxPlayers;
        
        // 初始化默认值
        initializeDefaults();
        
        // 初始化床相关字段
        initializeBed();
    }
    
    /**
     * 初始化团队默认属性值
     */
    private void initializeDefaults() {
        // 床状态初始化
        this.hasBed = false;
        this.isDestroyed = false;
        
        // 团队升级初始化
        this.hasSharpenedEnchant = false;
        this.reinforcedArmor = 0;
        this.manicMiner = 0;
        this.forge = 0;
        this.hasMiner = false;
        this.hasHealPool = false;
        this.hasTrap = false;
        this.hasAlarmTrap = false;
    }
    
    /**
     * 初始化床相关字段
     */
    private void initializeBed() {
        // 查找床方块
        List<Block> bedBlocks = findBedBlocks();
        
        if (bedBlocks.size() >= 2) {
            determineBedParts(bedBlocks.get(0), bedBlocks.get(1));
        } else {
            // 如果找不到足够的床方块，初始化为默认值
            setDefaultBedValues();
        }
    }
    
    /**
     * 查找团队出生点附近的床方块
     * 
     * @return 床方块列表
     */
    private List<Block> findBedBlocks() {
        List<Block> bedBlocks = new ArrayList<>();
        
        for (int x = -BED_SEARCH_RADIUS; x < BED_SEARCH_RADIUS; x++) {
            for (int y = -BED_SEARCH_RADIUS; y < BED_SEARCH_RADIUS; y++) {
                for (int z = -BED_SEARCH_RADIUS; z < BED_SEARCH_RADIUS; z++) {
                    Block block = spawnLocation.clone().add(x, y, z).getBlock();
                    if (isBedBlock(block)) {
                        bedBlocks.add(block);
                        
                        // 只需要找到2个床方块即可返回
                        if (bedBlocks.size() >= 2) {
                            return bedBlocks;
                        }
                    }
                }
            }
        }
        
        return bedBlocks;
    }
    
    /**
     * 确定床的头部和脚部，以及朝向
     * 
     * @param block1 第一个床方块
     * @param block2 第二个床方块
     */
    private void determineBedParts(Block block1, Block block2) {
        if (!VersionUtil.isLessThan113()) {
            determineBedsForNewVersions(block1, block2);
        } else {
            determineBedsForOldVersions(block1, block2);
        }
    }
    
    /**
     * 为1.13+版本确定床的头部和脚部
     * 
     * @param block1 第一个床方块
     * @param block2 第二个床方块
     */
    private void determineBedsForNewVersions(Block block1, Block block2) {
        try {
            org.bukkit.block.data.type.Bed bed1 = (org.bukkit.block.data.type.Bed) block1.getBlockData();
            org.bukkit.block.data.type.Bed bed2 = (org.bukkit.block.data.type.Bed) block2.getBlockData();
            
            if (bed1.getPart() == org.bukkit.block.data.type.Bed.Part.HEAD) {
                this.bedHead = block1;
                this.bedFeet = block2;
                this.bedFace = bed1.getFacing();
            } else {
                this.bedFeet = block1;
                this.bedHead = block2;
                this.bedFace = bed2.getFacing();
            }
        } catch (Exception e) {
            // 如果出错，使用默认值
            setFallbackBedValues(block1, block2);
        }
    }
    
    /**
     * 为1.8-1.12版本确定床的头部和脚部
     * 
     * @param block1 第一个床方块
     * @param block2 第二个床方块
     */
    @SuppressWarnings("deprecation")
    private void determineBedsForOldVersions(Block block1, Block block2) {
        // 首先尝试使用反射方法安全获取Bed数据
        try {
            // 获取方块状态和数据
            BlockState state1 = block1.getState();
            BlockState state2 = block2.getState();
            
            // 检查是否可以获取MaterialData
            if (state1.getData() instanceof org.bukkit.material.Bed bedData1 &&
                    state2.getData() instanceof org.bukkit.material.Bed bedData2) {

                if (bedData1.isHeadOfBed()) {
                    this.bedHead = block1;
                    this.bedFeet = block2;
                    this.bedFace = bedData1.getFacing();
                    return;
                } else if (bedData2.isHeadOfBed()) {
                    this.bedFeet = block1;
                    this.bedHead = block2;
                    this.bedFace = bedData2.getFacing();
                    return;
                }
            }
            
            // 如果上述方法无法确定床头床脚，尝试使用方块类型名称判断
            String type1 = block1.getType().name();
            String type2 = block2.getType().name();
            
            // 某些版本使用BED_BLOCK，有些版本会在名称中包含HEAD或FOOT
            if (type1.contains("HEAD") || state1.getData().toString().contains("HEAD")) {
                this.bedHead = block1;
                this.bedFeet = block2;
                // 尝试从方块朝向判断床的朝向
                this.bedFace = getBedFacingFromBlock(block1);
                return;
            } else if (type2.contains("HEAD") || state2.getData().toString().contains("HEAD")) {
                this.bedHead = block2;
                this.bedFeet = block1;
                this.bedFace = getBedFacingFromBlock(block2);
                return;
            }
            
            // 如果依然无法确定，基于方块的相对位置进行猜测
            if (areBlocksAligned(block1, block2)) {
                // 简单赋值，保证有值
                this.bedHead = block1;
                this.bedFeet = block2;
                this.bedFace = getEstimatedFacing(block1, block2);
                return;
            }
            
            // 所有方法都失败，使用默认值
            setFallbackBedValues(block1, block2);
        } catch (Exception e) {
            // 如果出错，使用默认值
            setFallbackBedValues(block1, block2);
        }
    }
    
    /**
     * 从方块状态尝试获取床的朝向
     * 
     * @param block 床方块
     * @return 床的朝向，如果无法确定则返回默认朝向
     */
    private BlockFace getBedFacingFromBlock(Block block) {
        try {
            // 尝试从方块数据中获取朝向信息
            BlockState state = block.getState();
            if (state.getData() instanceof org.bukkit.material.Directional) {
                return ((org.bukkit.material.Directional) state.getData()).getFacing();
            }
            
            // 尝试使用9x和8x服务器的朝向数据
            byte data = block.getData();
            // 0: 头朝南, 1: 头朝西, 2: 头朝北, 3: 头朝东
            return switch (data & 0x3) {
                case 0 -> BlockFace.SOUTH;
                case 1 -> BlockFace.WEST;
                case 2 -> BlockFace.NORTH;
                case 3 -> BlockFace.EAST;
                default -> DEFAULT_BED_FACE;
            };
        } catch (Exception e) {
            return DEFAULT_BED_FACE;
        }
    }
    
    /**
     * 检查两个方块是否在同一水平线上
     * 
     * @param block1 第一个方块
     * @param block2 第二个方块
     * @return 如果两个方块在同一水平线上返回true
     */
    private boolean areBlocksAligned(Block block1, Block block2) {
        return block1.getY() == block2.getY() && 
               (Math.abs(block1.getX() - block2.getX()) == 1 && block1.getZ() == block2.getZ() || 
                Math.abs(block1.getZ() - block2.getZ()) == 1 && block1.getX() == block2.getX());
    }
    
    /**
     * 根据两个方块的相对位置估计床的朝向
     * 
     * @param block1 第一个方块
     * @param block2 第二个方块
     * @return 估计的床朝向
     */
    private BlockFace getEstimatedFacing(Block block1, Block block2) {
        if (block1.getX() < block2.getX()) {
            return BlockFace.EAST;
        }
        if (block1.getX() > block2.getX()) {
            return BlockFace.WEST;
        }
        if (block1.getZ() < block2.getZ()) {
            return BlockFace.SOUTH;
        }
        if (block1.getZ() > block2.getZ()) {
            return BlockFace.NORTH;
        }
        return DEFAULT_BED_FACE;
    }
    
    /**
     * 当无法确定床头床脚时的备用方法
     * 
     * @param block1 第一个床方块
     * @param block2 第二个床方块
     */
    private void setFallbackBedValues(Block block1, Block block2) {
        this.bedHead = block1;
        this.bedFeet = block2;
        this.bedFace = DEFAULT_BED_FACE;
    }
    
    /**
     * 当找不到床时设置默认值
     */
    private void setDefaultBedValues() {
        this.bedHead = spawnLocation.getBlock();
        this.bedFeet = spawnLocation.getBlock();
        this.bedFace = DEFAULT_BED_FACE;
    }
    
    /**
     * 判断方块是否为床方块，兼容新旧版本
     * 
     * @param block 待检查的方块
     * @return 如果是床方块返回true，否则返回false
     */
    private boolean isBedBlock(Block block) {
        if (!VersionUtil.isLessThan113()) {
            // 1.13+版本，床拆分为多种颜色的床
            return block.getType().name().endsWith("_BED");
        } else {
            // 1.8-1.12版本，使用BED_BLOCK
            try {
                return block.getType() == Material.valueOf("BED_BLOCK");
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    /**
     * 获取团队聊天颜色
     * 
     * @return 团队对应的聊天颜色
     */
    public ChatColor getChatColor() {
        return teamColor.getChatColor();
    }

    /**
     * 获取团队染料颜色
     * 
     * @return 团队对应的染料颜色
     */
    public DyeColor getDyeColor() {
        return teamColor.getDyeColor();
    }

    /**
     * 获取团队颜色对象
     * 
     * @return 团队对应的颜色对象
     */
    public Color getColor() {
        return teamColor.getColor();
    }

    /**
     * 获取团队名称
     * 
     * @return 团队名称
     */
    public String getName() {
        return teamColor.getName();
    }

    /**
     * 获取团队中所有玩家
     * 
     * @return 团队玩家列表
     */
    public List<GamePlayer> getGamePlayers() {
        List<GamePlayer> teamPlayers = new ArrayList<>();
        
        for (GamePlayer gamePlayer : GamePlayer.getGamePlayers()) {
            if (gamePlayer.getGameTeam() == this) {
                teamPlayers.add(gamePlayer);
            }
        }

        return teamPlayers;
    }

    /**
     * 获取团队中所有存活的玩家
     * 
     * @return 存活玩家列表
     */
    public List<GamePlayer> getAlivePlayers() {
        List<GamePlayer> alivePlayers = new ArrayList<>();
        
        for (GamePlayer gamePlayer : getGamePlayers()) {
            if (gamePlayer.isOnline() && !gamePlayer.isSpectator()) {
                alivePlayers.add(gamePlayer);
            }
        }
        
        return alivePlayers;
    }

    /**
     * 检查指定玩家是否在团队中
     * 
     * @param gamePlayer 要检查的玩家
     * @return 如果玩家在团队中返回true，否则返回false
     */
    public boolean isInTeam(GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return false;
        }
        
        return gamePlayer.getGameTeam() == this;
    }

    /**
     * 检查指定玩家是否在团队中，排除特定玩家
     * 
     * @param excludedPlayer 要排除的玩家
     * @param targetPlayer 要检查的玩家
     * @return 如果玩家在团队中且不是排除的玩家，返回true，否则返回false
     */
    public boolean isInTeam(GamePlayer excludedPlayer, GamePlayer targetPlayer) {
        if (targetPlayer == null || excludedPlayer == null) {
            return false;
        }
        
        return targetPlayer.getGameTeam() == this && !targetPlayer.equals(excludedPlayer);
    }

    /**
     * 将玩家添加到团队
     * 
     * @param gamePlayer 要添加的玩家
     * @return 如果添加成功返回true，否则返回false
     */
    public boolean addPlayer(GamePlayer gamePlayer) {
        if (gamePlayer == null || isFull() || isInTeam(gamePlayer)) {
            return false;
        }
        
        gamePlayer.setGameTeam(this);
        return true;
    }

    /**
     * 检查团队是否已满
     * 
     * @return 如果团队已满返回true，否则返回false
     */
    public boolean isFull() {
        return getGamePlayers().size() >= maxPlayers;
    }

    /**
     * 检查团队是否已经全灭
     * 
     * @return 如果团队没有存活玩家返回true，否则返回false
     */
    public boolean isDead() {
        for (GamePlayer gamePlayer : getGamePlayers()) {
            if (gamePlayer.isOnline() && !gamePlayer.isSpectator()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取团队当前的活跃玩家数量
     * 
     * @return 活跃玩家数量
     */
    public int getActivePlayerCount() {
        return getAlivePlayers().size();
    }

    /**
     * 检查床是否被摧毁
     * 
     * @return 如果床被摧毁返回true，否则返回false
     */
    public boolean isBedDestroyed() {
        return isDestroyed || hasBed;
    }
    
    /**
     * 设置床被摧毁状态
     * 
     * @param destroyed 床被摧毁状态
     * @param destroyer 摧毁床的玩家，可以为null
     */
    public void setBedDestroyed(boolean destroyed, GamePlayer destroyer) {
        this.isDestroyed = destroyed;
        
        if (destroyed) {
            this.destroyPlayer = destroyer;
        }
    }
}