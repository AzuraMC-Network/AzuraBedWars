package cc.azuramc.bedwars.database;

import lombok.Getter;

/**
 * @author an5w1r@163.com
 */

@Getter
public enum PlayerShopTable {

    TABLE_NAME("bw_players_shop"),
    KEY("key"),
    DATA("data"),
    CREATED_AT("created_at"),
    UPDATED_AT("updated_at");


    private final String string;

    PlayerShopTable(String string) {
        this.string = string;
    }
}
