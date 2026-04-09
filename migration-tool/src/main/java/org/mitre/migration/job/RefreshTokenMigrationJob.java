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
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class RefreshTokenMigrationJob {

    private static final String COLLECTION = "refresh_tokens";
    private static final String DB = "auth_db";

    @Bean
    public Job refreshTokenMigrationJob(JobRepository jobRepository, Step refreshTokenStep) {
        return new JobBuilder("refreshTokenMigrationJob", jobRepository)
            .start(refreshTokenStep)
            .build();
    }

    @Bean
    public Step refreshTokenStep(JobRepository jobRepository,
                                  PlatformTransactionManager txManager,
                                  DataSource dataSource,
                                  @Qualifier("authMongoClient") MongoClient authMongoClient,
                                  MigrationReport report,
                                  MigrationProperties props,
                                  Map<Long, Document> authHolderCache) {
        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("refreshTokenReader")
            .dataSource(dataSource)
            .sql("SELECT id, token_value, expiration, auth_holder_id, client_id FROM refresh_token")
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

                doc.put("clientId", rs.getString("client_id"));

                long authHolderId = rs.getLong("auth_holder_id");
                if (!rs.wasNull()) {
                    Document authHolder = authHolderCache.get(authHolderId);
                    if (authHolder != null) {
                        doc.put("authenticationHolder", authHolder);
                    } else {
                        doc.put("authHolderId", String.valueOf(authHolderId));
                    }
                }

                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(authMongoClient, DB, COLLECTION, report);

        return new StepBuilder("refreshTokenStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
