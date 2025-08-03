package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;

/**
 * 设置配置对象
 * 存储插件的基本设置信息
 * @author an5w1r@163.com
 */
@Data
public class SettingsConfig {
    private boolean editorMode = false;
    private boolean debugMode = false;
    private final int teamBlockSearchRadius = 15;
    private String mapStorage = "JSON";
    private boolean enabledJedisMapFeature = false;
    private String defaultMapName = "game";

    private DatabaseConfig database = new DatabaseConfig();
    private ChatConfig chatConfig = new ChatConfig();
    private DisplayDamage displayDamage = new DisplayDamage();
    private SetupMap setupMap = new SetupMap();

    @Data
    public static class DatabaseConfig {
        private String host = "localhost";
        private int port = 3306;
        private String username = "root";
        private String password = "123456";
        private String database = "azurabw";
    }

    @Data
    public static class ChatConfig {
        private String globalChatPrefix = MessageUtil.color("!");
        private String spectatorPrefix = MessageUtil.color("&7[旁观者]");
        private String globalChatTag = MessageUtil.color("&6[全局]");
        private String teamChatTag = MessageUtil.color("&9[团队]");
        private String chatSeparator = MessageUtil.color("&7: ");

        private int globalChatCooldown = 10;
    }


    @Data
    public static class DisplayDamage {
        private final boolean arrowDisplayEnabled = true;
        private final boolean attackDisplayEnabled = true;
    }

    @Data
    public static class SetupMap {

    }
}
