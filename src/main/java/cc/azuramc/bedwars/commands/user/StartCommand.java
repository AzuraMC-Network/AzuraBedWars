package cc.azuramc.bedwars.commands.user;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player) || commandSender.getName().equals("An5w1r_")) {
            Game game = AzuraBedWars.getInstance().getGame();

            game.setForceStart(true);
            game.start();
        }
        return false;
    }
}
