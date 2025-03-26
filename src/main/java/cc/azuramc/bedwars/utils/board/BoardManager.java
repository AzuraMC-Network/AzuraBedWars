package cc.azuramc.bedwars.utils.board;

import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoardManager implements Listener {
    private Map<UUID, Board> boardMap;

    public BoardManager() {
        this.boardMap = new HashMap<UUID, Board>();
        this.boardMap = new HashMap<UUID, Board>();
    }

    public Map<UUID, Board> getBoardMap() {
        return this.boardMap;
    }

    public void setBoardMap(final Map<UUID, Board> boardMap) {
        this.boardMap = boardMap;
    }
}
