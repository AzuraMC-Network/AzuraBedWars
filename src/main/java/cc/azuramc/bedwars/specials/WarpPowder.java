package cc.azuramc.bedwars.specials;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.Game;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.utils.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WarpPowder extends SpecialItem {
    private final int fullTeleportingTime = 6;
    private Game game = AzuraBedWars.getInstance().getGame();
    private GamePlayer gamePlayer = null;
    private ItemStack stack = null;
    private BukkitTask teleportingTask = null;
    private double teleportingTime = 6.0;

    public WarpPowder() {
        super();
    }

    public void cancelTeleport(boolean removeSpecial, boolean showMessage) {
        Player player = gamePlayer.getPlayer();

        this.teleportingTask.cancel();
        this.teleportingTime = 6;

        player.setLevel(0);

        if (removeSpecial) {
            game.removeSpecialItem(this);
        }

        if (showMessage) {
            gamePlayer.sendMessage("§c你的传送被取消!");
        }

        setStackAmount(this.getStack().getAmount() - 1);
        player.getInventory().setItem(player.getInventory().first(getCancelItemStack()), stack);
        player.updateInventory();
    }

    @Override
    public Material getActivatedMaterial() {
        return MaterialUtil.getMaterial("GLOWSTONE_DUST", "GLOWSTONE_DUST");
    }

    private ItemStack getCancelItemStack() {
        ItemStack glowstone = new ItemStack(this.getActivatedMaterial(), 1);
        ItemMeta meta = glowstone.getItemMeta();
        meta.setDisplayName("§4取消传送");
        glowstone.setItemMeta(meta);

        return glowstone;
    }

    @Override
    public Material getItemMaterial() {
        return MaterialUtil.GUNPOWDER();
    }

    public GamePlayer getPlayer() {
        return gamePlayer;
    }

    public void setPlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public void runTask() {
        final int circles = 15;
        final double height = 2.0;
        Player player = gamePlayer.getPlayer();


        stack = MaterialUtil.getItemInHand(player);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), this.getCancelItemStack());
        player.updateInventory();

        teleportingTime = 6;
        gamePlayer.sendMessage("§a在 §c" + this.fullTeleportingTime + "§a 秒后你将被传送，请不要移动!");

        this.teleportingTask = new BukkitRunnable() {

            public double through = 0.0;

            @Override
            public void run() {
                try {
                    int circleElements = 20;
                    double radius = 1.0;
                    double height2 = 1.0;
                    double circles = 15.0;
                    double fulltime = WarpPowder.this.fullTeleportingTime;
                    double teleportingTime = WarpPowder.this.teleportingTime;

                    double perThrough = (Math.ceil((height / circles) * ((fulltime * 20) / circles)) / 20);

                    WarpPowder.this.teleportingTime = teleportingTime - perThrough;
                    GameTeam gameTeam = GamePlayer.get(player.getUniqueId()).getGameTeam();
                    Location tLoc = gameTeam.getSpawn();

                    if (WarpPowder.this.teleportingTime <= 1.0) {
                        player.teleport(gameTeam.getSpawn());
                        WarpPowder.this.cancelTeleport(true, false);
                        return;
                    }

                    player.setLevel((int) WarpPowder.this.teleportingTime);

                    Location loc = player.getLocation();

                    double y = (height2 / circles) * through;
                    for (int i = 0; i < 20; i++) {
                        double alpha = (360.0 / circleElements) * i;
                        double x = radius * Math.sin(Math.toRadians(alpha));
                        double z = radius * Math.cos(Math.toRadians(alpha));

                        Location particleFrom = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + y, loc.getZ() + z);
                        Util.spawnParticle(GamePlayer.getOnlinePlayers(), particleFrom);

                        Location particleTo = new Location(tLoc.getWorld(), tLoc.getX() + x, tLoc.getY() + y, tLoc.getZ() + z);
                        Util.spawnParticle(GamePlayer.getOnlinePlayers(), particleTo);
                    }

                    this.through += 1.0;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.cancel();
                    WarpPowder.this.cancelTeleport(true, false);
                }
            }
        }.runTaskTimer(AzuraBedWars.getInstance(), 0L,
                (long) Math.ceil((height / circles) * ((this.fullTeleportingTime * 20) / circles)));
        this.game.addSpecialItem(this);
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setStackAmount(int amount) {
        this.stack.setAmount(amount);
    }
}
