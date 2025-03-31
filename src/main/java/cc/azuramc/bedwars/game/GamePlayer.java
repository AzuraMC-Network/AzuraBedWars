package cc.azuramc.bedwars.game;

import cc.azuramc.bedwars.utils.ActionBarUtil;
import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.utils.TitleUtil;
import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.spectator.SpectatorSettings;
import cc.azuramc.bedwars.spectator.SpectatorTarget;
import cc.azuramc.bedwars.types.ArmorType;
import cc.azuramc.bedwars.types.ToolType;
import cc.azuramc.bedwars.utils.Util;
import cc.azuramc.bedwars.utils.MaterialUtil;
import cc.azuramc.bedwars.utils.EnchantmentUtil;
import cc.azuramc.bedwars.utils.board.FastBoard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GamePlayer {
    private static final ConcurrentHashMap<UUID, GamePlayer> gamePlayers = new ConcurrentHashMap<>();

    @Getter
    private final UUID uuid;
    @Getter
    private final String name;
    @Getter
    private final AssistsMap assistsMap;
    @Getter
    private final PlayerData playerData;
    @Setter
    private String displayname;
    @Getter
    @Setter
    private FastBoard board;
    @Getter
    private boolean spectator;
    @Getter
    @Setter
    private SpectatorTarget spectatorTarget;
    @Getter
    @Setter
    private GameTeam gameTeam;
    @Getter
    private final PlayerCompass playerCompass;
    @Getter
    private int kills;
    @Getter
    private int finalKills;
    @Getter
    @Setter
    private ArmorType armorType;
    @Getter
    @Setter
    private ToolType pickaxeType;
    @Getter
    @Setter
    private ToolType axeType;
    @Getter
    @Setter
    private boolean shear;

    public GamePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;

        this.armorType = ArmorType.DEFAULT;
        this.pickaxeType = ToolType.NONE;
        this.axeType = ToolType.NONE;

        assistsMap = new AssistsMap(this);
        playerData = new PlayerData(this);
        playerCompass = new PlayerCompass(this);
    }

    public static GamePlayer create(UUID uuid, String name) {
        GamePlayer gamePlayer = get(uuid);
        if (gamePlayer != null) {
            return gamePlayer;
        }
        gamePlayer = new GamePlayer(uuid, name);
        gamePlayers.put(uuid, gamePlayer);
        return gamePlayer;
    }

    public static GamePlayer get(UUID uuid) {
        for (GamePlayer gamePlayer : gamePlayers.values()) {
            if (gamePlayer.getUuid().equals(uuid)) {
                return gamePlayer;
            }
        }
        return null;
    }

    public String getDisplayname() {
        return this.name;
    }

    public static List<GamePlayer> getGamePlayers() {
        return new ArrayList<>(gamePlayers.values());
    }

    public static List<GamePlayer> getTeamPlayers() {
        List<GamePlayer> teamPlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : gamePlayers.values()) {
            if (gamePlayer.getGameTeam() != null) {
                teamPlayers.add(gamePlayer);
            }
        }
        return teamPlayers;
    }

    public static List<GamePlayer> getOnlinePlayers() {
        List<GamePlayer> onlinePlayers = new ArrayList<>();
        for (GamePlayer gamePlayer : gamePlayers.values()) {
            if (gamePlayer.isOnline()) {
                onlinePlayers.add(gamePlayer);
            }
        }
        return onlinePlayers;
    }

    public static List<GamePlayer> getSpectators() {
        List<GamePlayer> spectators = new ArrayList<>();
        for (GamePlayer gamePlayer : gamePlayers.values()) {
            if (gamePlayer.isSpectator()) {
                spectators.add(gamePlayer);
            }
        }
        return spectators;
    }

    public static List<GamePlayer> sortFinalKills() {
        List<GamePlayer> list = new ArrayList<>(getOnlinePlayers());
        list.sort((player1, player2) -> player2.getFinalKills() - player1.getFinalKills());
        return list;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    public void sendActionBar(String message) {
        if (!isOnline()) return;
        ActionBarUtil.sendBar(getPlayer(), message);
    }

    public void sendTitle(int fadeIn, int stay, int fadeOut, String title, String subTitle) {
        if (!isOnline()) return;
        TitleUtil.sendTitle(getPlayer(), fadeIn, stay, fadeOut, title, subTitle);
    }

    public void sendMessage(String message) {
        if (!isOnline()) return;
        getPlayer().sendMessage(message);
    }

    public void playSound(Sound sound, float volume, float pitch) {
        if (!isOnline()) return;
        getPlayer().playSound(getPlayer().getLocation(), sound, volume, pitch);
    }

    public void setSpectator() {
        spectator = true;
    }

    public void toSpectator(String title, String subTitle) {
        spectator = true;
        spectatorTarget = new SpectatorTarget(this, null);

        Player player = getPlayer();
        sendTitle(10, 20, 10, title, subTitle);
        getOnlinePlayers().forEach((gamePlayer1 -> {
            try {
                // 尝试使用新版本API (1.12.2+)
                gamePlayer1.getPlayer().hidePlayer(AzuraBedWars.getInstance(), player);
            } catch (Throwable e) {
                // 如果新版本API不可用，回退到旧版本API
                gamePlayer1.getPlayer().hidePlayer(player);
            }
        }));
        player.setGameMode(GameMode.ADVENTURE);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        SpectatorSettings spectatorSettings = SpectatorSettings.get(this);
        if (spectatorSettings.getOption(SpectatorSettings.Option.NIGHTVISION)) {
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
        }

        player.getInventory().setItem(0, new ItemBuilderUtil().setType(MaterialUtil.COMPASS()).setDisplayName("§a§l传送器§7(右键打开)").getItem());
        player.getInventory().setItem(4, new ItemBuilderUtil().setType(MaterialUtil.COMPARATOR()).setDisplayName("§c§l旁观者设置§7(右键打开)").getItem());
        player.getInventory().setItem(7, new ItemBuilderUtil().setType(Material.PAPER).setDisplayName("§b§l快速加入§7(右键加入)").getItem());
        player.getInventory().setItem(8, new ItemBuilderUtil().setType(MaterialUtil.SLIME_BALL()).setDisplayName("§c§l离开游戏§7(右键离开)").getItem());

        player.setAllowFlight(true);
        Util.setFlying(player);
        player.teleport(AzuraBedWars.getInstance().getGame().getMapData().getReSpawn().toLocation());

        if (gameTeam != null && !gameTeam.getAlivePlayers().isEmpty()) {
            spectatorTarget.setTarget(gameTeam.getAlivePlayers().get(0));
        }
    }

    public void addKills() {
        kills += 1;
    }

    public void addFinalKills() {
        finalKills += 1;
    }

    public void setLastDamage(GamePlayer damager, long time) {
        assistsMap.setLastDamage(damager, time);
    }

    public void giveInventory() {
        Player player = getPlayer();
        player.getInventory().setHelmet(new ItemBuilderUtil().setType(MaterialUtil.LEATHER_HELMET()).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
        player.getInventory().setChestplate(new ItemBuilderUtil().setType(MaterialUtil.LEATHER_CHESTPLATE()).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
        giveArmor();
        giveSword(false);
        givePickaxe(false);
        giveAxe(false);
        giveShear();

        player.updateInventory();
    }

    public void giveArmor() {
        Player player = getPlayer();

        switch (armorType) {
            case CHAINMAIL:
                player.getInventory().setLeggings(new ItemBuilderUtil().setType(MaterialUtil.CHAINMAIL_LEGGINGS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilderUtil().setType(MaterialUtil.CHAINMAIL_BOOTS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
            case IRON:
                player.getInventory().setLeggings(new ItemBuilderUtil().setType(MaterialUtil.IRON_LEGGINGS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilderUtil().setType(MaterialUtil.IRON_BOOTS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
            case DIAMOND:
                player.getInventory().setLeggings(new ItemBuilderUtil().setType(MaterialUtil.DIAMOND_LEGGINGS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilderUtil().setType(MaterialUtil.DIAMOND_BOOTS()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
            default:
                player.getInventory().setLeggings(new ItemBuilderUtil().setType(MaterialUtil.LEATHER_LEGGINGS()).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                player.getInventory().setBoots(new ItemBuilderUtil().setType(MaterialUtil.LEATHER_BOOTS()).setColor(gameTeam.getColor()).setUnbreakable(true, true).addItemFlag(ItemFlag.HIDE_ATTRIBUTES).getItem());
                break;
        }

        if (gameTeam.getReinforcedArmor() > 0) {
            for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
                ItemStack armor = player.getInventory().getArmorContents()[i];
                if (armor != null) {
                    armor.addEnchantment(EnchantmentUtil.PROTECTION_ENVIRONMENTAL(), gameTeam.getReinforcedArmor());
                }
            }
        }
    }

    public void giveSword(boolean remove) {
        Player player = getPlayer();

        if (remove) {
            player.getInventory().remove(MaterialUtil.WOODEN_SWORD());
        }
        if (gameTeam.isSharpenedSwords()) {
            player.getInventory().addItem(new ItemBuilderUtil()
                .setType(MaterialUtil.WOODEN_SWORD())
                .addEnchant(EnchantmentUtil.DAMAGE_ALL(), 1)
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .setUnbreakable(true, true)
                .getItem());
        } else {
            player.getInventory().addItem(new ItemBuilderUtil()
                .setType(MaterialUtil.WOODEN_SWORD())
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .setUnbreakable(true, true)
                .getItem());
        }
    }

    public void givePickaxe(boolean remove) {
        Player player = getPlayer();

        switch (pickaxeType) {
            case WOOD:
                player.getInventory().addItem(new ItemBuilderUtil()
                    .setType(MaterialUtil.WOODEN_PICKAXE())
                    .setUnbreakable(true, true)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .getItem());
                break;
            case STONE:
                if (remove) player.getInventory().remove(MaterialUtil.WOODEN_PICKAXE());
                player.getInventory().addItem(new ItemBuilderUtil()
                    .setType(MaterialUtil.STONE_PICKAXE())
                    .setUnbreakable(true, true)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .getItem());
                break;
            case IRON:
                if (remove) player.getInventory().remove(MaterialUtil.STONE_PICKAXE());
                player.getInventory().addItem(new ItemBuilderUtil()
                    .setType(MaterialUtil.IRON_PICKAXE())
                    .setUnbreakable(true, true)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .getItem());
                break;
            case DIAMOND:
                if (remove) player.getInventory().remove(MaterialUtil.IRON_PICKAXE());
                player.getInventory().addItem(new ItemBuilderUtil()
                    .setType(MaterialUtil.DIAMOND_PICKAXE())
                    .setUnbreakable(true, true)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .getItem());
                break;
            default:
                break;
        }
    }

    public void giveAxe(boolean remove) {
        Player player = getPlayer();

        switch (axeType) {
            case WOOD:
                player.getInventory().addItem(new ItemBuilderUtil()
                    .setType(MaterialUtil.WOODEN_AXE())
                    .setUnbreakable(true, true)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .getItem());
                break;
            case STONE:
                if (remove) player.getInventory().remove(MaterialUtil.WOODEN_AXE());
                player.getInventory().addItem(new ItemBuilderUtil()
                    .setType(MaterialUtil.STONE_AXE())
                    .setUnbreakable(true, true)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .getItem());
                break;
            case IRON:
                if (remove) player.getInventory().remove(MaterialUtil.STONE_AXE());
                player.getInventory().addItem(new ItemBuilderUtil()
                    .setType(MaterialUtil.IRON_AXE())
                    .setUnbreakable(true, true)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .getItem());
                break;
            case DIAMOND:
                if (remove) player.getInventory().remove(MaterialUtil.IRON_AXE());
                player.getInventory().addItem(new ItemBuilderUtil()
                    .setType(MaterialUtil.DIAMOND_AXE())
                    .setUnbreakable(true, true)
                    .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                    .addEnchant(EnchantmentUtil.DIG_SPEED(), 1)
                    .getItem());
                break;
            default:
                break;
        }
    }

    public void giveShear() {
        Player player = getPlayer();

        if (shear) {
            player.getInventory().addItem(new ItemBuilderUtil()
                .setType(MaterialUtil.SHEARS())
                .setUnbreakable(true, true)
                .addItemFlag(ItemFlag.HIDE_ATTRIBUTES)
                .getItem());
        }
    }

    public void clean() {
        Player player = getPlayer();
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setExp(0.0F);
        player.setLevel(0);
        player.setSneaking(false);
        player.setSprinting(false);
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setExhaustion(0.0f);
        player.setMaxHealth(20.0D);
        player.setHealth(20.0f);
        player.setFireTicks(0);

        PlayerInventory inv = player.getInventory();
        inv.setArmorContents(new ItemStack[4]);
        inv.setContents(new ItemStack[]{});
        player.getActivePotionEffects().forEach((potionEffect -> player.removePotionEffect(potionEffect.getType())));
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GamePlayer)) {
            return false;
        }

        GamePlayer gamePlayer = (GamePlayer) obj;
        return uuid.equals(gamePlayer.getUuid());
    }

    public static class PlayerCompass {
        @Getter
        private final GamePlayer gamePlayer;
        @Getter
        private final Player player;

        public PlayerCompass(GamePlayer gamePlayer) {
            this.gamePlayer = gamePlayer;
            this.player = gamePlayer.getPlayer();
        }

        public void sendClosestPlayer() {
            GamePlayer closestPlayer = AzuraBedWars.getInstance().getGame().findTargetPlayer(gamePlayer);

            if (closestPlayer != null) {
                gamePlayer.sendActionBar("§f玩家 " + closestPlayer.getGameTeam().getChatColor() + closestPlayer.getDisplayname() + " §f距离您 " + ((int) closestPlayer.getPlayer().getLocation().distance(player.getLocation())) + "m");
                player.setCompassTarget(closestPlayer.getPlayer().getLocation());
            } else {
                gamePlayer.sendActionBar("§c没有目标");
            }
        }
    }
}
