package cc.azuramc.bedwars.util;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;

/**
 * @author An5w1r@163.com
 */
public class LuckPermsUtil {

    public static boolean isLoaded = AzuraBedWars.getInstance().getLuckPermsApi() != null;
    private static final LuckPerms luckPermsApi = AzuraBedWars.getInstance().getLuckPermsApi();

    /**
     * 获取玩家的 prefix（同步，玩家必须在线）
     *
     * @param gamePlayer Bukkit 玩家对象
     * @return 玩家前缀，若未设置返回空字符串
     */
    public static String getPrefix(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        User user = luckPermsApi.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return ""; // 用户未加载，可能是异步调用或尚未登录
        }

        QueryOptions queryOptions = luckPermsApi.getContextManager().getQueryOptions(user).orElse(null);
        if (queryOptions == null) {
            return "";
        }

        String prefix = user.getCachedData().getMetaData(queryOptions).getPrefix();
        return prefix != null ? MessageUtil.color(prefix) : "";
    }
}
