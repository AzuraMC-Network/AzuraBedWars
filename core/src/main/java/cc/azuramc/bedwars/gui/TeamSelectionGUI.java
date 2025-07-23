package cc.azuramc.bedwars.gui;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.game.*;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author ant1aura@qq.com
 */
public class TeamSelectionGUI extends CustomGUI {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();
    private final GameTeam redTeam = gameManager.getTeam(TeamColor.RED);
    private final GameTeam yellowTeam = gameManager.getTeam(TeamColor.YELLOW);
    private final GameTeam blueTeam = gameManager.getTeam(TeamColor.BLUE);
    private final GameTeam greenTeam = gameManager.getTeam(TeamColor.GREEN);

    private static final int BORDER_GLASS_COLOR = 7;
    private static final int HIGHLIGHT_GLASS_COLOR = 5;

    public TeamSelectionGUI(GamePlayer gamePlayer) {
        super(gamePlayer, "§8队伍选择", 27);

        setupBorders();

        setupRedTeamItem(gamePlayer);
        setupYellowTeamItem(gamePlayer);
        setupBlueTeamItem(gamePlayer);
        setupGreenTeamItem(gamePlayer);
    }

    private void setupBorders() {
        // 设置顶部边框
        for (int i = 0; i < 9; i++) {
            setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
        }

        // 设置左右边框
        setItem(9, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
        setItem(17, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));

        // 设置底部边框
        for (int i = 18; i < 27; i++) {
            setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
        }

        // 设置选择区域装饰
        setItem(11, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + HIGHLIGHT_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
        setItem(13, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + HIGHLIGHT_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
        setItem(15, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + HIGHLIGHT_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {}, false));
    }

    private void setupRedTeamItem(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        boolean isSelected = gamePlayer.getGameTeam() == redTeam;

        setItem(10, new ItemBuilder()
                        .setType(XMaterial.RED_WOOL.get())
                        .setDisplayName(ChatColor.RED + "红队")
                        .setLores(
                                "§7",
                                "§7",
                                " ",
                                isSelected ? "§a§l✓ 已选择" : "§7点击选择此队伍"
                        )
                        .getItem(),
                new GUIAction(0, () -> {
                    if (isSelected) {
                        player.sendMessage(MessageUtil.color("&c你已经选择了该队伍！"));
                        return;
                    }

                    if (gameManager.getLowestTeam() != redTeam) {
                        player.sendMessage(MessageUtil.color("&c你不能选择该队伍！"));
                        return;
                    }

                    if (redTeam.addPlayer(gamePlayer)) {
                        player.sendMessage(MessageUtil.color("&a你选择了 &c红队"));
                        player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 10F, 1F);
                        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
                    }
                }, true));
    }

    private void setupYellowTeamItem(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        boolean isSelected = gamePlayer.getGameTeam() == yellowTeam;

        setItem(12, new ItemBuilder()
                        .setType(XMaterial.YELLOW_WOOL.get())
                        .setDisplayName(ChatColor.YELLOW + "黄队")
                        .setLores(
                                "§7",
                                "§7",
                                " ",
                                isSelected ? "§a§l✓ 已选择" : "§7点击选择此队伍"
                        )
                        .getItem(),
                new GUIAction(0, () -> {
                    if (isSelected) {
                        player.sendMessage(MessageUtil.color("&c你已经选择了该队伍！"));
                        return;
                    }

                    if (gameManager.getLowestTeam() != yellowTeam) {
                        player.sendMessage(MessageUtil.color("&c你不能选择该队伍！"));
                        return;
                    }

                    if (redTeam.addPlayer(gamePlayer)) {
                        player.sendMessage(MessageUtil.color("&a你选择了 &e黄队"));
                        player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 10F, 1F);
                        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
                    }
                }, true));
    }

    private void setupBlueTeamItem(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        boolean isSelected = gamePlayer.getGameTeam() == blueTeam;

        setItem(14, new ItemBuilder()
                        .setType(XMaterial.BLUE_WOOL.get())
                        .setDisplayName(ChatColor.DARK_BLUE + "蓝队")
                        .setLores(
                                "§7",
                                "§7",
                                " ",
                                isSelected ? "§a§l✓ 已选择" : "§7点击选择此队伍"
                        )
                        .getItem(),
                new GUIAction(0, () -> {
                    if (isSelected) {
                        player.sendMessage(MessageUtil.color("&c你已经选择了该队伍！"));
                        return;
                    }

                    if (gameManager.getLowestTeam() != blueTeam) {
                        player.sendMessage(MessageUtil.color("&c你不能选择该队伍！"));
                        return;
                    }

                    if (redTeam.addPlayer(gamePlayer)) {
                        player.sendMessage(MessageUtil.color("&a你选择了 &b蓝队"));
                        player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 10F, 1F);
                        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
                    }
                }, true));
    }

    private void setupGreenTeamItem(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        boolean isSelected = gamePlayer.getGameTeam() == greenTeam;

        setItem(16, new ItemBuilder()
                        .setType(XMaterial.GREEN_WOOL.get())
                        .setDisplayName(ChatColor.GREEN + "绿队")
                        .setLores(
                                "§7",
                                "§7",
                                " ",
                                isSelected ? "§a§l✓ 已选择" : "§7点击选择此队伍"
                        )
                        .getItem(),
                new GUIAction(0, () -> {
                    if (isSelected) {
                        player.sendMessage(MessageUtil.color("&c你已经选择了该队伍！"));
                        return;
                    }

                    if (gameManager.getLowestTeam() != greenTeam) {
                        player.sendMessage(MessageUtil.color("&c你不能选择该队伍！"));
                        return;
                    }

                    if (redTeam.addPlayer(gamePlayer)) {
                        player.sendMessage(MessageUtil.color("&a你选择了 绿队"));
                        player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 10F, 1F);
                        AzuraBedWars.getInstance().getScoreboardManager().updateAllBoards();
                    }
                }, true));
    }
}
