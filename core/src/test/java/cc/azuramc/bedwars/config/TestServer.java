package cc.azuramc.bedwars.config;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Proxy;
import java.util.logging.Logger;

/**
 * Just enough of a server for code that logs through Bukkit
 */
final class TestServer {

    private TestServer() {
    }

    static synchronized void install() {
        if (Bukkit.getServer() != null) {
            return;
        }
        Logger logger = Logger.getLogger("AzuraBedWarsTest");
        Server server = (Server) Proxy.newProxyInstance(TestServer.class.getClassLoader(), new Class<?>[]{Server.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getLogger":
                            return logger;
                        case "getName":
                        case "getVersion":
                        case "getBukkitVersion":
                            return "test";
                        default:
                            return method.getReturnType() == boolean.class ? false : method.getReturnType().isPrimitive() ? 0 : null;
                    }
                });
        Bukkit.setServer(server);
    }
}
