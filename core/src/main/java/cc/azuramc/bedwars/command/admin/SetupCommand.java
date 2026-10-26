package cc.azuramc.bedwars.command.admin;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.LangConfig;
import cc.azuramc.bedwars.listener.setup.SetupItemListener;
import cc.azuramc.bedwars.util.MessageUtil;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;


/**
 * @author an5w1r@163.com
 */
@Command("setup")
@CommandPermission("azurabedwars.admin")
public class SetupCommand {

    private final AzuraBedWars plugin = AzuraBedWars.getInstance();
    private final SetupItemListener setup;

    public SetupCommand(SetupItemListener setup) {
        this.setup = setup;
    }

    @DefaultFor("setup")
    public void help(Player player) {
        player.sendMessage(MessageUtil.CHAT_BAR);
        for (String line : lang().getHelp()) {
            player.sendMessage(MessageUtil.fill(line, "version", plugin.getPlugin().getDescription().getVersion()));
        }
        player.sendMessage(MessageUtil.CHAT_BAR);
    }

    @Subcommand("start")
    @AutoComplete("@mapNames")
    public void start(Player player, String mapName) {
        if (plugin.getMapManager().getMap(mapName) == null) {
            player.sendMessage(MessageUtil.fill(plugin.getLang().getMap().getNotFoundCreate(), "map", mapName));
            return;
        }
        setup.start(player, mapName);
        player.sendMessage(MessageUtil.fill(lang().getStarted(), "map", mapName));
    }

    @Subcommand("stop")
    public void stop(Player player) {
        player.sendMessage(setup.stop(player) ? lang().getStopped() : lang().getNotActive());
    }

    private LangConfig.Setup lang() {
        return plugin.getLang().getSetup();
    }
}
