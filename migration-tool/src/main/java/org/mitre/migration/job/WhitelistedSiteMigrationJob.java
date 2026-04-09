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
public class WhitelistedSiteMigrationJob {

    private static final String COLLECTION = "whitelisted_sites";
    private static final String DB = "oidc_db";

    @Bean
    public Job whitelistedSiteMigrationJob(JobRepository jobRepository, Step whitelistedSiteStep) {
        return new JobBuilder("whitelistedSiteMigrationJob", jobRepository)
            .start(whitelistedSiteStep)
            .build();
    }

    @Bean
    public Step whitelistedSiteStep(JobRepository jobRepository,
                                     PlatformTransactionManager txManager,
                                     DataSource dataSource,
                                     @Qualifier("oidcMongoClient") MongoClient oidcMongoClient,
                                     MigrationReport report,
                                     MigrationProperties props) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("whitelistedSiteReader")
            .dataSource(dataSource)
            .sql("SELECT id, creator_user_id, client_id FROM whitelisted_site")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                Document doc = new Document();
                doc.put("_id", String.valueOf(id));
                doc.put("creatorUserId", rs.getString("creator_user_id"));
                doc.put("clientId", rs.getString("client_id"));

                List<String> scopes = jdbc.queryForList(
                    "SELECT scope FROM whitelisted_site_scope WHERE owner_id = ?", String.class, id);
                doc.put("allowedScopes", scopes);

                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(oidcMongoClient, DB, COLLECTION, report);

        return new StepBuilder("whitelistedSiteStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
