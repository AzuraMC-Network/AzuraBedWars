package cc.azuramc.bedwars.utils;

import revxrsal.commands.bukkit.BukkitCommandActor;

import java.util.List;

public class CommandUtil {

    public static void sendLayout(BukkitCommandActor actor, String message) {
        if (actor.isPlayer()) {
            actor.requirePlayer().sendMessage(CC.color(message));
        } else {
            actor.requireConsole().sendMessage(CC.color(message));
        }
    }

    public static void sendLayout(BukkitCommandActor actor, List<String> message) {
        if (actor.isPlayer()) {
            CC.color(message).forEach(actor.requirePlayer()::sendMessage);
        } else {
            CC.color(message).forEach(actor.requireConsole()::sendMessage);
        }
    }
}
