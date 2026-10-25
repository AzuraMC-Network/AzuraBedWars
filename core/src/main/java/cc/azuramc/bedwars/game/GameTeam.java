package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.api.game.IGamePlayer;
import cc.azuramc.bedwars.api.game.IGameTeam;
import cc.azuramc.bedwars.compat.util.GameTeamBedHandler;
import cc.azuramc.bedwars.upgrade.TeamUpgrades;
import cc.azuramc.bedwars.util.LoggerUtil;
import lombok.Data;
import lombok.ToString;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author an5w1r@163.com
 */
@Data
@ToString(exclude = {"gameManager", "upgrades"})
public class GameTeam implements IGameTeam {

    @NotNull
    private final GameManager gameManager;

    @NotNull
    private final TeamColor teamColor;
    @NotNull
    private final Location spawnLocation;
    @NotNull
    private final Location resourceDropLocation;
    private int maxPlayers;

    @Nullable
    private Block bedFeet;
    @Nullable
    private Block bedHead;
    @Nullable
    private BlockFace bedFace;
    private boolean rejuvenated;
    private boolean isDestroyed;
    @Nullable
    private GamePlayer destroyPlayer;

    @NotNull
    private TeamUpgrades upgrades;

    public GameTeam(@NotNull GameManager gameManager, @NotNull TeamColor teamColor, @NotNull Location location, @NotNull Location resourceDropLocation, int maxPlayers) {
        this.gameManager = gameManager;
        this.teamColor = Objects.requireNonNull(teamColor, "teamColor");
        this.spawnLocation = Objects.requireNonNull(location, "spawnLocation");
        this.resourceDropLocation = Objects.requireNonNull(resourceDropLocation, "resourceDropLocation");
        this.maxPlayers = maxPlayers;
        this.upgrades = new TeamUpgrades(this);

        new GameTeamBedHandler(this);
        if (bedHead == null) {
            LoggerUtil.warn("No bed found near the " + teamColor.name() + " team spawn " + location + ", the team starts without a bed");
            this.isDestroyed = true;
        }
    }

    public boolean isBedBlock(@NotNull Block block) {
        Location location = block.getLocation();
        return (bedHead != null && bedHead.getLocation().equals(location))
                || (bedFeet != null && bedFeet.getLocation().equals(location));
    }

    @NotNull
    public ChatColor getChatColor() {
        return teamColor.getChatColor();
    }

    @NotNull
    public DyeColor getDyeColor() {
        return teamColor.getDyeColor();
    }

    @NotNull
    public Color getColor() {
        return teamColor.getColor();
    }

    @NotNull
    public String getName() {
        return teamColor.getName();
    }

    @NotNull
    public String getNameWithoutColor() {
        String teamName = teamColor.getName();
        return teamName.length() < 2 ? teamName : teamName.substring(2);
    }

    @NotNull
    public List<GamePlayer> getGamePlayers() {
        List<GamePlayer> teamPlayers = new ArrayList<>();

        for (GamePlayer gamePlayer : GamePlayer.getGamePlayers()) {
            if (gamePlayer.getGameTeam() == this) {
                teamPlayers.add(gamePlayer);
            }
        }

        return Collections.unmodifiableList(teamPlayers);
    }

    @NotNull
    public List<GamePlayer> getAlivePlayers() {
        List<GamePlayer> alivePlayers = new ArrayList<>();

        for (GamePlayer gamePlayer : getGamePlayers()) {
            if (gamePlayer.isOnline() && !gamePlayer.isSpectator()) {
                alivePlayers.add(gamePlayer);
            }
        }

        return Collections.unmodifiableList(alivePlayers);
    }

    public boolean isInTeam(@Nullable IGamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return false;
        }

        return gamePlayer.getGameTeam() == this;
    }

    public boolean isInTeam(@Nullable GamePlayer excludedPlayer, @Nullable GamePlayer targetPlayer) {
        if (targetPlayer == null || excludedPlayer == null) {
            return false;
        }

        return targetPlayer.getGameTeam() == this && !targetPlayer.equals(excludedPlayer);
    }

    public boolean addPlayer(@Nullable GamePlayer gamePlayer) {
        if (gamePlayer == null || isFull() || isInTeam(gamePlayer)) {
            return false;
        }

        gamePlayer.setGameTeam(this);

        return true;
    }

    public boolean isFull() {
        return getGamePlayers().size() >= maxPlayers;
    }

    public boolean isDead() {
        return getAlivePlayers().isEmpty();
    }

    public void setDestroyed(boolean destroyed) {
        this.isDestroyed = destroyed;
        if (destroyed) {
            GameTeamBedHandler.destroyBed(this);
        }
    }

    public int getActivePlayerCount() {
        return getAlivePlayers().size();
    }
}
