package cc.azuramc.bedwars.tablist;

import cc.azuramc.bedwars.game.GamePlayer;
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
public class TabListPacketSender {

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

        String processedHeader = MessageUtil.parse(player, header);
        String processedFooter = processFooterPlaceholders(MessageUtil.parse(player, footer), gamePlayer);

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
     * @param headerFooterManager Header/Footer管理器
     */
    public void sendCurrentHeaderFooter(Player player, HeaderFooterManager headerFooterManager) {
        GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());
        sendHeaderFooter(player, headerFooterManager.getHeader(), headerFooterManager.getFooter(), gamePlayer);
    }

    /**
     * 处理Footer中的占位符
     *
     * @param footer     原始Footer文本
     * @param gamePlayer 游戏玩家
     * @return 处理后的Footer文本
     */
    private String processFooterPlaceholders(String footer, GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return footer;
        }

        return footer
                .replace("<currentGameKill>", String.valueOf(gamePlayer.getCurrentGameKills()))
                .replace("<currentGameFinalKill>", String.valueOf(gamePlayer.getCurrentGameFinalKills()))
                .replace("<currentGameBedBreak>", String.valueOf(gamePlayer.getCurrentGameDestroyedBeds()));
    }
}
