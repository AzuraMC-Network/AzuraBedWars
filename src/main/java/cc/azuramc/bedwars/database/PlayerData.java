package cc.azuramc.bedwars.database;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.types.ModeType;
import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class PlayerData {
    private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);

    private GamePlayer gamePlayer;
    private ModeType modeType;
    private int kills;
    private int deaths;
    private int destroyedBeds;
    private int wins;
    private int loses;
    private int games;
    private String[] shopSort;

    public PlayerData(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
            // 确保表存在
            ensureStatsTableExists(connection);
            
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM bw_stats_players Where Name=?");
            preparedStatement.setString(1, gamePlayer.getName());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                this.modeType = ModeType.valueOf(resultSet.getString("Mode"));
                this.kills = resultSet.getInt("kills");
                this.deaths = resultSet.getInt("deaths");
                this.destroyedBeds = resultSet.getInt("destroyedBeds");
                this.wins = resultSet.getInt("wins");
                this.loses = resultSet.getInt("loses");
                this.games = resultSet.getInt("games");
            } else {
                this.modeType = ModeType.DEFAULT;
                preparedStatement = connection.prepareStatement("INSERT INTO bw_stats_players (Name,Mode,kills,deaths,destroyedBeds,wins,loses,games) VALUES (?,?,0,0,0,0,0,0)");
                preparedStatement.setString(1, gamePlayer.getName());
                preparedStatement.setString(2, ModeType.DEFAULT.toString());
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void ensureStatsTableExists(Connection connection) throws SQLException {
        String createStatsTable = "CREATE TABLE IF NOT EXISTS bw_stats_players (" +
                "Name VARCHAR(36) PRIMARY KEY, " +
                "Mode VARCHAR(20) NOT NULL, " +
                "kills INT DEFAULT 0, " +
                "deaths INT DEFAULT 0, " +
                "destroyedBeds INT DEFAULT 0, " +
                "wins INT DEFAULT 0, " +
                "loses INT DEFAULT 0, " +
                "games INT DEFAULT 0" +
                ")";
        try (PreparedStatement statement = connection.prepareStatement(createStatsTable)) {
            statement.executeUpdate();
        }
    }
    
    private void ensureShopTableExists(Connection connection) throws SQLException {
        String createShopTable = "CREATE TABLE IF NOT EXISTS bw_shop_players (" +
                "Name VARCHAR(36) PRIMARY KEY, " +
                "data TEXT NOT NULL" +
                ")";
        try (PreparedStatement statement = connection.prepareStatement(createShopTable)) {
            statement.executeUpdate();
        }
    }
    
    private void ensureSpectatorSettingsTableExists(Connection connection) throws SQLException {
        String createSpectatorTable = "CREATE TABLE IF NOT EXISTS bw_spectator_settings (" +
                "Name VARCHAR(36) PRIMARY KEY, " +
                "firstPerson BOOLEAN DEFAULT TRUE, " +
                "hideSpectators BOOLEAN DEFAULT TRUE, " +
                "nightVision BOOLEAN DEFAULT FALSE, " +
                "speedLevel INT DEFAULT 1" +
                ")";
        try (PreparedStatement statement = connection.prepareStatement(createSpectatorTable)) {
            statement.executeUpdate();
        }
    }

    public void asyncLoadShop() {
        fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                // 确保表存在
                ensureShopTableExists(connection);
                
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM bw_shop_players Where Name=?");
                preparedStatement.setString(1, gamePlayer.getName());
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    shopSort = resultSet.getString("data").split(", ");
                } else {
                    shopSort = new String[]{"BlockShop#1", "SwordShop#1", "ArmorShop#1", "FoodShop#1", "BowShop#2", "PotionShop#1", "UtilityShop#2", "BlockShop#8", "SwordShop#2", "ArmorShop#2", "UtilityShop#1", "BowShop#1", "PotionShop#2", "UtilityShop#4", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR"};

                    StringBuilder string = null;
                    for (String s : shopSort) {
                        if (string == null) {
                            string = new StringBuilder(s + ", ");
                            continue;
                        }

                        string.append(s).append(", ");
                    }
                    preparedStatement = connection.prepareStatement("INSERT INTO bw_shop_players (Name,data) VALUES (?,?)");
                    preparedStatement.setString(1, gamePlayer.getName());
                    preparedStatement.setString(2, string.toString().substring(0, string.length() - 2));
                }

                resultSet.close();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveShops() {
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                // 确保表存在
                ensureShopTableExists(connection);
                
                StringBuilder string = null;
                for (String s : shopSort) {
                    if (string == null) {
                        string = new StringBuilder(s + ", ");
                        continue;
                    }

                    string.append(s).append(", ");
                }

                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_shop_players SET data=? Where Name=?");
                preparedStatement.setString(1, string.toString().substring(0, string.length() - 2));
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void setModeType(ModeType modeType) {
        if (this.modeType == modeType) {
            return;
        }

        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                // 确保表存在
                ensureStatsTableExists(connection);
                
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET Mode=? Where Name=?");
                preparedStatement.setString(1, modeType.toString());
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        this.modeType = modeType;
    }

    public void addKills() {
        kills += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                // 确保表存在
                ensureStatsTableExists(connection);
                
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET kills=? Where Name=?");
                preparedStatement.setInt(1, kills);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addFinalKills() {
        //finalKills += 1;
    }

    public void addDeaths() {
        deaths += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                // 确保表存在
                ensureStatsTableExists(connection);
                
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET deaths=? Where Name=?");
                preparedStatement.setInt(1, deaths);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addDestroyedBeds() {
        destroyedBeds += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                // 确保表存在
                ensureStatsTableExists(connection);
                
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET destroyedBeds=? Where Name=?");
                preparedStatement.setInt(1, destroyedBeds);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addWins() {
        wins += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                // 确保表存在
                ensureStatsTableExists(connection);
                
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET wins=? Where Name=?");
                preparedStatement.setInt(1, wins);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void addLoses() {
        loses += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                // 确保表存在
                ensureStatsTableExists(connection);
                
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET loses=? Where Name=?");
                preparedStatement.setInt(1, loses);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }


    public void addGames() {
        games += 1;
        PlayerData.fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                // 确保表存在
                ensureStatsTableExists(connection);
                
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_stats_players SET games=? Where Name=?");
                preparedStatement.setInt(1, games);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}