package cc.azuramc.bedwars.tablist.util;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.tablist.display.HeaderFooter;
import cc.azuramc.bedwars.util.MessageUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * TabList数据包发送类
 * 负责发送Header和Footer数据包
 *
 * @author an5w1r@163.com
 */
public class PacketSender {

    /**
     * 发送Header和Footer数据包给指定玩家
     *
     * @param player     目标玩家
     * @param header     Header文本
     * @param footer     Footer文本
     * @param gamePlayer 游戏玩家（用于替换占位符）
     */
    public void sendHeaderFooter(Player player, String header, String footer, GamePlayer gamePlayer) {
        if (player == null || !player.isOnline()) {
            return;
        }

        String processedHeader = processPlaceholders(MessageUtil.parse(player, header), gamePlayer);
        String processedFooter = processPlaceholders(MessageUtil.parse(player, footer), gamePlayer);

        // 将String转换为Adventure Component
        Component headerComponent = Component.text(processedHeader);
        Component footerComponent = Component.text(processedFooter);

        // 创建数据包
        WrapperPlayServerPlayerListHeaderAndFooter packet = new WrapperPlayServerPlayerListHeaderAndFooter(headerComponent, footerComponent);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    /**
     * 发送当前设置的Header和Footer给指定玩家
     *
     * @param player              目标玩家
     * @param headerFooter Header/Footer管理器
     */
    public void sendCurrentHeaderFooter(Player player, HeaderFooter headerFooter) {
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        sendHeaderFooter(player, headerFooter.getHeader(), headerFooter.getFooter(), gamePlayer);
    }

    /**
     * 处理文本中的占位符
     *
     * @param text       原始文本
     * @param gamePlayer 游戏玩家
     * @return 处理后的文本
     */
    private String processPlaceholders(String text, GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return text;
        }

        return text
                .replace("<currentGameKill>", String.valueOf(gamePlayer.getCurrentGameKills()))
                .replace("<currentGameFinalKill>", String.valueOf(gamePlayer.getCurrentGameFinalKills()))
                .replace("<currentGameBedBreak>", String.valueOf(gamePlayer.getCurrentGameDestroyedBeds()));
    }
}
