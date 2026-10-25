package cc.azuramc.bedwars.database.service;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.game.GamePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Caches player data by uuid for the whole game, so players who left can still be updated and saved on shutdown
 *
 * @author an5w1r@163.com
 */
public class PlayerDataService {

    private final IPlayerDataRepository playerDataRepository;
    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();

    public PlayerDataService(IPlayerDataRepository playerDataRepository) {
        this.playerDataRepository = playerDataRepository;
        playerDataRepository.createTable();
    }

    /**
     * Loads the player's data, creating the record on first join
     */
    public PlayerData selectPlayerData(GamePlayer gamePlayer) {
        PlayerData playerData = playerDataCache.get(gamePlayer.getUuid());
        if (playerData != null) {
            return playerData;
        }

        playerData = playerDataRepository.selectPlayerDataByUuid(gamePlayer.getUuid(), gamePlayer);
        String databaseId = playerData.getId();
        if (databaseId == null || databaseId.isEmpty() || databaseId.equals("0")) {
            playerData = playerDataRepository.insertPlayerData(new PlayerData(gamePlayer));
        }
        if (playerData != null) {
            playerDataCache.put(gamePlayer.getUuid(), playerData);
        }
        return playerData;
    }

    @Nullable
    public PlayerData getCachedPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    public void updatePlayerData(GamePlayer gamePlayer) {
        playerDataRepository.updatePlayerData(selectPlayerData(gamePlayer));
    }

    public void shutdown() {
        playerDataCache.values().forEach(playerDataRepository::updatePlayerData);
        playerDataCache.clear();
    }
}
