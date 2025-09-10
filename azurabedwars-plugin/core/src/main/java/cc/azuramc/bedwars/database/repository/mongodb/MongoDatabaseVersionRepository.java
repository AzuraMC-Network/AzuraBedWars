package cc.azuramc.bedwars.database.repository.mongodb;

import cc.azuramc.bedwars.database.entity.DatabaseVersion;
import cc.azuramc.bedwars.database.entity.DatabaseVersionTableKey;
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
    private final MongoCollection<Document> collection;

    public MongoDatabaseVersionRepository(MongoDatabaseProvider mongoDatabaseProvider) {
        MongoDatabase mongoDatabase = mongoDatabaseProvider.getMongoDatabase();
        this.collection = getCollection(mongoDatabase);
    }

    public MongoCollection<Document> getCollection(MongoDatabase mongoDatabase) {
        boolean collectionExists = mongoDatabase.listCollectionNames()
                .into(new ArrayList<>())
                .contains(DatabaseVersionTableKey.tableName);

        if (collectionExists) {
            return mongoDatabase.getCollection(DatabaseVersionTableKey.tableName);
        }
        mongoDatabase.createCollection(DatabaseVersionTableKey.tableName);
        return mongoDatabase.getCollection(DatabaseVersionTableKey.tableName);
    }

    @Override
    public void createTable() {
        IndexOptions options = new IndexOptions().unique(true);
        collection.createIndex(Indexes.ascending(DatabaseVersionTableKey.version), options);
    }

    @Override
    public int getCurrentVersion() {
        Document result = collection.find(Filters.exists(DatabaseVersionTableKey.version)).first();

        return result != null ? result.getInteger(DatabaseVersionTableKey.version) : -1;
    }

    @Override
    public DatabaseVersion selectDatabaseVersion() {
        try {
            Document result = collection.find(Filters.exists(DatabaseVersionTableKey.version)).first();

            DatabaseVersion dv = new DatabaseVersion();
            dv.setVersion(result != null ? result.getInteger(DatabaseVersionTableKey.version) : -1);
            return dv;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertVersion(int version) {
        try {
            Document doc = new Document().append(DatabaseVersionTableKey.version, version);
            collection.insertOne(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert version", e);
        }
    }

    @Override
    public void insertDatabaseVersion(DatabaseVersion databaseVersion) {
        try {
            Document doc = new Document().append(DatabaseVersionTableKey.version, databaseVersion.getVersion());
            collection.insertOne(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert database version", e);
        }
    }

    @Override
    public void updateVersion(int version) {
        try {
            UpdateOptions options = new UpdateOptions().upsert(true);
            Document doc = new Document().append(DatabaseVersionTableKey.version, version);
            collection.updateOne(Filters.exists(DatabaseVersionTableKey.version), doc, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert version", e);
        }
    }

    @Override
    public void updateDatabaseVersion(DatabaseVersion databaseVersion) {
        try {
            UpdateOptions options = new UpdateOptions().upsert(true);
            Document doc = new Document().append(DatabaseVersionTableKey.version, databaseVersion.getVersion());
            collection.updateOne(Filters.exists(DatabaseVersionTableKey.version), doc, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert database version", e);
        }
    }

    @Override
    public boolean hasVersionRecord() {
        Document result = collection.find(Filters.exists(DatabaseVersionTableKey.version)).first();
        return result != null && result.getInteger(DatabaseVersionTableKey.version) != -1;
    }
}
