package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.MessageUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ant1aura@qq.com
 */
@Data
@EqualsAndHashCode
public class ChatConfig {
    private String globalChatPrefix = MessageUtil.color("!");
    private String spectatorPrefix = MessageUtil.color("&7[旁观者]");
    private String globalChatTag = MessageUtil.color("&6[全局]");
    private String teamChatTag = MessageUtil.color("&9[团队]");
    private String chatSeparator = MessageUtil.color("&7: ");

    private int globalChatCooldown = 10;
}
