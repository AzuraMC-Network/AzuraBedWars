package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Raw;
import cc.azuramc.bedwars.config.annotation.RestartRequired;
import cc.azuramc.bedwars.database.storage.MapStorageType;
import cc.azuramc.bedwars.game.economy.EconomyType;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment("AzuraBedWars 服务器设置")
public class SettingsConfig {

    @Comment("是否启用编辑器模式(用于地图编辑)")
    @RestartRequired
    private boolean editorMode = false;

    @Comment("是否启用调试模式(输出详细日志)")
    private boolean debugMode = false;

    @Comment({"资源模式", "ITEM: 资源以物品形式拾取和消费", "EXPERIENCE: 资源拾取后转为经验等级 商品按经验定价"})
    @RestartRequired
    private EconomyType economyMode = EconomyType.ITEM;

    @Comment({"地图存储方式", "可选: JSON, MYSQL"})
    @RestartRequired
    private MapStorageType mapStorage = MapStorageType.JSON;

    @Comment("默认地图名称")
    @Raw
    @RestartRequired
    private String defaultMapName = "game";

    @Comment("是否启用队伍选择")
    private boolean enableTeamSelection = true;

    @Comment("再来一局命令")
    @Raw
    private String playAgainCommand = "/not impl now";

    @Comment({"离开游戏和游戏结束时前往的 BungeeCord 子服", "留空则直接踢出玩家"})
    @Raw
    private String lobbyServer = "G_BedwarsLobby#1";
}
