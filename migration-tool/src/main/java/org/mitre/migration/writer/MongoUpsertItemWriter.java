package org.mitre.migration.writer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.mitre.migration.report.MigrationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import static com.mongodb.client.model.Filters.eq;

public class MongoUpsertItemWriter implements ItemWriter<Document> {

    private static final Logger log = LoggerFactory.getLogger(MongoUpsertItemWriter.class);

    private final MongoClient mongoClient;
    private final String databaseName;
    private final String collectionName;
    private final MigrationReport report;

    public MongoUpsertItemWriter(MongoClient mongoClient, String databaseName,
                                  String collectionName, MigrationReport report) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.report = report;
    }

    @Override
    public void write(Chunk<? extends Document> chunk) {
        MongoCollection<Document> collection = mongoClient
            .getDatabase(databaseName)
            .getCollection(collectionName);
        ReplaceOptions opts = new ReplaceOptions().upsert(true);

        for (Document doc : chunk) {
            Object id = doc.get("_id");
            collection.replaceOne(eq("_id", id), doc, opts);
            report.recordMigrated(collectionName);
        }
    }
}
