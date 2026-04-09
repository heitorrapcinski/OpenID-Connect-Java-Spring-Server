package org.mitre.migration.job;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.mitre.migration.config.MigrationProperties;
import org.mitre.migration.report.MigrationReport;
import org.mitre.migration.skip.MigrationSkipListener;
import org.mitre.migration.skip.MigrationSkipPolicy;
import org.mitre.migration.skip.MigrationSkippableException;
import org.mitre.migration.writer.MongoUpsertItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class ResourceSetMigrationJob {

    private static final String COLLECTION = "resource_sets";
    private static final String DB = "uma_db";

    @Bean
    public Job resourceSetMigrationJob(JobRepository jobRepository, Step resourceSetStep) {
        return new JobBuilder("resourceSetMigrationJob", jobRepository)
            .start(resourceSetStep)
            .build();
    }

    @Bean
    public Step resourceSetStep(JobRepository jobRepository,
                                 PlatformTransactionManager txManager,
                                 DataSource dataSource,
                                 @Qualifier("umaMongoClient") MongoClient umaMongoClient,
                                 MigrationReport report,
                                 MigrationProperties props) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("resourceSetReader")
            .dataSource(dataSource)
            .sql("SELECT id, name, uri, icon_uri, rs_type, owner, client_id FROM resource_set")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                if (name == null) {
                    throw new MigrationSkippableException(String.valueOf(id), "resource_set name is null");
                }

                Document doc = new Document();
                doc.put("_id", String.valueOf(id));
                doc.put("name", name);
                doc.put("uri", rs.getString("uri"));
                doc.put("iconUri", rs.getString("icon_uri"));
                doc.put("type", rs.getString("rs_type"));
                doc.put("owner", rs.getString("owner"));
                doc.put("clientId", rs.getString("client_id"));

                List<String> scopes = jdbc.queryForList(
                    "SELECT scope FROM resource_set_scope WHERE owner_id = ?", String.class, id);
                doc.put("scopes", scopes);

                // Embed policies with their scopes and claims
                List<Map<String, Object>> policies = jdbc.queryForList(
                    "SELECT id, name FROM policy WHERE resource_set_id = ?", id);
                List<Document> policyDocs = new ArrayList<>();
                for (Map<String, Object> policy : policies) {
                    long policyId = ((Number) policy.get("id")).longValue();
                    Document policyDoc = new Document();
                    policyDoc.put("id", String.valueOf(policyId));
                    policyDoc.put("name", policy.get("name"));

                    List<String> policyScopes = jdbc.queryForList(
                        "SELECT scope FROM policy_scope WHERE owner_id = ?", String.class, policyId);
                    policyDoc.put("scopes", policyScopes);

                    // Embed claims for this policy
                    List<Map<String, Object>> claimRows = jdbc.queryForList(
                        "SELECT c.id, c.name, c.friendly_name, c.claim_type, c.claim_value " +
                        "FROM claim c JOIN policy_claim pc ON c.id = pc.claim_id " +
                        "WHERE pc.policy_id = ?", policyId);
                    List<Document> claimDocs = new ArrayList<>();
                    for (Map<String, Object> claimRow : claimRows) {
                        long claimId = ((Number) claimRow.get("id")).longValue();
                        Document claimDoc = new Document();
                        claimDoc.put("id", String.valueOf(claimId));
                        claimDoc.put("name", claimRow.get("name"));
                        claimDoc.put("friendlyName", claimRow.get("friendly_name"));
                        claimDoc.put("claimType", claimRow.get("claim_type"));
                        claimDoc.put("claimValue", claimRow.get("claim_value"));

                        List<String> tokenFormats = jdbc.queryForList(
                            "SELECT claim_token_format FROM claim_token_format WHERE owner_id = ?",
                            String.class, claimId);
                        claimDoc.put("claimTokenFormats", tokenFormats);

                        List<String> issuers = jdbc.queryForList(
                            "SELECT issuer FROM claim_issuer WHERE owner_id = ?", String.class, claimId);
                        claimDoc.put("issuers", issuers);

                        claimDocs.add(claimDoc);
                    }
                    policyDoc.put("claimsRequired", claimDocs);
                    policyDocs.add(policyDoc);
                }
                doc.put("policies", policyDocs);

                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(umaMongoClient, DB, COLLECTION, report);

        return new StepBuilder("resourceSetStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
