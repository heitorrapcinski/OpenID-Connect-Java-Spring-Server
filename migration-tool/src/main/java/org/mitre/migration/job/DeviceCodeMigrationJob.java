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
public class DeviceCodeMigrationJob {

    private static final String COLLECTION = "device_codes";
    private static final String DB = "auth_db";

    @Bean
    public Job deviceCodeMigrationJob(JobRepository jobRepository, Step deviceCodeStep) {
        return new JobBuilder("deviceCodeMigrationJob", jobRepository)
            .start(deviceCodeStep)
            .build();
    }

    @Bean
    public Step deviceCodeStep(JobRepository jobRepository,
                                PlatformTransactionManager txManager,
                                DataSource dataSource,
                                @Qualifier("authMongoClient") MongoClient authMongoClient,
                                MigrationReport report,
                                MigrationProperties props) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("deviceCodeReader")
            .dataSource(dataSource)
            .sql("SELECT id, device_code, user_code, expiration, client_id, approved, auth_holder_id FROM device_code")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                String deviceCode = rs.getString("device_code");
                if (deviceCode == null) {
                    throw new MigrationSkippableException(String.valueOf(id), "device_code is null");
                }

                Document doc = new Document();
                doc.put("_id", String.valueOf(id));
                doc.put("deviceCode", deviceCode);
                doc.put("userCode", rs.getString("user_code"));

                java.sql.Timestamp expiration = rs.getTimestamp("expiration");
                doc.put("expiration", expiration != null ? new java.util.Date(expiration.getTime()) : null);

                doc.put("clientId", rs.getString("client_id"));
                doc.put("approved", rs.getBoolean("approved"));

                long authHolderId = rs.getLong("auth_holder_id");
                if (!rs.wasNull()) {
                    doc.put("authHolderId", String.valueOf(authHolderId));
                }

                List<String> scopes = jdbc.queryForList(
                    "SELECT scope FROM device_code_scope WHERE owner_id = ?", String.class, id);
                doc.put("scope", scopes);

                List<Map<String, Object>> requestParams = jdbc.queryForList(
                    "SELECT param, val FROM device_code_request_parameter WHERE owner_id = ?", id);
                Document reqParamsDoc = new Document();
                for (Map<String, Object> param : requestParams) {
                    reqParamsDoc.put((String) param.get("param"), param.get("val"));
                }
                doc.put("requestParameters", reqParamsDoc);

                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(authMongoClient, DB, COLLECTION, report);

        return new StepBuilder("deviceCodeStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
