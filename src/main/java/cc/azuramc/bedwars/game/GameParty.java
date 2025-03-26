package cc.azuramc.bedwars.game;

import lombok.Getter;

import java.util.List;

public class GameParty {
    private final Game game;
    @Getter
    private final GamePlayer leader;
    @Getter
    private final List<GamePlayer> players;

    public GameParty(Game game, GamePlayer leader, List<GamePlayer> gamePlayers) {
        this.game = game;
        this.leader = leader;
        players = gamePlayers;
        players.add(leader);
        game.addParty(this);
    }

    public boolean isLeader(GamePlayer p) {
        return p.equals(leader);
    }

    public boolean isInTeam(GamePlayer player) {
        return players.contains(player);
    }

    public void removePlayer(GamePlayer p) {
        if (!isInTeam(p)) {
            return;
        }
        players.remove(p);
        if (players.isEmpty()) {
            game.removeParty(this);
        }
    }
}
