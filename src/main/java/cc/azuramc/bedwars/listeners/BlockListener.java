package cc.azuramc.bedwars.listeners;

import cc.azuramc.bedwars.compat.VersionUtil;
import cc.azuramc.bedwars.compat.util.PlayerUtil;
import cc.azuramc.bedwars.utils.ActionBarUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.map.data.MapData;
import cc.azuramc.bedwars.events.BedwarsDestroyBedEvent;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameState;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import cc.azuramc.bedwars.utils.Util;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * 方块监听器类
 * <p>
 * 处理游戏中与方块相关的事件，包括放置、破坏和爆炸等
 * 提供了跨版本兼容性支持，确保在不同版本的Minecraft服务器上正常运行
 * </p>
 */
public class BlockListener implements Listener {
    // 常量定义
    private static final int TEAM_SPAWN_PROTECTION_RADIUS = 5;
    private static final int RESOURCE_SPAWN_PROTECTION_RADIUS = 3;
    private static final int BED_SEARCH_RADIUS = 18;
    private static final int BED_DESTROY_REWARD = 10;
    private static final int FIREBALL_EXPLOSION_RADIUS_X = 4;
    private static final int FIREBALL_EXPLOSION_RADIUS_Y = 3;
    private static final int FIREBALL_EXPLOSION_RADIUS_Z = 4;
    private static final int FIREBALL_DAMAGE = 3;
    private static final double FIREBALL_KNOCKBACK_MULTIPLIER = 0.5;
    private static final long BLOCK_PLACEMENT_COOLDOWN = 1000; // 毫秒
    private static final int MAX_BRIDGE_EGG_LENGTH = 6;
    private static final String BLOCK_TIMER_METADATA = "Game BLOCK TIMER";
    private static final String FIREBALL_METADATA = "Game FIREBALL";
    private static final String NOFALL_METADATA = "FIREBALL PLAYER NOFALL";
    
    private final AzuraBedWars plugin;
    private final Game game;

    /**
     * 构造方法
     * 
     * @param plugin 插件主类实例
     */
    public BlockListener(AzuraBedWars plugin) {
        this.plugin = plugin;
        this.game = plugin.getGame();
    }

    /**
     * 处理方块破坏事件
     *
     * @param event 方块破坏事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (game.getGameState() == GameState.WAITING) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameState() == GameState.RUNNING) {
            Player player = event.getPlayer();
            Block block = event.getBlock();
            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

            if (gamePlayer == null) {
                return;
            }

            GameTeam gameTeam = gamePlayer.getGameTeam();

            if (gamePlayer.isSpectator()) {
                event.setCancelled(true);
                return;
            }

            // 处理床方块破坏
            if (isBedBlock(block)) {
                handleBedBreak(event, player, block, gamePlayer, gameTeam);
                return;
            }

            // 检查区域保护和玩家放置的方块
            if (game.getMapData().hasRegion(block.getLocation()) || game.getBlocks().contains(block.getLocation())) {
                player.sendMessage("break canceled:  " + block.getLocation() + "first: " + game.getMapData().hasRegion(block.getLocation()));
                event.setCancelled(true);
            }
        }
    }

    /**
     * 处理方块放置事件
     *
     * @param event 方块放置事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        Block block = event.getBlock();

        // 游戏未开始或玩家为观察者时不允许放置方块
        if (gamePlayer != null && (game.getGameState() == GameState.WAITING || gamePlayer.isSpectator())) {
            event.setCancelled(true);
            return;
        }

        // 不允许放置床方块
        if (block.getType().toString().startsWith("BED")) {
            event.setCancelled(true);
            return;
        }

        // 检查区域保护
        if (isProtectedArea(block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        // 处理TNT放置
        if (block.getType() == MaterialUtil.TNT()) {
            handleTNTPlacement(event, player);
            return;
        }

        // 处理搭桥蛋
        ItemStack item = PlayerUtil.getItemInHand(player);
        if (isBridgeEggItem(item)) {
            handleBridgeEggPlacement(event, player, item);
            return;
        }

        if (isSpeedWool(item)) {
            handleSpeedWoolPlacement(event, player, item);
        }
    }

    /**
     * 处理实体爆炸事件
     *
     * @param event 实体爆炸事件
     */
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        // 游戏未运行时取消爆炸
        if (game.getGameState() != GameState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        // 处理爆炸块列表
        processExplodedBlocks(event);

        // 处理火球爆炸
        if (entity instanceof Fireball) {
            handleFireballExplosion((Fireball) entity);
        }

        // 取消原版爆炸效果，使用自定义爆炸效果
        event.setCancelled(true);
    }

