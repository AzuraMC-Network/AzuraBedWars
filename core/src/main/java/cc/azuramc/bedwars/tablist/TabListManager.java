package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

/**
 * TabList主管理类
 * 负责协调各个组件和管理自动更新任务
 *
 * @author an5w1r@163.com
 */
public class TabListManager {

    @Getter
    private final GameManager gameManager;
    @Getter
    private final PlayerTabListHandler playerHandler;
    @Getter
    private final HeaderFooterManager headerFooterManager;
    @Getter
    private final TeamSorter teamSorter;
    @Getter
    private final TabListPacketSender packetSender;
    @Getter
    private final GameStateTabListProvider gameStateProvider;
    private final Map<GamePlayer, Team> teamMap = new HashMap<>();
    private BukkitTask updateTask;

    public TabListManager(GameManager gameManager) {
        this.gameManager = gameManager;
        this.playerHandler = new PlayerTabListHandler(this);
        this.headerFooterManager = new HeaderFooterManager();
        this.teamSorter = new TeamSorter();
        this.packetSender = new TabListPacketSender();
        this.gameStateProvider = new GameStateTabListProvider();
    }

    /**
     * 启动TabList自动更新
     */
    public void startAutoUpdate(Plugin plugin) {
        if (updateTask != null && !updateTask.isCancelled()) {
            return;
        }

        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    updateAllTabListNames();
                    updateHeaderFooter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    /**
     * 停止自动更新
     */
    public void stopAutoUpdate() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
            updateTask = null;
        }
    }

    /**
     * 添加玩家到TabList
     */
    public void addToTab(GamePlayer gamePlayer) {
        playerHandler.addPlayerToTab(gamePlayer, teamMap);
        packetSender.sendCurrentHeaderFooter(gamePlayer.getPlayer(), headerFooterManager);
    }

    /**
     * 从TabList移除玩家
     */
    public void removePlayerFromTab(GamePlayer gamePlayer) {
        playerHandler.removePlayerFromTab(gamePlayer, teamMap);
    }

    /**
     * 更新所有玩家的TabList显示名称
     */
    public void updateAllTabListNames() {
        for (Map.Entry<GamePlayer, Team> entry : teamMap.entrySet()) {
            GamePlayer gamePlayer = entry.getKey();
            Team team = entry.getValue();

            String newTeamName = teamSorter.generateSortedTeamName(gamePlayer);

            if (!team.getName().equals(newTeamName)) {
                playerHandler.updatePlayerTeam(gamePlayer, team, newTeamName, teamMap);
            }
        }

        gameStateProvider.updateHeaderFooterByGameState(gameManager, headerFooterManager);
    }

    /**
     * 更新单个玩家的TabList
     */
    public void updatePlayerTabList(GamePlayer gamePlayer) {
        playerHandler.updatePlayerTabList(gamePlayer, gameManager);
    }

    /**
     * 更新所有玩家的Header和Footer
     */
    public void updateHeaderFooter() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            if (gamePlayer.getPlayer() != null && gamePlayer.getPlayer().isOnline()) {
                packetSender.sendHeaderFooter(gamePlayer.getPlayer(),
                        headerFooterManager.getHeader(),
                        headerFooterManager.getFooter(),
                        gamePlayer);
            }
        }
    }

    /**
     * 清除Header和Footer
     */
    public void clearHeaderFooter() {
        headerFooterManager.clear();
        updateHeaderFooter();
    }

    /**
     * 获取Header内容
     */
    public String getHeader() {
        return headerFooterManager.getHeader();
    }

    /**
     * 设置Header内容
     */
    public void setHeader(String header) {
        headerFooterManager.setHeader(header);
    }

    /**
     * 设置Header内容
     */
    public void setHeader(java.util.List<String> lines) {
        headerFooterManager.setHeader(lines);
    }

    /**
     * 获取Footer内容
     */
    public String getFooter() {
        return headerFooterManager.getFooter();
    }

    /**
     * 设置Footer内容
     */
    public void setFooter(String footer) {
        headerFooterManager.setFooter(footer);
    }

    /**
     * 设置Footer内容
     */
    public void setFooter(java.util.List<String> lines) {
        headerFooterManager.setFooter(lines);
    }

    /**
     * 发送Header和Footer数据包给指定玩家
     *
     * @param player 目标玩家
     * @param header Header文本
     * @param footer Footer文本
     */
    public void sendHeaderFooter(org.bukkit.entity.Player player, String header, String footer) {
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        packetSender.sendHeaderFooter(player, header, footer, gamePlayer);
    }

    /**
     * 发送当前设置的Header和Footer给指定玩家
     *
     * @param player 目标玩家
     */
    public void sendCurrentHeaderFooter(org.bukkit.entity.Player player) {
        packetSender.sendCurrentHeaderFooter(player, headerFooterManager);
    }
}
