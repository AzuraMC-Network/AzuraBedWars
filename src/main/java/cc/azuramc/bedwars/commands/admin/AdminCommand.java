package cc.azuramc.bedwars.commands.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.utils.CC;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Dependency;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"bedwars", "bw", "azurabedwars"})
@CommandPermission("azurabedwars.admin")
public class AdminCommand {

    @Dependency
    private AzuraBedWars pluginDependency;

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();

    @DefaultFor({"bedwars", "bw", "azurabedwars"})
    public void getHelpCommand(Player player) {
        player.sendMessage(CC.color("&b&lAzuraBedWars &8- &7v" + plugin.getDescription().getVersion() + " &8- &b起床战争 - 指令帮助"));
        player.sendMessage("");
        player.sendMessage(CC.color("&7 • &f/bw map &7查看地图相关指令帮助"));
        player.sendMessage("");
    }
}
