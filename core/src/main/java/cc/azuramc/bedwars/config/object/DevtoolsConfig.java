package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.api.config.Comment;
import cc.azuramc.bedwars.api.config.Range;
import cc.azuramc.bedwars.api.config.Raw;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment({"Developer tools", "For development servers only, keep enabled: false on production", "Run /bw devtools restart after editing"})
public class DevtoolsConfig {

    @Comment({"Run the MCP endpoint that lets AI clients read and change configs and maps", "/bw devtools on and /bw devtools off switch it too"})
    private boolean enabled = false;

    @Comment({"Listen address", "Only this machine can connect by default, forward the port over SSH for a remote dev server"})
    @Raw
    private String host = "127.0.0.1";

    @Comment("Listen port")
    @Range(min = 1, max = 65535)
    private int port = 25599;

    @Comment({"Allow listening on addresses other than this machine", "Anyone on the network with the token can then change the server config"})
    private boolean allowRemote = false;

    @Comment({"Allow AI writes while players without admin rights are online", "When off, configs and maps change only while only admins are online"})
    private boolean allowWithPlayers = false;
}
