package cc.azuramc.bedwars.database.repository.mongodb;

import cc.azuramc.bedwars.database.entity.DatabaseVersion;
import cc.azuramc.bedwars.database.mapper.EntityMapper;
import cc.azuramc.bedwars.database.provider.mongodb.MongoDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.ArrayList;

/**
 * @author Irina
 */
public class MongoDatabaseVersionRepository implements IDatabaseVersionRepository {

    private static final String TABLE_NAME = EntityMapper.getTableName(DatabaseVersion.class);
    private static final String COL_VERSION = EntityMapper.getQueryColumn(DatabaseVersion.class, DatabaseVersion.Query.BY_VERSION.name());

    private final MongoCollection<Document> collection;

    public MongoDatabaseVersionRepository(MongoDatabaseProvider mongoDatabaseProvider) {
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
        IndexOptions options = new IndexOptions().unique(true);
        collection.createIndex(Indexes.ascending(COL_VERSION), options);
    }

    @Override
    public int getCurrentVersion() {
        Document result = collection.find(Filters.exists(COL_VERSION)).first();
        return result != null ? result.getInteger(COL_VERSION) : -1;
    }

    @Override
    public DatabaseVersion selectDatabaseVersion() {
        try {
            Document result = collection.find(Filters.exists(COL_VERSION)).first();

            DatabaseVersion dv = new DatabaseVersion();
            dv.setVersion(result != null ? result.getInteger(COL_VERSION) : -1);
            return dv;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertVersion(int version) {
        try {
            DatabaseVersion dv = new DatabaseVersion();
            dv.setVersion(version);
            Document doc = EntityMapper.toDocument(dv);
            collection.insertOne(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert version", e);
        }
    }

    @Override
    public void insertDatabaseVersion(DatabaseVersion databaseVersion) {
        try {
            Document doc = EntityMapper.toDocument(databaseVersion);
            collection.insertOne(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert database version", e);
        }
    }

    @Override
    public void updateVersion(int version) {
        try {
            UpdateOptions options = new UpdateOptions().upsert(true);
            Document updateDoc = new Document("$set", new Document().append(COL_VERSION, version));
            collection.updateOne(Filters.exists(COL_VERSION), updateDoc, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update version", e);
        }
    }

    @Override
    public void updateDatabaseVersion(DatabaseVersion databaseVersion) {
        try {
            UpdateOptions options = new UpdateOptions().upsert(true);
            Document updateDoc = EntityMapper.toUpdateDocument(databaseVersion);
            collection.updateOne(Filters.exists(COL_VERSION), updateDoc, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update database version", e);
        }
    }

    @Override
    public boolean hasVersionRecord() {
        Document result = collection.find(Filters.exists(COL_VERSION)).first();
        return result != null && result.getInteger(COL_VERSION) != -1;
    }
}
