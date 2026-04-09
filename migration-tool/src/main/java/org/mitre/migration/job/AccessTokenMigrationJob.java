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
import java.util.List;
import java.util.Map;

@Configuration
public class AccessTokenMigrationJob {

    private static final String COLLECTION = "access_tokens";
    private static final String DB = "auth_db";

    @Bean
    public Job accessTokenMigrationJob(JobRepository jobRepository, Step accessTokenStep) {
        return new JobBuilder("accessTokenMigrationJob", jobRepository)
            .start(accessTokenStep)
            .build();
    }

    @Bean
    public Step accessTokenStep(JobRepository jobRepository,
                                 PlatformTransactionManager txManager,
                                 DataSource dataSource,
                                 @Qualifier("authMongoClient") MongoClient authMongoClient,
                                 MigrationReport report,
                                 MigrationProperties props,
                                 Map<Long, Document> authHolderCache) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("accessTokenReader")
            .dataSource(dataSource)
            .sql("SELECT id, token_value, expiration, token_type, refresh_token_id, client_id, " +
                 "auth_holder_id, approved_site_id FROM access_token")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                String tokenValue = rs.getString("token_value");
                if (tokenValue == null) {
                    throw new MigrationSkippableException(String.valueOf(id), "token_value is null");
                }

                Document doc = new Document();
                doc.put("_id", String.valueOf(id));
                doc.put("tokenValue", tokenValue);

                java.sql.Timestamp expiration = rs.getTimestamp("expiration");
                doc.put("expiration", expiration != null ? new java.util.Date(expiration.getTime()) : null);

                doc.put("tokenType", rs.getString("token_type"));
                doc.put("clientId", rs.getString("client_id"));

                long refreshTokenId = rs.getLong("refresh_token_id");
                if (!rs.wasNull()) {
                    doc.put("refreshTokenId", String.valueOf(refreshTokenId));
                }

                long approvedSiteId = rs.getLong("approved_site_id");
                if (!rs.wasNull()) {
                    doc.put("approvedSiteId", String.valueOf(approvedSiteId));
                }

                long authHolderId = rs.getLong("auth_holder_id");
                if (!rs.wasNull()) {
                    Document authHolder = authHolderCache.get(authHolderId);
                    if (authHolder != null) {
                        doc.put("authenticationHolder", authHolder);
                    } else {
                        doc.put("authHolderId", String.valueOf(authHolderId));
                    }
                }

                List<String> scopes = jdbc.queryForList(
                    "SELECT scope FROM token_scope WHERE owner_id = ?", String.class, id);
                doc.put("scope", scopes);

                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(authMongoClient, DB, COLLECTION, report);

        return new StepBuilder("accessTokenStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
