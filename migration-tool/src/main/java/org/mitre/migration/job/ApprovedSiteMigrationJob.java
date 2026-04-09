package org.mitre.migration.job;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.mitre.migration.config.MigrationProperties;
import org.mitre.migration.report.MigrationReport;
import org.mitre.migration.skip.MigrationSkipListener;
import org.mitre.migration.skip.MigrationSkipPolicy;
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

@Configuration
public class ApprovedSiteMigrationJob {

    private static final String COLLECTION = "approved_sites";
    private static final String DB = "oidc_db";

    @Bean
    public Job approvedSiteMigrationJob(JobRepository jobRepository, Step approvedSiteStep) {
        return new JobBuilder("approvedSiteMigrationJob", jobRepository)
            .start(approvedSiteStep)
            .build();
    }

    @Bean
    public Step approvedSiteStep(JobRepository jobRepository,
                                  PlatformTransactionManager txManager,
                                  DataSource dataSource,
                                  @Qualifier("oidcMongoClient") MongoClient oidcMongoClient,
                                  MigrationReport report,
                                  MigrationProperties props) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("approvedSiteReader")
            .dataSource(dataSource)
            .sql("SELECT id, user_id, client_id, creation_date, access_date, timeout_date FROM approved_site")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                Document doc = new Document();
                doc.put("_id", String.valueOf(id));
                doc.put("userId", rs.getString("user_id"));
                doc.put("clientId", rs.getString("client_id"));

                java.sql.Timestamp creationDate = rs.getTimestamp("creation_date");
                doc.put("creationDate", creationDate != null ? new java.util.Date(creationDate.getTime()) : null);

                java.sql.Timestamp accessDate = rs.getTimestamp("access_date");
                doc.put("accessDate", accessDate != null ? new java.util.Date(accessDate.getTime()) : null);

                java.sql.Timestamp timeoutDate = rs.getTimestamp("timeout_date");
                doc.put("timeoutDate", timeoutDate != null ? new java.util.Date(timeoutDate.getTime()) : null);

                List<String> scopes = jdbc.queryForList(
                    "SELECT scope FROM approved_site_scope WHERE owner_id = ?", String.class, id);
                doc.put("allowedScopes", scopes);

                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(oidcMongoClient, DB, COLLECTION, report);

        return new StepBuilder("approvedSiteStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
