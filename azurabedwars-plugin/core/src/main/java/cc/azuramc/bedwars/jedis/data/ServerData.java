package cc.azuramc.bedwars.jedis.data;

import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
public class ServerData {
    private String gameType;
    private String name;
    private String ip;
    private ServerType serverType;
    private int maxPlayers = -1;

    public int getPlayers() {
        return -1;
    }
}
