package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.compat.util.GameTeamBedHandler;
import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 游戏团队管理类
 * 负责管理游戏中的队伍，包括队伍颜色、出生点、床位置和队伍升级等信息
 *
 * @author an5w1r@163.com
 */
@Data
public class GameTeam {
    /** 搜索床的范围 */
    public static final int BED_SEARCH_RADIUS = 18;
    /** 默认床朝向 */
    public static final BlockFace DEFAULT_BED_FACE = BlockFace.NORTH;

    private final TeamColor teamColor;
    private final Location spawnLocation;
    private int maxPlayers;

    private Block bedFeet;
    private Block bedHead;
    private BlockFace bedFace;
    private boolean hasBed;
    private boolean isDestroyed;
    private GamePlayer destroyPlayer;

    private boolean hasSharpnessUpgrade;
    private int protectionUpgrade;
    private int magicMinerUpgrade;
    private int resourceFurnaceUpgrade;
    private boolean hasHealPoolUpgrade;
    private int fallingProtectionUpgrade;

    private boolean hasBlindnessTrap;
    private boolean hasFightBackTrap;
    private boolean hasAlarmTrap;
    private boolean hasMinerTrap;

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
        new GameTeamBedHandler(this);
    }
    
    /**
     * 初始化团队默认属性值
     */
    private void initializeDefaults() {
        // 床状态初始化
        this.hasBed = false;
        this.isDestroyed = false;
        
        // 团队升级初始化
        this.hasSharpnessUpgrade = false;
        this.protectionUpgrade = 0;
        this.magicMinerUpgrade = 0;
        this.resourceFurnaceUpgrade = 0;
        this.hasHealPoolUpgrade = false;
        this.fallingProtectionUpgrade = 0;

        this.hasBlindnessTrap = false;
        this.hasFightBackTrap = false;
        this.hasAlarmTrap = false;
        this.hasMinerTrap = false;
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
        
        // 更新玩家的TabList显示名称
        if (gamePlayer.getPlayer() != null) {
            cc.azuramc.bedwars.tablist.TabList.changeTabListNameInGame(gamePlayer);
        }
        
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