    /**
     * 检查方块是否为床方块
     * 兼容全版本Minecraft
     * 
     * @param block 需要检查的方块
     * @return 如果是床方块返回true，否则返回false
     */
    private boolean isBedBlock(Block block) {
        String typeName = block.getType().name();
        return typeName.endsWith("_BED") || typeName.equals("BED_BLOCK");
    }
    
    /**
     * 检查方块是否为染色玻璃
     * 
     * @param block 需要检查的方块
     * @return 如果是染色玻璃返回true，否则返回false
     */
    private boolean isStainedGlass(Block block) {
        return block.getType().name().contains("STAINED_GLASS");
    }
    
    /**
     * 设置方块的数据值（跨版本兼容）
     * 兼容从1.8到最新版本的Minecraft服务器
     * 
     * @param block 需要设置的方块
     * @param data 数据值
     */
    private void setBlockData(Block block, byte data) {
        try {
            if (VersionUtil.isLessThan113()) {
                // 1.8 - 1.12版本使用反射调用setData方法
                java.lang.reflect.Method setDataMethod = Block.class.getMethod("setData", byte.class);
                setDataMethod.invoke(block, data);
            } else if (VersionUtil.isLessThan116()) {
                // 1.13 - 1.15版本，可以通过BlockData API设置
                org.bukkit.block.data.BlockData blockData = block.getBlockData();
                // 根据方块类型处理方向数据
                if (blockData instanceof org.bukkit.block.data.Directional directional) {
                    // 将旧版本的数据值转换为方向
                    BlockFace face = convertDataToBlockFace(data);
                    if (face != null && directional.getFaces().contains(face)) {
                        directional.setFacing(face);
                        block.setBlockData(directional);
                    }
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("无法设置方块数据: " + e.getMessage());
            if (Bukkit.getPluginManager().isPluginEnabled("AzuraBedWars")) {
                Bukkit.getLogger().info("尝试使用替代方法设置方块数据...");
                try {
                    // 尝试使用替代方法
                    fallbackSetBlockData(block, data);
                } catch (Exception ex) {
                    Bukkit.getLogger().warning("替代方法也失败: " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * 将旧版本的数据值转换为BlockFace方向
     * 
     * @param data 旧版本的数据值
     * @return 对应的BlockFace方向，如果无法转换则返回null
     */
    private BlockFace convertDataToBlockFace(byte data) {
        // 此映射基于大多数方向性方块的通用规则，可能需要根据特定方块调整
        return switch (data & 0x7) { // 只使用低3位
            case 0 -> BlockFace.DOWN;
            case 1 -> BlockFace.UP;
            case 2 -> BlockFace.NORTH;
            case 3 -> BlockFace.SOUTH;
            case 4 -> BlockFace.WEST;
            case 5 -> BlockFace.EAST;
            default -> null;
        };
    }
    
    /**
     * 根据数据值获取对应的颜色名称
     * 
     * @param data 颜色数据值
     * @return 颜色名称
     */
    private String getColorNameFromData(byte data) {
        return switch (data & 0xF) { // 只使用低4位
            case 0 -> "WHITE";
            case 1 -> "ORANGE";
            case 2 -> "MAGENTA";
            case 3 -> "LIGHT_BLUE";
            case 4 -> "YELLOW";
            case 5 -> "LIME";
            case 6 -> "PINK";
            case 7 -> "GRAY";
            case 8 -> "LIGHT_GRAY";
            case 9 -> "CYAN";
            case 10 -> "PURPLE";
            case 11 -> "BLUE";
            case 12 -> "BROWN";
            case 13 -> "GREEN";
            case 14 -> "RED";
            case 15 -> "BLACK";
            default -> "WHITE";
        };
    }
    
    /**
     * 回退方法：尝试使用其他方式设置方块数据
     * 
     * @param block 需要设置的方块
     * @param data 数据值
     */
    private void fallbackSetBlockData(Block block, byte data) {
        // 尝试使用NMS或其他方法
        try {
            // 获取当前服务器版本
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            
            // 对于羊毛等染色方块，我们可以直接替换为对应颜色的方块
            Material type = block.getType();
            if (MaterialUtil.isWool(type)) {
                String colorName = getColorNameFromData(data);
                Material newType = Material.getMaterial(colorName + "_WOOL");
                if (newType != null) {
                    block.setType(newType);
                    return;
                }
            }
            
            // 最后的回退方案：重新放置相同类型的方块
            // 这不会保留数据值，但至少保证方块类型正确
            if (block.getType() != MaterialUtil.AIR()) {
                Material currentType = block.getType();
                block.setType(currentType);
            }
            
        } catch (Exception e) {
            Bukkit.getLogger().warning("回退设置方块数据方法失败: " + e.getMessage());
        }
    }

    /**
     * 检查区域是否受保护
     * 
     * @param location 位置
     * @return 如果区域受保护返回true，否则返回false
     */
    private boolean isProtectedArea(Location location) {
        // 检查地图区域保护
        if (game.getMapData().hasRegion(location)) {
            return true;
        }
        
        // 检查团队出生点保护
        for (GameTeam gameTeam : game.getGameTeams()) {
            if (gameTeam.getSpawn().distance(location) <= TEAM_SPAWN_PROTECTION_RADIUS) {
                return true;
            }
        }
        
        // 检查资源点保护
        for (MapData.RawLocation rawLocation : game.getMapData().getDrops()) {
            if (rawLocation.toLocation().distance(location) <= RESOURCE_SPAWN_PROTECTION_RADIUS) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 处理TNT放置
     * 
     * @param event 方块放置事件
     * @param player 玩家
     */
    private void handleTNTPlacement(BlockPlaceEvent event, Player player) {
        event.setCancelled(true);
        Block block = event.getBlock();
        block.setType(MaterialUtil.AIR());

        // 生成已激活的TNT实体
        TNTPrimed tnt = block.getWorld().spawn(block.getLocation().add(0.5D, 0.0D, 0.5D), TNTPrimed.class);
        tnt.setVelocity(new Vector(0, 0, 0));

        // 减少玩家物品栏中的TNT数量
        consumeItem(player, MaterialUtil.TNT());
    }

    /**
     * 减少玩家物品栏中指定物品的数量
     * 
     * @param player 玩家
     * @param material 物品类型
     */
    private void consumeItem(Player player, Material material) {
        ItemStack item = PlayerUtil.getItemInHand(player);
        if (item != null && item.getType() == material) {
            if (item.getAmount() == 1) {
                PlayerUtil.setItemInHand(player, null);
            } else {
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    /**
     * 检查物品是否为搭桥蛋
     * 
     * @param item 物品
     * @return 如果是搭桥蛋返回true，否则返回false
     */
    private boolean isBridgeEggItem(ItemStack item) {
        return item != null && item.getType() == Material.EGG;
    }

    /**
     * 处理搭桥蛋放置
     * 
     * @param event 方块放置事件
     * @param player 玩家
     * @param item 物品
     */
    private void handleBridgeEggPlacement(BlockPlaceEvent event, Player player, ItemStack item) {
        // 检查冷却时间
        if (isOnCooldown(player)) {
            event.setCancelled(true);
            return;
        }
        
        // 设置冷却时间
        player.setMetadata(BLOCK_TIMER_METADATA, new FixedMetadataValue(AzuraBedWars.getInstance(), System.currentTimeMillis()));

        // 防止玩家卡在方块中
        Block block = event.getBlock();
        if (block.getY() != event.getBlockAgainst().getY()) {
            if (Math.max(Math.abs(player.getLocation().getX() - (block.getX() + 0.5D)), 
                          Math.abs(player.getLocation().getZ() - (block.getZ() + 0.5D))) < 0.5) {
                return;
            }
        }
        
        // 获取搭桥方向
        BlockFace blockFace = event.getBlockAgainst().getFace(block);
        
        // 开始搭桥任务
        startBridgeTask(block, blockFace, item);
    }

    /**
     * 检查物品是否为火速羊毛
     *
     * @param item 物品
     * @return 如果是火速羊毛返回true，否则返回false
     */
    private boolean isSpeedWool(ItemStack item) {
        return item != null && MaterialUtil.isWool(item.getType()) && !item.getEnchantments().isEmpty();
    }

    /**
     * 处理搭桥蛋放置
     *
     * @param event 方块放置事件
     * @param player 玩家
     * @param item 物品
     */
    private void handleSpeedWoolPlacement(BlockPlaceEvent event, Player player, ItemStack item) {
        // 检查冷却时间
        if (isOnCooldown(player)) {
            event.setCancelled(true);
            return;
        }

        // 设置冷却时间
        player.setMetadata(BLOCK_TIMER_METADATA, new FixedMetadataValue(AzuraBedWars.getInstance(), System.currentTimeMillis()));

        // 防止玩家卡在方块中
        Block block = event.getBlock();
        if (block.getY() != event.getBlockAgainst().getY()) {
            if (Math.max(Math.abs(player.getLocation().getX() - (block.getX() + 0.5D)),
                    Math.abs(player.getLocation().getZ() - (block.getZ() + 0.5D))) < 0.5) {
                return;
            }
        }

        // 获取搭桥方向
        BlockFace blockFace = event.getBlockAgainst().getFace(block);

        // 开始搭桥任务
        startBridgeTask(block, blockFace, item);
    }

    /**
     * 检查玩家是否在冷却时间内
     * 
     * @param player 玩家
     * @return 如果在冷却时间内返回true，否则返回false
     */
    private boolean isOnCooldown(Player player) {
        long lastUse = player.hasMetadata(BLOCK_TIMER_METADATA) ? 
                       player.getMetadata(BLOCK_TIMER_METADATA).getFirst().asLong() : 0L;
        return Math.abs(System.currentTimeMillis() - lastUse) < BLOCK_PLACEMENT_COOLDOWN;
    }

    /**
     * 开始搭桥任务
     *
     * @param block 起始方块
     * @param blockFace 方向
     * @param item 使用的物品
     */
    private void startBridgeTask(Block block, BlockFace blockFace, ItemStack item) {
        new BukkitRunnable() {
            int i = 1;

            @Override
            public void run() {
                if (i > MAX_BRIDGE_EGG_LENGTH) {
                    cancel();
                    return;
                }

                Block relativeBlock = block.getRelative(blockFace, i);

                // 检查是否可以在此位置放置方块
                if (isProtectedRelativeLocation(relativeBlock, blockFace)) {
                    cancel();
                    return;
                }

                // 放置方块
                if (relativeBlock.getType() == MaterialUtil.AIR()) {
                    relativeBlock.setType(item.getType());
                    if (!VersionUtil.isLessThan113() && item.getData() != null) {
                        setBlockData(relativeBlock, item.getData().getData());
                    }
                    block.getWorld().playSound(block.getLocation(), SoundUtil.STEP_WOOL(), 1f, 1f);
                }

                i++;
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), 0, 4L);
    }

    /**
     * 检查相对位置是否受保护
     * 
     * @param block 方块
     * @param blockFace 方向
     * @return 如果位置受保护返回true，否则返回false
     */
    private boolean isProtectedRelativeLocation(Block block, BlockFace blockFace) {
        // 检查团队出生点保护
        for (GameTeam gameTeam : game.getGameTeams()) {
            if (gameTeam.getSpawn().distance(block.getLocation()) <= TEAM_SPAWN_PROTECTION_RADIUS) {
                return true;
            }
        }
        
        // 检查地图区域保护
        if (game.getMapData().hasRegion(block.getLocation())) {
            return true;
        }
        
        // 检查钻石资源点保护
        for (Location location : game.getMapData().getDropLocations(MapData.DropType.DIAMOND)) {
            if (location.distance(block.getLocation()) <= RESOURCE_SPAWN_PROTECTION_RADIUS) {
                return true;
            }
        }
        
        // 检查绿宝石资源点保护
        for (Location location : game.getMapData().getDropLocations(MapData.DropType.EMERALD)) {
            if (location.distance(block.getLocation()) <= RESOURCE_SPAWN_PROTECTION_RADIUS) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 处理床方块破坏
     * 
     * @param event 方块破坏事件
     * @param player 玩家
     * @param block 方块
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在团队
     */
    private void handleBedBreak(BlockBreakEvent event, Player player, Block block, GamePlayer gamePlayer, GameTeam gameTeam) {
        event.setCancelled(true);

        // 不能破坏自己的床
        if (gameTeam.getSpawn().distance(block.getLocation()) <= BED_SEARCH_RADIUS) {
            player.sendMessage("§c你不能破坏你家的床");
            return;
        }

        // 查找床所属团队
        for (GameTeam targetTeam : game.getGameTeams()) {
            if (targetTeam.getSpawn().distance(block.getLocation()) <= BED_SEARCH_RADIUS) {
                if (!targetTeam.isDead()) {
                    processBedDestruction(player, gamePlayer, gameTeam, targetTeam, block);
                    return;
                }
                player.sendMessage("§c此床没有队伍");
                return;
            }
        }
    }

    /**
     * 处理床被破坏的逻辑
     * 
     * @param player 玩家
     * @param gamePlayer 游戏玩家
     * @param gameTeam 玩家所在团队
     * @param targetTeam 床所属团队
     * @param block 床方块
     */
    private void processBedDestruction(Player player, GamePlayer gamePlayer, GameTeam gameTeam, GameTeam targetTeam, Block block) {
        // 掉落床方块物品
        Util.dropTargetBlock(block);

        // 奖励金币
        rewardBedDestruction(player);

        // 广播消息
        broadcastBedDestructionMessages(gamePlayer, gameTeam, targetTeam);

        // 触发床被破坏事件
        Bukkit.getPluginManager().callEvent(new BedwarsDestroyBedEvent(player, targetTeam));

        // 更新团队状态
        targetTeam.setDestroyPlayer(gamePlayer);
        targetTeam.setBedDestroy(true);

        // 更新玩家统计数据
        gamePlayer.getPlayerData().addDestroyedBeds();
    }

    /**
     * 奖励破坏床的玩家金币
     * 
     * @param player 玩家
     */
    private void rewardBedDestruction(Player player) {
        // 动作栏显示奖励
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                if (i == 5) {
                    cancel();
                    return;
                }
                ActionBarUtil.sendBar(player, "§6+" + BED_DESTROY_REWARD + "个金币");
                i++;
            }
        }.runTaskTimerAsynchronously(plugin, 0, 10);
        
        // 聊天栏显示奖励
        player.sendMessage("§6+" + BED_DESTROY_REWARD + "个金币 (破坏床)");
        
        // 实际奖励金币
        plugin.getEcon().depositPlayer(player, BED_DESTROY_REWARD);
    }

    /**
     * 广播床被破坏的消息
     * 
     * @param gamePlayer 破坏床的玩家
     * @param gameTeam 玩家所在团队
     * @param targetTeam 床所属团队
     */
    private void broadcastBedDestructionMessages(GamePlayer gamePlayer, GameTeam gameTeam, GameTeam targetTeam) {
        // 播放全局音效
        game.broadcastSound(SoundUtil.ENDERDRAGON_HIT(), 10, 10);
        
        // 发送全局消息
        game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");
        game.broadcastMessage(" ");
        game.broadcastMessage("§c§l" + targetTeam.getName() + " §a的床被 " + gameTeam.getChatColor() + gamePlayer.getNickName() + "§a 挖爆!");
        game.broadcastMessage(" ");
        game.broadcastMessage("§7▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃▃");

        // 向受影响的团队发送标题提示
        game.broadcastTeamTitle(targetTeam, 1, 20, 1, "§c§l床被摧毁", "§c死亡将无法复活");
    }


    /**
     * 处理爆炸块列表
     * 
     * @param event 爆炸事件
     */
    private void processExplodedBlocks(EntityExplodeEvent event) {
        for (int i = 0; i < event.blockList().size(); i++) {
            Block block = event.blockList().get(i);
            
            // 检查受保护区域
            if (game.getMapData().hasRegion(block.getLocation())) {
                continue;
            }

            // 处理可爆炸的方块
            if (!isProtectedBlockType(block) && !game.getBlocks().contains(block.getLocation())) {
                // 清除方块并显示爆炸效果
                block.setType(MaterialUtil.AIR());
                
                // 根据版本显示不同的爆炸粒子效果
                if (!VersionUtil.isLessThan113()) {
                    block.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, block.getLocation(), 5);
                } else {
                    block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 0);
                }
                
                // 播放爆炸音效
                block.getWorld().playSound(block.getLocation(), SoundUtil.EXPLODE(), 1.0F, 1.0F);
            }
        }
    }

    /**
     * 检查方块类型是否受保护不受爆炸影响
     * 
     * @param block 方块
     * @return 如果方块受保护返回true，否则返回false
     */
    private boolean isProtectedBlockType(Block block) {
        return isStainedGlass(block) || isBedBlock(block);
    }

    /**
     * 处理火球爆炸
     * 
     * @param fireball 火球实体
     */
    private void handleFireballExplosion(Fireball fireball) {
        // 检查是否是玩家发射的火球
        if (!fireball.hasMetadata(FIREBALL_METADATA)) {
            return;
        }
        
        // 获取火球发射者
        GamePlayer ownerPlayer = GamePlayer.get((UUID) fireball.getMetadata(FIREBALL_METADATA).getFirst().value());
        if (ownerPlayer == null) {
            return;
        }

        // 处理火球爆炸范围内的玩家
        for (Entity entity : fireball.getNearbyEntities(
                FIREBALL_EXPLOSION_RADIUS_X, 
                FIREBALL_EXPLOSION_RADIUS_Y, 
                FIREBALL_EXPLOSION_RADIUS_Z)) {
            
            if (!(entity instanceof Player player)) {
                continue;
            }

            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
            
            // 检查是否是队友
            if (isFireballTeammate(ownerPlayer, gamePlayer)) {
                continue;
            }

            // 对敌人造成伤害和击退
            if (gamePlayer != null) {
                applyFireballDamage(player, gamePlayer, ownerPlayer, fireball);
            }
        }
    }

    /**
     * 检查是否是火球发射者的队友
     * 
     * @param ownerPlayer 火球发射者
     * @param targetPlayer 目标玩家
     * @return 如果是队友返回true，否则返回false
     */
    private boolean isFireballTeammate(GamePlayer ownerPlayer, GamePlayer targetPlayer) {
        GameTeam ownerTeam = ownerPlayer.getGameTeam();
        return ownerTeam != null && ownerTeam.isInTeam(ownerPlayer, targetPlayer);
    }

    /**
     * 对玩家应用火球伤害和击退效果
     * 
     * @param player 玩家
     * @param gamePlayer 游戏玩家对象
     * @param ownerPlayer 火球发射者
     * @param fireball 火球实体
     */
    private void applyFireballDamage(Player player, GamePlayer gamePlayer, GamePlayer ownerPlayer, Fireball fireball) {
        // 造成伤害
        player.damage(FIREBALL_DAMAGE);
        
        // 记录伤害来源（用于助攻系统）
        gamePlayer.getAssistsManager().setLastDamage(ownerPlayer, System.currentTimeMillis());
        
        // 设置元数据以防止掉落伤害
        player.setMetadata(NOFALL_METADATA, new FixedMetadataValue(plugin, ownerPlayer.getUuid()));
        
        // 应用击退效果
        Vector knockbackVector = Util.getPosition(player.getLocation(), fireball.getLocation(), 1.5D);
        player.setVelocity(knockbackVector.multiply(FIREBALL_KNOCKBACK_MULTIPLIER));
    }
}
