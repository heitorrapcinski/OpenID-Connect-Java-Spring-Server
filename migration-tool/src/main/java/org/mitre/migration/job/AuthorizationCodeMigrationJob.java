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
public class AuthorizationCodeMigrationJob {

    private static final String COLLECTION = "authorization_codes";
    private static final String DB = "auth_db";

    @Bean
    public Job authorizationCodeMigrationJob(JobRepository jobRepository, Step authorizationCodeStep) {
        return new JobBuilder("authorizationCodeMigrationJob", jobRepository)
            .start(authorizationCodeStep)
            .build();
    }

    @Bean
    public Step authorizationCodeStep(JobRepository jobRepository,
                                       PlatformTransactionManager txManager,
                                       DataSource dataSource,
                                       @Qualifier("authMongoClient") MongoClient authMongoClient,
                                       MigrationReport report,
                                       MigrationProperties props,
                                       Map<Long, Document> authHolderCache) {
        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("authorizationCodeReader")
            .dataSource(dataSource)
            .sql("SELECT id, code, auth_holder_id, expiration FROM authorization_code")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                String code = rs.getString("code");
                if (code == null) {
                    throw new MigrationSkippableException(String.valueOf(id), "code is null");
                }

                Document doc = new Document();
                doc.put("_id", String.valueOf(id));
                doc.put("code", code);

                java.sql.Timestamp expiration = rs.getTimestamp("expiration");
                doc.put("expiration", expiration != null ? new java.util.Date(expiration.getTime()) : null);

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

        return new StepBuilder("authorizationCodeStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
