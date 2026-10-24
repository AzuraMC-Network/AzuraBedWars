package cc.azuramc.bedwars.database.repository.mongodb;

import cc.azuramc.bedwars.database.entity.PlayerData;
import cc.azuramc.bedwars.database.mapper.EntityMapper;
import cc.azuramc.bedwars.database.provider.mongodb.MongoDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IPlayerDataRepository;
import cc.azuramc.bedwars.game.GamePlayer;
import cc.azuramc.bedwars.util.LoggerUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Irina
 */
public class MongoPlayerDataRepository implements IPlayerDataRepository {

    private static final String TABLE_NAME = EntityMapper.getTableName(PlayerData.class);
    private static final String COL_UUID = EntityMapper.getQueryColumn(PlayerData.class, PlayerData.Query.BY_UUID.name());

    private final MongoCollection<Document> collection;

    public MongoPlayerDataRepository(MongoDatabaseProvider mongoDatabaseProvider) {
        MongoDatabase mongoDatabase = mongoDatabaseProvider.getMongoDatabase();
        this.collection = getCollection(mongoDatabase);
    }

    public MongoCollection<Document> getCollection(MongoDatabase mongoDatabase) {
        boolean collectionExists = mongoDatabase.listCollectionNames()
                .into(new ArrayList<>())
                .contains(TABLE_NAME);

        if (collectionExists) {
            return mongoDatabase.getCollection(TABLE_NAME);
        }
        mongoDatabase.createCollection(TABLE_NAME);
        return mongoDatabase.getCollection(TABLE_NAME);
    }

    @Override
    public void createTable() {
        try {
            IndexOptions options = new IndexOptions().unique(true);
            collection.createIndex(Indexes.ascending(COL_UUID), options);

            LoggerUtil.info("MongoDB 索引创建完毕");
        } catch (Exception e) {
            LoggerUtil.error("MongoDB 无法创建索引 " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData insertPlayerData(PlayerData playerData) {
        try {
            Document doc = EntityMapper.toDocument(playerData);
            collection.insertOne(doc);

            ObjectId objectId = doc.getObjectId("_id");
            playerData.setId(objectId.toString());

            return playerData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert player data", e);
        }
    }

    @Override
    public void updatePlayerData(PlayerData playerData) {
        try {
            Document updateDoc = EntityMapper.toUpdateDocument(playerData);
            UpdateOptions options = new UpdateOptions().upsert(true);
            collection.updateOne(Filters.eq(COL_UUID, playerData.getUuid().toString()), updateDoc, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update player data", e);
        }
    }

    @Override
    public PlayerData selectPlayerDataByUuid(UUID uuid, GamePlayer gamePlayer) {
        try {
            Document result = collection.find(Filters.eq(COL_UUID, uuid.toString())).first();
            if (result == null) {
                return new PlayerData(gamePlayer);
            }

            return EntityMapper.fromDocument(result, PlayerData.class, gamePlayer);
        } catch (Exception e) {
            throw new RuntimeException("Failed to select player data", e);
        }
    }
}
