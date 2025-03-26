package cc.azuramc.bedwars.game;

import lombok.Data;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameTeam {
    private final TeamColor teamColor;
    private final Location spawn;
    private Block bedFeet;
    private Block bedHead;
    private BlockFace bedFace;
    private int maxPlayers;
    private boolean unbed;
    private boolean bedDestroy;
    private GamePlayer destroyPlayer;

    private int forge;
    private int manicMiner;
    private boolean sharpenedSwords;
    private int reinforcedArmor;
    private boolean healPool;
    private boolean trap;
    private boolean miner;
    
    // 判断服务器版本
    private static final boolean NEW_VERSION;
    
    static {
        boolean newVersion = false;
        try {
            // 1.13+版本存在Material.PLAYER_HEAD
            Material.valueOf("PLAYER_HEAD");
            newVersion = true;
        } catch (IllegalArgumentException e) {
            // 1.8-1.12版本
            newVersion = false;
        }
        NEW_VERSION = newVersion;
    }

    public GameTeam(TeamColor teamColor, Location location, int maxPlayers) {
        this.unbed = false;

        this.sharpenedSwords = false;
        this.reinforcedArmor = 0;
        this.manicMiner = 0;
        this.miner = false;
        this.healPool = false;
        this.trap = false;

        this.spawn = location;
        this.teamColor = teamColor;
        this.maxPlayers = maxPlayers;

        // 初始化床相关字段
        initializeBed();
    }
    
    /**
     * 初始化床相关字段
     */
    private void initializeBed() {
        List<Block> blocks = new ArrayList<>();
        for (int x = -18; x < 18; x++) {
            for (int y = -18; y < 18; y++) {
                for (int z = -18; z < 18; z++) {
                    Block block = spawn.clone().add(x, y, z).getBlock();
                    if (isBedBlock(block)) {
                        blocks.add(block);
                    }
                }
            }
        }
        
        // 判断床脚床头，并记录床朝向
        if (blocks.size() >= 2) {
            // 尝试确定哪个是床头，哪个是床脚
            Block block1 = blocks.get(0);
            Block block2 = blocks.get(1);
            
            if (NEW_VERSION) {
                // 新版本(1.13+)使用BlockData API
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
                    // 如果出错，使用备用方法
                    this.bedHead = block1;
                    this.bedFeet = block2;
                    this.bedFace = BlockFace.NORTH; // 默认朝向
                }
            } else {
                // 旧版本(1.8-1.12)使用MaterialData API
                try {
                    org.bukkit.material.Bed bedData1 = (org.bukkit.material.Bed) block1.getState().getData();
                    org.bukkit.material.Bed bedData2 = (org.bukkit.material.Bed) block2.getState().getData();
                    
                    if (bedData1.isHeadOfBed()) {
                        this.bedHead = block1;
                        this.bedFeet = block2;
                        this.bedFace = bedData1.getFacing();
                    } else {
                        this.bedFeet = block1;
                        this.bedHead = block2;
                        this.bedFace = bedData2.getFacing();
                    }
                } catch (Exception e) {
                    // 如果出错，使用备用方法
                    this.bedHead = block1;
                    this.bedFeet = block2;
                    this.bedFace = BlockFace.NORTH; // 默认朝向
                }
            }
        } else {
            // 如果找不到足够的床方块，初始化为空值，避免空指针
            this.bedHead = spawn.getBlock();
            this.bedFeet = spawn.getBlock();
            this.bedFace = BlockFace.NORTH; // 默认朝向
        }
    }
    
    /**
     * 判断方块是否为床方块，兼容新旧版本
     */
    private boolean isBedBlock(Block block) {
        if (NEW_VERSION) {
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

    public ChatColor getChatColor() {
        return teamColor.getChatColor();
    }

    public DyeColor getDyeColor() {
        return teamColor.getDyeColor();
    }

    public Color getColor() {
        return teamColor.getColor();
    }

    public List<GamePlayer> getGamePlayers() {
        List<GamePlayer> gamePlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : GamePlayer.getGamePlayers()) {
            if (gamePlayer.getGameTeam() == this) {
                gamePlayers.add(gamePlayer);
            }
        }

        return gamePlayers;
    }

    public List<GamePlayer> getAlivePlayers() {
        List<GamePlayer> alivePlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : getGamePlayers()) {
            if (gamePlayer.isOnline() && !gamePlayer.isSpectator()) {
                alivePlayers.add(gamePlayer);
            }
        }
        return alivePlayers;
    }

    public boolean isInTeam(GamePlayer gamePlayer) {
        for (GamePlayer player : getGamePlayers()) {
            if (player.equals(gamePlayer)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInTeam(GamePlayer removePlayer, GamePlayer gamePlayer) {
        for (GamePlayer player : getGamePlayers()) {
            if (player.equals(gamePlayer) && !player.equals(removePlayer)) {
                return true;
            }
        }
        return false;
    }

    public boolean addPlayer(GamePlayer gamePlayer) {
        if (isFull() || isInTeam(gamePlayer)) {
            return false;
        }
        gamePlayer.setGameTeam(this);
        return true;
    }

    public boolean isFull() {
        return getGamePlayers().size() >= maxPlayers;
    }

    public boolean isDead() {
        for (GamePlayer gamePlayer : getGamePlayers()) {
            if ((gamePlayer.isOnline()) && (!gamePlayer.isSpectator())) {
                return false;
            }
        }
        return true;
    }


    public String getName() {
        return teamColor.getName();
    }
}