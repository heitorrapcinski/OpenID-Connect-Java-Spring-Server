package org.mitre.migration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.AfterContainer;
import net.jqwik.api.lifecycle.BeforeContainer;
import org.bson.Document;
import org.mitre.migration.report.MigrationReport;
import org.mitre.migration.writer.MongoUpsertItemWriter;
import org.springframework.batch.item.Chunk;
import org.testcontainers.containers.MongoDBContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature: oidc-microservices-ddd-hexagonal
 * Property 9: Migração é idempotente
 *
 * For any set of SQL input records, executing the migration once or multiple times
 * must produce the same final MongoDB state: no duplication, no data loss,
 * and document count equals the number of valid input records.
 *
 * Validates: Requirements 12.5
 */
class MigrationIdempotencyProperties {

    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7.0");
    static MongoClient mongoClient;

    @BeforeContainer
    static void startMongo() {
        mongoContainer.start();
        mongoClient = MongoClients.create(mongoContainer.getConnectionString());
    }

    @AfterContainer
    static void stopMongo() {
        if (mongoClient != null) mongoClient.close();
        if (mongoContainer != null) mongoContainer.stop();
    }

    /**
     * Property 9: Running migration twice produces the same document count as running it once.
     * No duplication occurs because MongoUpsertItemWriter uses replaceOne with upsert=true.
     */
    @Property(tries = 50)
    void migrationIsIdempotent(@ForAll("systemScopeDocuments") List<Document> inputDocs) {
        String dbName = "test_idempotency_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String collection = "system_scopes";

        MigrationReport report1 = new MigrationReport();
        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(mongoClient, dbName, collection, report1);

        // Run migration first time
        writer.write(new Chunk<>(inputDocs));

        long countAfterFirstRun = mongoClient.getDatabase(dbName)
            .getCollection(collection).countDocuments();

        // Run migration second time with same data
        MigrationReport report2 = new MigrationReport();
        MongoUpsertItemWriter writer2 = new MongoUpsertItemWriter(mongoClient, dbName, collection, report2);
        writer2.write(new Chunk<>(inputDocs));

        long countAfterSecondRun = mongoClient.getDatabase(dbName)
            .getCollection(collection).countDocuments();

        // Count must be identical — no duplication
        assertThat(countAfterSecondRun)
            .as("Second migration run must not create duplicate documents")
            .isEqualTo(countAfterFirstRun);

        // Count must equal number of distinct input documents
        long distinctIds = inputDocs.stream()
            .map(d -> d.get("_id"))
            .distinct()
            .count();
        assertThat(countAfterFirstRun)
            .as("Document count must equal number of distinct input records")
            .isEqualTo(distinctIds);

        // Verify data integrity: each document in MongoDB matches the last written version
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(dbName).getCollection(collection);
        for (Document input : inputDocs) {
            Document stored = mongoCollection.find(new Document("_id", input.get("_id"))).first();
            assertThat(stored).as("Document with _id=%s must exist in MongoDB", input.get("_id")).isNotNull();
            assertThat(stored.getString("value"))
                .as("Stored value must match input value for _id=%s", input.get("_id"))
                .isEqualTo(input.getString("value"));
        }

        // Cleanup
        mongoClient.getDatabase(dbName).drop();
    }

    /**
     * Property 9b: Running migration with updated data replaces documents (no stale data).
     */
    @Property(tries = 30)
    void migrationReplacesExistingDocuments(@ForAll("systemScopeDocuments") List<Document> initialDocs) {
        Assume.that(!initialDocs.isEmpty());

        String dbName = "test_replace_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String collection = "system_scopes";

        // First run
        MigrationReport report1 = new MigrationReport();
        new MongoUpsertItemWriter(mongoClient, dbName, collection, report1)
            .write(new Chunk<>(initialDocs));

        // Create updated versions of the same documents
        List<Document> updatedDocs = initialDocs.stream()
            .map(d -> {
                Document updated = new Document(d);
                updated.put("description", "updated-" + d.getString("value"));
                return updated;
            })
            .toList();

        // Second run with updated data
        MigrationReport report2 = new MigrationReport();
        new MongoUpsertItemWriter(mongoClient, dbName, collection, report2)
            .write(new Chunk<>(updatedDocs));

        // Verify updated data is stored (not stale)
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(dbName).getCollection(collection);
        for (Document updated : updatedDocs) {
            Document stored = mongoCollection.find(new Document("_id", updated.get("_id"))).first();
            assertThat(stored).isNotNull();
            assertThat(stored.getString("description"))
                .as("Document must contain updated description after second run")
                .startsWith("updated-");
        }

        // Cleanup
        mongoClient.getDatabase(dbName).drop();
    }

    @Provide
    Arbitrary<List<Document>> systemScopeDocuments() {
        Arbitrary<String> scopeValues = Arbitraries.strings()
            .alpha()
            .ofMinLength(2)
            .ofMaxLength(30)
            .map(String::toLowerCase);

        Arbitrary<String> descriptions = Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(0)
            .ofMaxLength(100);

        Arbitrary<Boolean> booleans = Arbitraries.of(true, false);

        Arbitrary<Document> docArbitrary = Combinators.combine(
            scopeValues, descriptions, booleans, booleans
        ).as((value, description, defaultScope, restricted) -> {
            Document doc = new Document();
            doc.put("_id", value);
            doc.put("value", value);
            doc.put("description", description);
            doc.put("icon", "user");
            doc.put("defaultScope", defaultScope);
            doc.put("restricted", restricted);
            return doc;
        });

        // Generate lists of 1-20 documents with unique _id values
        return docArbitrary.list()
            .ofMinSize(1)
            .ofMaxSize(20)
            .map(docs -> {
                // Deduplicate by _id to simulate distinct SQL rows
                List<Document> unique = new ArrayList<>();
                java.util.Set<Object> seen = new java.util.HashSet<>();
                for (Document d : docs) {
                    if (seen.add(d.get("_id"))) {
                        unique.add(d);
                    }
                }
                return unique;
            })
            .filter(docs -> !docs.isEmpty());
    }
}
