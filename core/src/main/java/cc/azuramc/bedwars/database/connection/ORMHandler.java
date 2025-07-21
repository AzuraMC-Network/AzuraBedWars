package cc.azuramc.bedwars.database.connection;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.orm.AzuraORM;
import cc.azuramc.orm.AzuraOrmClient;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author an5w1r@163.com
 */
public class ORMHandler {

    @Getter private AzuraOrmClient ormClient;

    public ORMHandler(AzuraBedWars plugin) {
        ormClient = plugin.getOrmClient();
        AzuraBedWars.getInstance().getPlayerDataService().createTable();
    }

    public Connection getConnection() throws SQLException {
        return ormClient.getConnection();
    }

    public void shutdown() {
        AzuraORM.shutdownAll();
    }
}
