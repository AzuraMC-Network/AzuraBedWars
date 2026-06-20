package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.game.item.armor.ArmorType;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
@Getter
public class ReconnectState {

    private final UUID uuid;

    @Nullable
    private final TeamColor teamColor;

    private final int currentGameKills;
    private final int currentGameFinalKills;
    private final int currentGameAssists;
    private final int currentGameDeaths;
    private final int currentGameDestroyedBeds;

    private final ArmorType armorType;
    private final ToolType pickaxeType;
    private final ToolType axeType;
    private final boolean shear;

    private final Map<String, Integer> experienceSources;

    /**
     * 从在线的 GamePlayer 抓取断线快照
     *
     * @param gamePlayer 断线的玩家
     */
    public ReconnectState(@NotNull GamePlayer gamePlayer) {
        this.uuid = gamePlayer.getUuid();
        this.teamColor = gamePlayer.getGameTeam() != null ? gamePlayer.getGameTeam().getTeamColor() : null;

        this.currentGameKills = gamePlayer.getCurrentGameKills();
        this.currentGameFinalKills = gamePlayer.getCurrentGameFinalKills();
        this.currentGameAssists = gamePlayer.getCurrentGameAssists();
        this.currentGameDeaths = gamePlayer.getCurrentGameDeaths();
        this.currentGameDestroyedBeds = gamePlayer.getCurrentGameDestroyedBeds();

        this.armorType = gamePlayer.getArmorType();
        this.pickaxeType = gamePlayer.getPickaxeType();
        this.axeType = gamePlayer.getAxeType();
        this.shear = gamePlayer.isShear();

        this.experienceSources = gamePlayer.getExperienceSources() != null
                ? new HashMap<>(gamePlayer.getExperienceSources())
                : new HashMap<>();
    }

    /**
     * 将快照恢复到重连后新建的 GamePlayer
     *
     * @param gamePlayer  重连后新建的玩家
     * @param gameManager 游戏管理器（用于按颜色解析队伍）
     */
    public void restoreTo(@NotNull GamePlayer gamePlayer, @NotNull GameManager gameManager) {
        if (teamColor != null) {
            GameTeam team = gameManager.getTeam(teamColor);
            if (team != null) {
                gamePlayer.setGameTeam(team);
            }
        }

        gamePlayer.setCurrentGameKills(currentGameKills);
        gamePlayer.setCurrentGameFinalKills(currentGameFinalKills);
        gamePlayer.setCurrentGameAssists(currentGameAssists);
        gamePlayer.setCurrentGameDeaths(currentGameDeaths);
        gamePlayer.setCurrentGameDestroyedBeds(currentGameDestroyedBeds);

        gamePlayer.setArmorType(armorType);
        gamePlayer.setPickaxeType(pickaxeType);
        gamePlayer.setAxeType(axeType);
        gamePlayer.setShear(shear);

        gamePlayer.setExperienceSources(experienceSources != null
                ? new HashMap<>(experienceSources)
                : new HashMap<>()
        );
    }
}
