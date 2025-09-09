package cc.azuramc.bedwars.database.repository.mongodb;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.entity.PlayerDataTableKey;
import cc.azuramc.bedwars.database.provider.DatabaseProviderFactory;
import cc.azuramc.bedwars.database.provider.mongodb.MongoDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.game.GameModeType;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @Author Irina
 * @Data 2025/9/9 16:06
 */

public class MongoPlayerDataRepository implements IPlayerDataRepository {

    @Getter
    private final MongoDatabaseProvider mongoDatabaseProvider;
    private final MongoCollection<Document> collection;

    public MongoPlayerDataRepository() {
        mongoDatabaseProvider = (MongoDatabaseProvider) DatabaseProviderFactory.getProvider(AzuraBedWars.getInstance());
        MongoDatabase mongoDatabase = mongoDatabaseProvider.getMongoDatabase();
        this.collection = getCollection(mongoDatabase);
    }

    public MongoCollection<Document> getCollection(MongoDatabase mongoDatabase) {
        boolean collectionExists = mongoDatabase.listCollectionNames()
                .into(new ArrayList<String>())
                .contains(PlayerDataTableKey.tableName);

        if (collectionExists) return mongoDatabase.getCollection(PlayerDataTableKey.tableName);
        mongoDatabase.createCollection(PlayerDataTableKey.tableName);
        return mongoDatabase.getCollection(PlayerDataTableKey.tableName);
    }

    @Override
    public void createTable() {
        try {
            IndexOptions options = new IndexOptions().unique(true);
            collection.createIndex(Indexes.ascending(PlayerDataTableKey.uuid), options);

            LoggerUtil.info("MongoDB 索引创建完毕");
        } catch (Exception e) {
            LoggerUtil.error("MongoDB 无法创建索引 " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData insertPlayerData(PlayerData playerData) {
        try {
            Document doc = new Document()
                    .append(PlayerDataTableKey.name, playerData.getName())
                    .append(PlayerDataTableKey.uuid, playerData.getUuid().toString())
                    .append(PlayerDataTableKey.mode, playerData.getMode().name())
                    .append(PlayerDataTableKey.level, playerData.getLevel())
                    .append(PlayerDataTableKey.experience, playerData.getExperience())
                    .append(PlayerDataTableKey.kills, playerData.getKills())
                    .append(PlayerDataTableKey.deaths, playerData.getDeaths())
                    .append(PlayerDataTableKey.assists, playerData.getAssists())
                    .append(PlayerDataTableKey.finalKills, playerData.getFinalKills())
                    .append(PlayerDataTableKey.finalDeaths, playerData.getFinalDeaths())
                    .append(PlayerDataTableKey.destroyedBeds, playerData.getDestroyedBeds())
                    .append(PlayerDataTableKey.wins, playerData.getWins())
                    .append(PlayerDataTableKey.ties, playerData.getTies())
                    .append(PlayerDataTableKey.losses, playerData.getLosses())
                    .append(PlayerDataTableKey.games, playerData.getGames())
                    .append(PlayerDataTableKey.shopDataJson, playerData.getShopDataJson())
                    .append(PlayerDataTableKey.createdAt, playerData.getCreatedAt())
                    .append(PlayerDataTableKey.updatedAt, playerData.getUpdatedAt());

            collection.insertOne(doc);
            return playerData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert player data", e);
        }
    }

    @Override
    public void updatePlayerData(PlayerData playerData) {
        try {
            Document doc = new Document()
                    .append(PlayerDataTableKey.uuid, playerData.getUuid().toString())
                    .append(PlayerDataTableKey.name, playerData.getName())
                    .append(PlayerDataTableKey.mode, playerData.getMode().name())
                    .append(PlayerDataTableKey.level, playerData.getLevel())
                    .append(PlayerDataTableKey.experience, playerData.getExperience())
                    .append(PlayerDataTableKey.kills, playerData.getKills())
                    .append(PlayerDataTableKey.deaths, playerData.getDeaths())
                    .append(PlayerDataTableKey.assists, playerData.getAssists())
                    .append(PlayerDataTableKey.finalKills, playerData.getFinalKills())
                    .append(PlayerDataTableKey.finalDeaths, playerData.getFinalDeaths())
                    .append(PlayerDataTableKey.destroyedBeds, playerData.getDestroyedBeds())
                    .append(PlayerDataTableKey.wins, playerData.getWins())
                    .append(PlayerDataTableKey.ties, playerData.getTies())
                    .append(PlayerDataTableKey.losses, playerData.getLosses())
                    .append(PlayerDataTableKey.games, playerData.getGames())
                    .append(PlayerDataTableKey.shopDataJson, playerData.getShopDataJson())
                    .append(PlayerDataTableKey.updatedAt, playerData.getUpdatedAt())
                    .append(PlayerDataTableKey.createdAt, playerData.getCreatedAt());

            Bson filter = Filters.eq(PlayerDataTableKey.uuid, playerData.getUuid());
            UpdateOptions options = new UpdateOptions().upsert(true);

            collection.updateOne(filter, doc, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update player data", e);
        }
    }

    @Override
    public PlayerData selectPlayerDataByUuid(UUID uuid, GamePlayer gamePlayer) {
        try {
            Document result = collection.find(Filters.eq(PlayerDataTableKey.uuid, uuid)).first();
            if (result == null) return new PlayerData(gamePlayer);

            PlayerData pd = new PlayerData(gamePlayer);

            pd.setId(result.getInteger(PlayerDataTableKey.id));
            pd.setName(result.getString(PlayerDataTableKey.name));
            pd.setUuid(UUID.fromString(result.getString(PlayerDataTableKey.uuid)));
            pd.setMode((GameModeType) result.get(PlayerDataTableKey.mode));
            pd.setLevel(result.getInteger(PlayerDataTableKey.level));
            pd.setExperience(result.getInteger(PlayerDataTableKey.experience));
            pd.setKills(result.getInteger(PlayerDataTableKey.kills));
            pd.setDeaths(result.getInteger(PlayerDataTableKey.deaths));
            pd.setAssists(result.getInteger(PlayerDataTableKey.assists));
            pd.setFinalKills(result.getInteger(PlayerDataTableKey.finalKills));
            pd.setFinalDeaths(result.getInteger(PlayerDataTableKey.finalDeaths));
            pd.setDestroyedBeds(result.getInteger(PlayerDataTableKey.destroyedBeds));
            pd.setWins(result.getInteger(PlayerDataTableKey.wins));
            pd.setTies(result.getInteger(PlayerDataTableKey.ties));
            pd.setLosses(result.getInteger(PlayerDataTableKey.losses));
            pd.setGames(result.getInteger(PlayerDataTableKey.games));
            pd.setShopDataJson(result.getString(PlayerDataTableKey.shopDataJson));
            pd.setUpdatedAt((Timestamp) result.get(PlayerDataTableKey.updatedAt));
            pd.setCreatedAt((Timestamp) result.get(PlayerDataTableKey.createdAt));

            return pd;
        } catch (Exception e) {
            throw new RuntimeException("Failed to select player data", e);
        }
    }
}
