package cc.azuramc.bedwars.spectator;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.game.GamePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpectatorSettings {
    private static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
    private static final Map<GamePlayer, SpectatorSettings> spectatorSettingsMap = new HashMap<>();

    private final GamePlayer gamePlayer;
    private int speed;
    private boolean autoTp;
    private boolean nightVision;
    private boolean firstPerson;
    private boolean hideOther;
    private boolean fly;

    public SpectatorSettings(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
        try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM bw_spectator_settings Where Name=?");
            preparedStatement.setString(1, gamePlayer.getName());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                this.speed = resultSet.getInt("speed");
                this.autoTp = resultSet.getBoolean("autoTp");
                this.nightVision = resultSet.getBoolean("nightVision");
                this.firstPerson = resultSet.getBoolean("firstPerson");
                this.hideOther = resultSet.getBoolean("hideOther");
                this.fly = resultSet.getBoolean("fly");
            } else {
                this.firstPerson = true;
                preparedStatement = connection.prepareStatement("INSERT INTO bw_spectator_settings (Name,speed,autoTp,nightVision,firstPerson,hideOther,fly) VALUES (?,0,0,0,1,0,0)");
                preparedStatement.setString(1, gamePlayer.getName());
                preparedStatement.executeUpdate();
            }
            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static SpectatorSettings get(GamePlayer player) {
        if (spectatorSettingsMap.containsKey(player)) {
            return spectatorSettingsMap.get(player);
        }

        SpectatorSettings spectatorSettings = new SpectatorSettings(player);
        spectatorSettingsMap.put(player, spectatorSettings);
        return spectatorSettings;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int key) {
        if ((key < 0) || (key > 4)) {
            return;
        }
        speed = key;

        fixedThreadPool.execute(() -> {
            try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_spectator_settings SET speed=? Where Name=?");
                preparedStatement.setInt(1, key);
                preparedStatement.setString(2, gamePlayer.getName());
                preparedStatement.executeUpdate();

                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean getOption(Option option) {
        switch (option) {
            case AUTOTP:
                return autoTp;
            case NIGHTVISION:
                return nightVision;
            case FIRSTPERSON:
                return firstPerson;
            case HIDEOTHER:
                return hideOther;
            case FLY:
                return fly;
        }
        return false;
    }

    public void setOption(Option o, boolean key) {
        if (getOption(o) && key) {
            return;
        }
        switch (o) {
            case AUTOTP:
                autoTp = key;
                fixedThreadPool.execute(() -> {
                    try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_spectator_settings SET autoTp=? Where Name=?");
                        preparedStatement.setBoolean(1, key);
                        preparedStatement.setString(2, gamePlayer.getName());
                        preparedStatement.executeUpdate();

                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case NIGHTVISION:
                nightVision = key;
                fixedThreadPool.execute(() -> {
                    try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_spectator_settings SET nightVision=? Where Name=?");
                        preparedStatement.setBoolean(1, key);
                        preparedStatement.setString(2, gamePlayer.getName());
                        preparedStatement.executeUpdate();

                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case FIRSTPERSON:
                firstPerson = key;
                fixedThreadPool.execute(() -> {
                    try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_spectator_settings SET firstPerson=? Where Name=?");
                        preparedStatement.setBoolean(1, key);
                        preparedStatement.setString(2, gamePlayer.getName());
                        preparedStatement.executeUpdate();

                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case HIDEOTHER:
                hideOther = key;
                fixedThreadPool.execute(() -> {
                    try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_spectator_settings SET hideOther=? Where Name=?");
                        preparedStatement.setBoolean(1, key);
                        preparedStatement.setString(2, gamePlayer.getName());
                        preparedStatement.executeUpdate();

                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                break;
            case FLY:
                fly = key;
                fixedThreadPool.execute(() -> {
                    try (Connection connection = AzuraBedWars.getInstance().getConnectionPoolHandler().getConnection("bwstats")) {
                        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE bw_spectator_settings SET fly=? Where Name=?");
                        preparedStatement.setBoolean(1, key);
                        preparedStatement.setString(2, gamePlayer.getName());
                        preparedStatement.executeUpdate();

                        preparedStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                break;
        }
    }

    public GamePlayer getPlayer() {
        return gamePlayer;
    }

    public enum Option {
        AUTOTP, NIGHTVISION, FIRSTPERSON, HIDEOTHER, FLY
    }
}
