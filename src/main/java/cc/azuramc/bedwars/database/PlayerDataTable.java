package cc.azuramc.bedwars.database;

import lombok.Getter;

/**
 * @author an5w1r@163.com
 */

@Getter
public enum PlayerDataTable {
    // PLAYER DATA TABLE
    TABLE_NAME("bw_players_data"),
    KEY("key"),
    NAME("name"),
    MODE("mode"),
    KILLS("kills"),
    DEATHS("deaths"),
    DESTROYED_BEDS("destroyed_beds"),
    WINS("wins"),
    LOSSES("losses"),
    GAMES("games"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");
    private final String string;

    PlayerDataTable(String string) {
        this.string = string;
    }

}
