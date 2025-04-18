package cc.azuramc.bedwars.compat.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TitleUtil {

    /**
     * 给一个玩家发送Title信息
     *
     * @param player   发送的玩家
     * @param fadeIn   淡入时间
     * @param stay     停留时间
     * @param fadeOut  淡出时间
     * @param title    主标题
     * @param subTitle 副标题
     */
    @SuppressWarnings("deprecation")
    public static void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subTitle) {
        ProtocolManager pm = ProtocolLibrary.getProtocolManager();
        
        try {
            // 尝试使用1.17+新版API (SET_TITLE_TEXT, SET_SUBTITLE_TEXT, SET_TITLES_ANIMATION)
            if (title != null) {
                String translatedTitle = ChatColor.translateAlternateColorCodes('&', title);
                translatedTitle = translatedTitle.replaceAll("%player%", player.getName());
                
                try {
                    // 尝试使用SET_TITLE_TEXT (1.17+)
                    PacketContainer packet = pm.createPacket(PacketType.Play.Server.SET_TITLE_TEXT);
                    packet.getChatComponents().write(0, WrappedChatComponent.fromText(translatedTitle));
                    pm.sendServerPacket(player, packet, false);
                } catch (Throwable e) {
                    // 回退到旧版本TITLE (1.8-1.16)
                    PacketContainer packet = pm.createPacket(PacketType.Play.Server.TITLE);
                    packet.getTitleActions().write(0, EnumWrappers.TitleAction.TITLE);
                    packet.getChatComponents().write(0, WrappedChatComponent.fromText(translatedTitle));
                    pm.sendServerPacket(player, packet, false);
                }
            }

            if (subTitle != null) {
                String translatedSubTitle = ChatColor.translateAlternateColorCodes('&', subTitle);
                translatedSubTitle = translatedSubTitle.replaceAll("%player%", player.getName());

                try {
                    // 尝试使用SET_SUBTITLE_TEXT (1.17+)
                    PacketContainer packet = pm.createPacket(PacketType.Play.Server.SET_SUBTITLE_TEXT);
                    packet.getChatComponents().write(0, WrappedChatComponent.fromText(translatedSubTitle));
                    pm.sendServerPacket(player, packet, false);
                } catch (Throwable e) {
                    // 回退到旧版本TITLE (1.8-1.16)
                    PacketContainer packet = pm.createPacket(PacketType.Play.Server.TITLE);
                    packet.getTitleActions().write(0, EnumWrappers.TitleAction.SUBTITLE);
                    packet.getChatComponents().write(0, WrappedChatComponent.fromText(translatedSubTitle));
                    pm.sendServerPacket(player, packet, false);
                }
            }
            
            try {
                // 尝试使用SET_TITLES_ANIMATION (1.17+)
                PacketContainer packet = pm.createPacket(PacketType.Play.Server.SET_TITLES_ANIMATION);
                packet.getIntegers().write(0, fadeIn).write(1, stay).write(2, fadeOut);
                pm.sendServerPacket(player, packet, false);
            } catch (Throwable e) {
                // 回退到旧版本TITLE (1.8-1.16)
                PacketContainer packet = pm.createPacket(PacketType.Play.Server.TITLE);
                packet.getTitleActions().write(0, EnumWrappers.TitleAction.TIMES);
                packet.getIntegers().write(0, fadeIn).write(1, stay).write(2, fadeOut);
                pm.sendServerPacket(player, packet, false);
            }
        } catch (Exception e) {
            // 如果发生其他错误，则使用原生Bukkit API (最后的回退)
            try {
                player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
            } catch (Throwable ex) {
                // 无法发送标题，可能是非常旧的版本
            }
        }
    }
}