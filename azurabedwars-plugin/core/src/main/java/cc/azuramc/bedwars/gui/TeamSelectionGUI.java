package cc.azuramc.bedwars.gui;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.compat.util.ItemBuilder;
import cc.azuramc.bedwars.compat.util.WoolUtil;
import cc.azuramc.bedwars.game.GameManager;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.game.GameTeam;
import cc.azuramc.bedwars.game.TeamColor;
import cc.azuramc.bedwars.gui.base.CustomGUI;
import cc.azuramc.bedwars.gui.base.action.GUIAction;
import cc.azuramc.bedwars.util.MessageUtil;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ant1aura@qq.com
 */
public class TeamSelectionGUI extends CustomGUI {

    private final GameManager gameManager = AzuraBedWars.getInstance().getGameManager();

    private List<TeamColor> teamColors;
    private List<Integer> teamSlots;

    private static final int BORDER_GLASS_COLOR = 7;
    private static final int HIGHLIGHT_GLASS_COLOR = 5;

    public TeamSelectionGUI(GamePlayer gamePlayer) {
        super(gamePlayer, "§8队伍选择", 27);

        initializeTeamData();
        setupBorders();
        setupTeamItems();
    }

    private void setupBorders() {
        // 设置顶部边框
        for (int i = 0; i < 9; i++) {
            setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
            }, false));
        }

        // 设置左右边框
        setItem(9, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));
        setItem(17, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));

        // 设置底部边框
        for (int i = 18; i < 27; i++) {
            setItem(i, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
            }, false));
        }

        // 设置选择区域装饰
        setItem(11, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));
        setItem(13, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));
        setItem(15, XMaterial.matchXMaterial("STAINED_GLASS_PANE:" + BORDER_GLASS_COLOR).orElse(XMaterial.GLASS_PANE).parseItem(), new GUIAction(0, () -> {
        }, false));
    }

    /**
     * 初始化队伍数据
     */
    private void initializeTeamData() {
        List<GameTeam> gameTeams = gameManager.getGameTeams();
        teamColors = new ArrayList<>();
        teamSlots = new ArrayList<>();

        // 根据实际队伍数量动态分配槽位
        int teamCount = gameTeams.size();
        if (teamCount <= 4) {
            // 4队或更少：使用中间4个槽位
            List<Integer> defaultSlots = Arrays.asList(10, 12, 14, 16);
            for (int i = 0; i < teamCount; i++) {
                teamColors.add(gameTeams.get(i).getTeamColor());
                teamSlots.add(defaultSlots.get(i));
            }
        } else if (teamCount <= 8) {
            // 5-8队：使用两行
            List<Integer> extendedSlots = Arrays.asList(9, 10, 11, 12, 14, 15, 16, 17);
            for (int i = 0; i < teamCount; i++) {
                teamColors.add(gameTeams.get(i).getTeamColor());
                teamSlots.add(extendedSlots.get(i));
            }
        } else {
            // 更多队伍：使用三行布局
            List<Integer> fullSlots = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 25);
            for (int i = 0; i < Math.min(teamCount, fullSlots.size()); i++) {
                teamColors.add(gameTeams.get(i).getTeamColor());
                teamSlots.add(fullSlots.get(i));
            }
        }
    }

    /**
     * 设置所有队伍选择项
     */
    private void setupTeamItems() {
        for (int i = 0; i < teamColors.size(); i++) {
            TeamColor teamColor = teamColors.get(i);
            int slot = teamSlots.get(i);
            setupTeamItem(teamColor, slot);
        }
    }

    /**
     * 设置单个队伍选择项
     */
    private void setupTeamItem(TeamColor teamColor, int slot) {
        GameTeam team = gameManager.getTeam(teamColor);

        if (team == null) {
            return;
        }

        boolean isSelected = getGamePlayer().getGameTeam() == team;
        boolean isFull = team.isFull();
        int currentPlayers = team.getGamePlayers().size();
        int maxPlayers = team.getMaxPlayers();

        // 获取队伍信息
        ItemStack coloredWoolItem = WoolUtil.getColoredWool(teamColor);
        ChatColor chatColor = teamColor.getChatColor();
        String teamName = teamColor.getName();

        // 构建物品描述
        String statusLine;
        if (isSelected) {
            statusLine = "§a§l✓ 已选择";
        } else if (isFull) {
            statusLine = "§c§l✗ 队伍已满";
        } else {
            statusLine = "§7点击选择此队伍";
        }

        setItem(slot, new ItemBuilder(coloredWoolItem)
                        .setDisplayName(chatColor + teamName)
                        .setLores(
                                "§7队伍人数: §f" + currentPlayers + "§7/§f" + maxPlayers,
                                "§7",
                                statusLine
                        )
                        .getItem(),
                new GUIAction(0, () -> handleTeamSelection(getGamePlayer(), team, teamColor), true));
    }

    /**
     * 处理队伍选择逻辑
     */
    private void handleTeamSelection(GamePlayer gamePlayer, GameTeam targetTeam, TeamColor teamColor) {
        Player player = gamePlayer.getPlayer();
        GameTeam currentTeam = gamePlayer.getGameTeam();

        // 检查是否已经选择了该队伍
        if (currentTeam == targetTeam) {
            player.sendMessage(MessageUtil.color("&c你已经选择了该队伍！"));
            return;
        }

        // 检查队伍是否已满
        if (targetTeam.isFull()) {
            player.sendMessage(MessageUtil.color("&c该队伍已满，无法加入！"));
            return;
        }

        // 检查是否只能选择人数最少的队伍（平衡机制）
        GameTeam lowestTeam = gameManager.getLowestTeam();
        if (lowestTeam != targetTeam && !targetTeam.getGamePlayers().isEmpty()) {
            // 如果目标队伍不是人数最少的队伍，且不为空，则检查是否允许选择
            int targetSize = targetTeam.getGamePlayers().size();
            int lowestSize = lowestTeam.getGamePlayers().size();

            // 允许选择人数相等或只多1人的队伍，保持平衡
            if (targetSize > lowestSize + 1) {
                player.sendMessage(MessageUtil.color("&c为了保持游戏平衡，请选择人数较少的队伍！"));
                return;
            }
        }

        // 尝试加入新队伍
        if (targetTeam.addPlayer(gamePlayer)) {
            String teamDisplayName = teamColor.getName();
            ChatColor teamChatColor = teamColor.getChatColor();

            player.sendMessage(MessageUtil.color("&a你选择了 " + teamChatColor + teamDisplayName));
            player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 1.0F, 1.0F);

            // 更新玩家手中的队伍颜色物品
            player.getInventory().setItem(2, WoolUtil.getColoredWool(teamColor));
            // TODO: 刷新GUI显示
        } else {
            player.sendMessage(MessageUtil.color("&c加入队伍失败，请稍后重试！"));
        }
    }
}
