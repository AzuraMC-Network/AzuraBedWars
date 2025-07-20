package cc.azuramc.bedwars.listener.projectile;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.config.object.ItemConfig;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.team.GameTeam;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * @author an5w1r@163.com
 */
public class FireballHandler implements Listener {

    private static final ItemConfig.FireBall CONFIG = AzuraBedWars.getInstance().getItemConfig().getFireBall();

    private static final int FIREBALL_EXPLOSION_RADIUS_X = CONFIG.getFireballExplosionRadiusX();
    private static final int FIREBALL_EXPLOSION_RADIUS_Y = CONFIG.getFireballExplosionRadiusY();
    private static final int FIREBALL_EXPLOSION_RADIUS_Z = CONFIG.getFireballExplosionRadiusZ();
    private static final int FIREBALL_DAMAGE = CONFIG.getFireballDamage();
    private static final double FIREBALL_KNOCK_BACK_MULTIPLIER = CONFIG.getFireballKnockbackMultiplier();
    public static final String FIREBALL_METADATA = "GAME_FIREBALL";
    public static final String NO_FALL_DAMAGE_METADATA = "FIREBALL_PLAYER_FALL_MODIFY";


    /**
     * 处理火球爆炸
     *
     * @param fireball 火球实体
     */
    public static void handleFireballExplosion(Fireball fireball) {
        // 检查是否是玩家发射的火球
        if (!fireball.hasMetadata(FIREBALL_METADATA)) {
            return;
        }

        // 获取火球发射者
        GamePlayer ownerPlayer = GamePlayer.get((UUID) fireball.getMetadata(FIREBALL_METADATA).get(0).value());
        if (ownerPlayer == null) {
            return;
        }

        // 处理火球爆炸范围内的玩家
        for (Entity entity : fireball.getNearbyEntities(
                FIREBALL_EXPLOSION_RADIUS_X,
                FIREBALL_EXPLOSION_RADIUS_Y,
                FIREBALL_EXPLOSION_RADIUS_Z)) {

            if (!(entity instanceof Player player)) {
                continue;
            }

            GamePlayer gamePlayer = GamePlayer.get(player.getUniqueId());

            // 检查是否是队友
            if (isTeammateFireball(ownerPlayer, gamePlayer)) {
                continue;
            }

            // 对敌人造成伤害和击退
            if (gamePlayer != null) {
                applyFireballDamage(gamePlayer, ownerPlayer, fireball);
            }
        }
    }

    /**
     * 检查是否是火球发射者的队友
     *
     * @param ownerPlayer 火球发射者
     * @param targetPlayer 目标玩家
     * @return 如果是队友返回true，否则返回false
     */
    private static boolean isTeammateFireball(GamePlayer ownerPlayer, GamePlayer targetPlayer) {
        GameTeam ownerTeam = ownerPlayer.getGameTeam();
        return ownerTeam != null && ownerTeam.isInTeam(ownerPlayer, targetPlayer);
    }

    /**
     * 对玩家应用火球伤害和击退效果
     *
     * @param gamePlayer 游戏玩家对象
     * @param ownerPlayer 火球发射者
     * @param fireball 火球实体
     */
    private static void applyFireballDamage(GamePlayer gamePlayer, GamePlayer ownerPlayer, Fireball fireball) {
        Player player = gamePlayer.getPlayer();
        // 造成伤害
        player.damage(FIREBALL_DAMAGE);

        // 记录伤害来源（用于助攻系统）
        gamePlayer.getAssistsManager().setLastDamage(ownerPlayer, System.currentTimeMillis());

        // 设置元数据以防止掉落伤害
        player.setMetadata(NO_FALL_DAMAGE_METADATA, new FixedMetadataValue(AzuraBedWars.getInstance(), ownerPlayer.getUuid()));

        // 应用击退效果
        Vector knockbackVector = getPosition(player.getLocation(), fireball.getLocation(), 2.1D);
        player.setVelocity(knockbackVector.multiply(FIREBALL_KNOCK_BACK_MULTIPLIER));
    }

    public static Vector getPosition(Location location1, Location location2, double y) {
        double x = location1.getX() - location2.getX();
        double z = location1.getZ() - location2.getZ();
        return new Vector(x, y, z);
    }
}
