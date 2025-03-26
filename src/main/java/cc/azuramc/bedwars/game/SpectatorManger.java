package cc.azuramc.bedwars.game;


public class SpectatorManger/* implements Runnable, Listener*/ {
/*    private final Game game;
    @Getter
    private final List<String> spectators;
    private final Map<String, String> spectatorPlayers;

    public SpectatorManger(Game game) {
        this.game = game;
        spectators = new ArrayList<>();
        spectatorPlayers = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, XBedwars.getInstance());
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(XBedwars.getInstance(), this, 0, 1);
    }

    public void addSpectator(Player player) {
        spectators.add(player.getName());
        player.setGameMode(GameMode.ADVENTURE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        player.setAllowFlight(true);
        Util.setFlying(player);
    }

    public void addSpectator(String name) {
        spectators.add(name);
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player.getName());
    }

    public boolean isSpectator(String name) {
        return spectators.contains(name);
    }

    @Override
    public void run() {
        if (game.getGameState() != GameState.RUNNING) {
            return;
        }

        for (String name : spectators) {
            Player player = Bukkit.getPlayerExact(name);
            if (player == null) continue;

            if (player.getLocation().getY() < 0.0D) {
                player.teleport(player.getLocation().add(0.0D, 128.0D, 0.0D));
                return;
            }

            if (player.getSpectatorTarget() == null) {
                if (spectatorPlayers.containsKey(player.getName())) {
                    spectatorPlayers.remove(player.getName());
                    XBedwars.getInstance().mainThreadRunnable(() -> {
                        player.setSpectatorTarget(null);
                        levelSpectator(player);
                    });
                }
                return;
            }
            if (!(player.getSpectatorTarget() instanceof Player)) return;

            Player spectatorTarget = (Player) player.getSpectatorTarget();
            TitleUtil.sendTitle(player, 0, 20, 0, "§a正在旁观§7" + Nick.get().getCache().getOrDefault(spectatorTarget.getName(), spectatorTarget.getName()), "§a点击左键打开菜单   §c按Shift键退出");
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (game.getGameState() != GameState.RUNNING) return;
        if (!isSpectator(player)) return;
        if (player.getGameMode() != GameMode.SPECTATOR && player.getSpectatorTarget() == null) return;
        if (!(player.getSpectatorTarget() instanceof Player)) return;

        levelSpectator(player);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (game.getGameState() != GameState.RUNNING) return;
        if (!game.getSpectatorManger().isSpectator(player)) return;
        if (!(event.getRightClicked() instanceof Player)) return;

        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(event.getRightClicked());
        spectatorPlayers.put(player.getName(), player.getName());
    }

    public void levelSpectator(Player player) {
        TitleUtil.sendTitle(player, 10, 30, 15, "", "§e退出旁观模式");
        player.setGameMode(GameMode.ADVENTURE);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        player.setAllowFlight(true);
        Util.setFlying(player);
        spectatorPlayers.remove(player.getName());
    }*/
}
