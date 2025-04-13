package cc.azuramc.bedwars.utils.command;

import cc.azuramc.bedwars.utils.chat.ChatColorUtil;
import revxrsal.commands.bukkit.BukkitCommandActor;

import java.util.List;

public class CommandHelper {

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
