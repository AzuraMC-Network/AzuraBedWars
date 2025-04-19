package cc.azuramc.bedwars.jedis.util;

import cc.azuramc.bedwars.jedis.data.ServerData;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * JSON工具类
 * 用于处理服务器数据的JSON序列化和反序列化
 */
public class JsonUtil {
    // JSON序列化器
    public static final Gson GSON = new Gson();

    // JSON字段名常量
    private static final String SERVER_TYPE = "serverType";
    private static final String GAME_TYPE = "gameType";
    private static final String NAME = "name";
    private static final String PLAYERS = "players";
    private static final String MAX_PLAYERS = "maxPlayers";
    private static final String IP = "ip";
    private static final String EXPAND = "expand";

    /**
     * 将服务器数据转换为JSON字符串
     * @param serverData 服务器数据
     * @param expand 扩展数据
     * @return JSON格式的字符串
     */
    public static String getDynamicString(ServerData serverData, HashMap<String, Object> expand) {
        HashMap<String, Object> jsonObject = new LinkedHashMap<>();
        
        // 添加基本数据
        jsonObject.put(SERVER_TYPE, serverData.getServerType().toString());
        jsonObject.put(GAME_TYPE, serverData.getGameType());
        jsonObject.put(IP, serverData.getIp());
        
        // 添加可选数据
        if (serverData.getName() != null) {
            jsonObject.put(NAME, serverData.getName());
            jsonObject.put(PLAYERS, serverData.getPlayers());
            jsonObject.put(MAX_PLAYERS, serverData.getMaxPlayers());
        }
        
        // 添加扩展数据
        jsonObject.put(EXPAND, expand);

        return GSON.toJson(jsonObject);
    }
}
