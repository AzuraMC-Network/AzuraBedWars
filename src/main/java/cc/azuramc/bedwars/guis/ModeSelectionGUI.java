package cc.azuramc.bedwars.guis;

import cc.azuramc.bedwars.scoreboards.LobbyBoard;
import cc.azuramc.bedwars.compat.material.MaterialUtil;
import cc.azuramc.bedwars.utils.gui.CustomGUI;
import cc.azuramc.bedwars.utils.gui.GUIAction;
import cc.azuramc.bedwars.utils.ItemBuilderUtil;
import cc.azuramc.bedwars.database.PlayerData;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.types.ModeType;
import cc.azuramc.bedwars.compat.sound.SoundUtil;
import org.bukkit.entity.Player;

public class ModeSelectionGUI extends CustomGUI {

    public ModeSelectionGUI(Player player) {
        super(player, "§8资源类型选择", 27);

        PlayerData playerData = GamePlayer.get(player.getUniqueId()).getPlayerData();

        setItem(11, new ItemBuilderUtil().setType(MaterialUtil.BED()).setDisplayName("§a普通模式").setLores(" ", playerData.getModeType() == ModeType.DEFAULT ? "§a§l✔已选择" : "").getItem(), new GUIAction(0, () -> {
            playerData.setModeType(ModeType.DEFAULT);
            SoundUtil.playOrbPickupSound(player);
            LobbyBoard.updateBoard();
            player.sendMessage("§a模式选择成功!");
        }, true));

        setItem(15, new ItemBuilderUtil().setType(MaterialUtil.EXPERIENCE_BOTTLE()).setDisplayName("§a经验模式").setLores(" ", playerData.getModeType() == ModeType.EXPERIENCE ? "§a§l✔已选择" : "").getItem(), new GUIAction(0, () -> {
            playerData.setModeType(ModeType.EXPERIENCE);
            SoundUtil.playOrbPickupSound(player);
            LobbyBoard.updateBoard();
            player.sendMessage("§a模式选择成功!");
        }, true));
    }
}
