package cc.azuramc.bedwars.scoreboard.provider;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.level.PlayerLevelManager;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * @author an5w1r@163.com
 */
public abstract class AbstractBoardProvider {

    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yy", Locale.CHINESE);

    protected final GameManager gameManager;
    protected final AzuraBedWars plugin;

    protected AbstractBoardProvider(GameManager gameManager) {
        this.gameManager = gameManager;
        this.plugin = AzuraBedWars.getInstance();
    }

    /**
     * 获取计分板标题
     *
     * @return 计分板标题
     */
    protected abstract String getTitle();

    /**
     * 构建计分板内容行
     *
     * @param gamePlayer 游戏玩家
     * @return 计分板内容行列表
     */
    protected abstract List<String> buildLines(GamePlayer gamePlayer);

    /**
     * 为玩家显示计分板
     *
     * @param player 玩家
     */
    public void show(Player player) {
        if (player == null) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer != null && gamePlayer.getBoard() == null) {
            FastBoard board = new FastBoard(player);
            board.updateTitle(getTitle());
            gamePlayer.setBoard(board);
            updatePlayerBoard(gamePlayer);
        }
    }

    /**
     * 更新所有在线玩家的计分板
     */
    public void updateBoard() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            updatePlayerBoard(gamePlayer);
        }
    }

    /**
     * 更新单个玩家的计分板
     *
     * @param gamePlayer 游戏玩家
     */
    public void updatePlayerBoard(GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return;
        }

        FastBoard board = gamePlayer.getBoard();
        Player player = gamePlayer.getPlayer();

        if (player == null || board == null || !player.isOnline()) {
            return;
        }

        // 构建并更新计分板内容
        List<String> lines = buildLines(gamePlayer);
        board.updateLines(lines.toArray(new String[0]));
    }

    /**
     * 移除玩家的计分板
     *
     * @param player 玩家
     */
    public void removeBoard(Player player) {
        if (player == null) {
            return;
        }

        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        if (gamePlayer != null && gamePlayer.getBoard() != null) {
            FastBoard board = gamePlayer.getBoard();
            board.delete();
            gamePlayer.setBoard(null);
        }
    }

    /**
     * 移除所有玩家的计分板
     */
    public void removeAllBoards() {
        for (GamePlayer gamePlayer : GamePlayer.getOnlinePlayers()) {
            Player player = gamePlayer.getPlayer();
            if (player != null) {
                removeBoard(player);
            }
        }
    }

    /**
     * 更新玩家等级显示
     *
     * @param player     玩家
     * @param playerData 玩家数据
     */
    protected void updatePlayerLevel(Player player, PlayerData playerData) {
        if (player == null || playerData == null) {
            return;
        }
        // 设置玩家的显示等级
        player.setLevel(playerData.getLevel());
        // 设置经验条显示当前等级的进度
        float progress = (float) PlayerLevelManager.getLevelProgress(playerData);
        player.setExp(progress);
    }

    protected String getFormattedDateRaw() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
}
