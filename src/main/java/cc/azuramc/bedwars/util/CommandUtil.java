package cc.azuramc.bedwars.util;

import revxrsal.commands.bukkit.BukkitCommandActor;

import java.util.List;

/**
 * @author an5w1r@163.com
 */
public class CommandUtil {

    public static void sendLayout(BukkitCommandActor actor, String message) {
        if (actor.isPlayer()) {
            actor.requirePlayer().sendMessage(ChatColorUtil.color(message));
        } else {
            actor.requireConsole().sendMessage(ChatColorUtil.color(message));
        }
    }

    public static void sendLayout(BukkitCommandActor actor, List<String> message) {
        if (actor.isPlayer()) {
            ChatColorUtil.color(message).forEach(actor.requirePlayer()::sendMessage);
        } else {
            ChatColorUtil.color(message).forEach(actor.requireConsole()::sendMessage);
        }
    }
}
