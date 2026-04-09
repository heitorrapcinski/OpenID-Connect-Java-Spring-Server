package org.mitre.migration.job;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.mitre.migration.config.MigrationProperties;
import org.mitre.migration.report.MigrationReport;
import org.mitre.migration.skip.MigrationSkipListener;
import org.mitre.migration.skip.MigrationSkipPolicy;
import org.mitre.migration.writer.MongoUpsertItemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AuthenticationHolderMigrationJob {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationHolderMigrationJob.class);
    private static final String COLLECTION = "authentication_holders";
    private static final String DB = "auth_db";

    /**
     * Eagerly loads all authentication holders from SQL at startup.
     * This cache is used by token migration jobs (refresh, access, authorization code).
     */
    @Bean
    public Map<Long, Document> authHolderCache(DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        Map<Long, Document> cache = new HashMap<>();

        List<Map<String, Object>> holders = jdbc.queryForList(
            "SELECT id, client_id, user_auth_id, approved, redirect_uri FROM authentication_holder");

        for (Map<String, Object> row : holders) {
            long id = ((Number) row.get("id")).longValue();
            Document doc = buildAuthHolderDocument(jdbc, id, row);
            cache.put(id, doc);
        }

        log.info("Loaded {} authentication holders into cache", cache.size());
        return cache;
    }

    private Document buildAuthHolderDocument(JdbcTemplate jdbc, long id, Map<String, Object> row) {
        Document doc = new Document();
        doc.put("_id", String.valueOf(id));
        doc.put("clientId", row.get("client_id"));
        doc.put("approved", row.get("approved"));
        doc.put("redirectUri", row.get("redirect_uri"));

        // Load saved_user_auth if present
        Object userAuthIdObj = row.get("user_auth_id");
        if (userAuthIdObj != null) {
            long userAuthId = ((Number) userAuthIdObj).longValue();
            List<Map<String, Object>> userAuths = jdbc.queryForList(
                "SELECT id, name, authenticated FROM saved_user_auth WHERE id = ?", userAuthId);
            if (!userAuths.isEmpty()) {
                Map<String, Object> ua = userAuths.get(0);
                Document userAuth = new Document();
                userAuth.put("name", ua.get("name"));
                userAuth.put("authenticated", ua.get("authenticated"));

                List<String> authorities = jdbc.queryForList(
                    "SELECT authority FROM saved_user_auth_authority WHERE owner_id = ?", String.class, userAuthId);
                userAuth.put("authorities", authorities);
                doc.put("userAuth", userAuth);
            }
        }

        List<String> scopes = jdbc.queryForList(
            "SELECT scope FROM authentication_holder_scope WHERE owner_id = ?", String.class, id);
        doc.put("scope", scopes);

        List<String> responseTypes = jdbc.queryForList(
            "SELECT response_type FROM authentication_holder_response_type WHERE owner_id = ?", String.class, id);
        doc.put("responseTypes", responseTypes);

        List<String> authorities = jdbc.queryForList(
            "SELECT authority FROM authentication_holder_authority WHERE owner_id = ?", String.class, id);
        doc.put("authorities", authorities);

        // Extensions as map
        List<Map<String, Object>> extensions = jdbc.queryForList(
            "SELECT extension, val FROM authentication_holder_extension WHERE owner_id = ?", id);
        Document extDoc = new Document();
        for (Map<String, Object> ext : extensions) {
            extDoc.put((String) ext.get("extension"), ext.get("val"));
        }
        doc.put("extensions", extDoc);

        // Request parameters as map
        List<Map<String, Object>> requestParams = jdbc.queryForList(
            "SELECT param, val FROM authentication_holder_request_parameter WHERE owner_id = ?", id);
        Document reqParamsDoc = new Document();
        for (Map<String, Object> param : requestParams) {
            reqParamsDoc.put((String) param.get("param"), param.get("val"));
        }
        doc.put("requestParameters", reqParamsDoc);

        return doc;
    }

    @Bean
    public Job authenticationHolderMigrationJob(JobRepository jobRepository, Step authHolderStep) {
        return new JobBuilder("authenticationHolderMigrationJob", jobRepository)
            .start(authHolderStep)
            .build();
    }

    @Bean
    public Step authHolderStep(JobRepository jobRepository,
                                PlatformTransactionManager txManager,
                                DataSource dataSource,
                                @Qualifier("authMongoClient") MongoClient authMongoClient,
                                MigrationReport report,
                                MigrationProperties props,
                                Map<Long, Document> authHolderCache) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("authHolderReader")
            .dataSource(dataSource)
            .sql("SELECT id, client_id, user_auth_id, approved, redirect_uri FROM authentication_holder")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                // Reuse from cache if available, otherwise build
                Document cached = authHolderCache.get(id);
                return cached != null ? cached : new Document("_id", String.valueOf(id));
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(authMongoClient, DB, COLLECTION, report);

        return new StepBuilder("authHolderStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
