package cc.azuramc.bedwars.database.repository.mongodb;

import cc.azuramc.bedwars.AzuraBedWars;
import cc.azuramc.bedwars.database.entity.DatabaseVersion;
import cc.azuramc.bedwars.database.entity.DatabaseVersionTableKey;
import cc.azuramc.bedwars.database.provider.DatabaseProviderFactory;
import cc.azuramc.bedwars.database.provider.mongodb.MongoDatabaseProvider;
import cc.azuramc.bedwars.database.repository.IDatabaseVersionRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import lombok.Getter;
import org.bson.Document;

import java.sql.SQLException;

/**
 * @Author Irina
 * @Date 2025/9/9 21:56
 */

public class MongoDatabaseVersionRepository implements IDatabaseVersionRepository {
    @Getter
    private final MongoDatabaseProvider mongoDatabaseProvider;
    private final MongoCollection<Document> collection;

    public MongoDatabaseVersionRepository() {
        mongoDatabaseProvider = (MongoDatabaseProvider) DatabaseProviderFactory.getProvider(AzuraBedWars.getInstance());
        MongoDatabase mongoDatabase = mongoDatabaseProvider.getMongoDatabase();
        collection = mongoDatabase.getCollection(DatabaseVersionTableKey.tableName);
    }

    @Override
    public void createTable() {

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
    public void insertVersion(int version) throws SQLException {
        try {
            Document doc = new Document()
                    .append(DatabaseVersionTableKey.version, version);

            collection.insertOne(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert version", e);
        }
    }

    @Override
    public void insertDatabaseVersion(DatabaseVersion databaseVersion) {
        try {
            Document doc = new Document()
                    .append(DatabaseVersionTableKey.version, databaseVersion.getVersion());

            collection.insertOne(doc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert database version", e);
        }
    }

    @Override
    public void updateVersion(int version) throws SQLException {
        try {
            UpdateOptions options = new UpdateOptions().upsert(true);

            Document doc = new Document()
                    .append(DatabaseVersionTableKey.version, version);

            collection.updateOne(Filters.exists(DatabaseVersionTableKey.version), doc, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert version", e);
        }
    }

    @Override
    public void updateDatabaseVersion(DatabaseVersion databaseVersion) {
        try {
            UpdateOptions options = new UpdateOptions().upsert(true);

            Document doc = new Document()
                    .append(DatabaseVersionTableKey.version, databaseVersion.getVersion());

            collection.updateOne(Filters.exists(DatabaseVersionTableKey.version), doc, options);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert database version", e);
        }
    }

    @Override
    public boolean hasVersionRecord() throws SQLException {
        Document result = collection.find(Filters.exists(DatabaseVersionTableKey.version)).first();
        return result != null && result.getInteger(DatabaseVersionTableKey.version) != -1;
    }
}
