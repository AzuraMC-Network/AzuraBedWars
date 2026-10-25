package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.game.item.armor.ArmorType;
import cc.azuramc.bedwars.game.item.tool.ToolType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * What a player keeps when they rejoin the running game
 *
 * @author an5w1r@163.com
 */
@Getter
public class ReconnectState {

    private final UUID uuid;
    private final String name;

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

    public ReconnectState(@NotNull GamePlayer gamePlayer) {
        this.uuid = gamePlayer.getUuid();
        this.name = gamePlayer.getNickName();
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
    }

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
    }
}
