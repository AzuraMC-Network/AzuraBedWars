package cc.azuramc.bedwars.config.object;

import cc.azuramc.bedwars.config.annotation.Comment;
import cc.azuramc.bedwars.config.annotation.Range;
import cc.azuramc.bedwars.config.annotation.Raw;
import lombok.Data;

/**
 * @author an5w1r@163.com
 */
@Data
@Comment({"开发工具配置", "只在开发服使用 生产服保持 enabled: false", "改动后执行 /bw devtools restart 生效"})
public class DevtoolsConfig {

    @Comment({"是否开启 MCP 端点 让 AI 客户端读写配置和地图", "也可以用 /bw devtools on 和 /bw devtools off 开关"})
    private boolean enabled = false;

    @Comment({"监听地址", "默认只允许本机连接 远程开发服请用 SSH 端口转发"})
    @Raw
    private String host = "127.0.0.1";

    @Comment("监听端口")
    @Range(min = 1, max = 65535)
    private int port = 25599;

    @Comment({"是否允许监听本机以外的地址", "打开后同一网络里拿到令牌的人都能修改服务器配置"})
    private boolean allowRemote = false;

    @Comment({"有非管理员玩家在线时是否仍允许 AI 写入", "关闭时只有管理员在线才能修改配置和地图"})
    private boolean allowWithPlayers = false;
}
