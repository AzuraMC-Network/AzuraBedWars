package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.util.ChatColorUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ChatConfig {
    private String globalChatPrefix = ChatColorUtil.color("!");
    private String spectatorPrefix = ChatColorUtil.color("&7[旁观者]");
    private String globalChatTag = ChatColorUtil.color("&6[全局]");
    private String teamChatTag = ChatColorUtil.color("&9[团队]");
    private String chatSeparator = ChatColorUtil.color("&7: ");

    private int globalChatCooldown = 10;
}
