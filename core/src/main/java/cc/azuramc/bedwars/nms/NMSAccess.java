package cc.azuramc.bedwars.nms;

import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * @author An5w1r@163.com
 */
public interface NMSAccess {
    void hideArmor(Player victim, Player receiver);
    void showArmor(Player victim, Player receiver);
    void hideArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList);
    void showArmor(GamePlayer gamePlayer, List<GamePlayer> receiverList);
    Fireball setFireballDirection(Fireball fireball, Vector vector);
    void registerCustomEntities();
    LivingEntity spawnIronGolem(Location loc, GameTeam gameTeam, double speed, double health, int despawn);
    LivingEntity spawnSilverfish(Location loc, GameTeam gameTeam, double speed, double health, int despawn, double damage);
    void placeLadder(Block block, int x, int y, int z, int data);
}
