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
public class PermissionTicketMigrationJob {

    private static final String COLLECTION = "permission_tickets";
    private static final String DB = "uma_db";

    @Bean
    public Job permissionTicketMigrationJob(JobRepository jobRepository, Step permissionTicketStep) {
        return new JobBuilder("permissionTicketMigrationJob", jobRepository)
            .start(permissionTicketStep)
            .build();
    }

    @Bean
    public Step permissionTicketStep(JobRepository jobRepository,
                                      PlatformTransactionManager txManager,
                                      DataSource dataSource,
                                      @Qualifier("umaMongoClient") MongoClient umaMongoClient,
                                      MigrationReport report,
                                      MigrationProperties props) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("permissionTicketReader")
            .dataSource(dataSource)
            .sql("SELECT id, ticket, expiration, permission_id FROM permission_ticket")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                String ticket = rs.getString("ticket");
                if (ticket == null) {
                    throw new MigrationSkippableException(String.valueOf(id), "ticket is null");
                }

                Document doc = new Document();
                doc.put("_id", String.valueOf(id));
                doc.put("ticket", ticket);

                java.sql.Timestamp expiration = rs.getTimestamp("expiration");
                doc.put("expiration", expiration != null ? new java.util.Date(expiration.getTime()) : null);

                // Embed permission with its scopes
                long permissionId = rs.getLong("permission_id");
                if (!rs.wasNull()) {
                    List<Map<String, Object>> permissions = jdbc.queryForList(
                        "SELECT id, resource_set_id FROM permission WHERE id = ?", permissionId);
                    if (!permissions.isEmpty()) {
                        Map<String, Object> perm = permissions.get(0);
                        Document permDoc = new Document();
                        permDoc.put("id", String.valueOf(((Number) perm.get("id")).longValue()));
                        Object rsId = perm.get("resource_set_id");
                        if (rsId != null) {
                            permDoc.put("resourceSetId", String.valueOf(((Number) rsId).longValue()));
                        }

                        List<String> permScopes = jdbc.queryForList(
                            "SELECT scope FROM permission_scope WHERE owner_id = ?", String.class, permissionId);
                        permDoc.put("scopes", permScopes);

                        doc.put("permission", permDoc);
                    }
                }

                // Embed claims supplied with this ticket
                List<Map<String, Object>> claimRows = jdbc.queryForList(
                    "SELECT c.id, c.name, c.friendly_name, c.claim_type, c.claim_value " +
                    "FROM claim c JOIN claim_to_permission_ticket cpt ON c.id = cpt.claim_id " +
                    "WHERE cpt.permission_ticket_id = ?", id);
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
                doc.put("claimsSupplied", claimDocs);

                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(umaMongoClient, DB, COLLECTION, report);

        return new StepBuilder("permissionTicketStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
