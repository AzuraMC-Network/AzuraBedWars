package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Raw;
import cc.azuramc.bedwars.config.annotation.RestartRequired;
import cc.azuramc.bedwars.database.storage.MapStorageType;
import cc.azuramc.bedwars.game.economy.EconomyType;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("AzuraBedWars server settings")
public class SettingsConfig {

    @Comment({"Language file name in lang/", "Options: en_US, zh_CN"})
    @Raw
    private String language = "en_US";

    @Comment("Editor mode for setting up maps")
    @RestartRequired
    private boolean editorMode = false;

    @Comment("Debug logging")
    private boolean debugMode = false;

    @Comment({"Economy mode", "ITEM: resources are picked up and spent as items", "EXPERIENCE: resources turn into levels and prices are in levels"})
    @RestartRequired
    private EconomyType economyMode = EconomyType.ITEM;

    @Comment({"Map storage", "Options: JSON, MYSQL"})
    @RestartRequired
    private MapStorageType mapStorage = MapStorageType.JSON;

    @Comment("Default map name")
    @Raw
    @RestartRequired
    private String defaultMapName = "game";

    @Comment("Let players pick a team")
    private boolean enableTeamSelection = true;

    @Comment("Play again command")
    @Raw
    private String playAgainCommand = "/not impl now";

    @Comment({"BungeeCord server players go to when they leave or the game ends", "Empty to kick the player"})
    @Raw
    private String lobbyServer = "G_BedwarsLobby#1";

    /**
     * Servers set up before lang/ existed keep the Chinese text
     */
    public static Map<String, Object> fromVersion1(Map<String, Object> old) {
        Map<String, Object> upgraded = new LinkedHashMap<>();
        upgraded.put("language", "zh_CN");
        upgraded.putAll(old);
        return upgraded;
    }
}
