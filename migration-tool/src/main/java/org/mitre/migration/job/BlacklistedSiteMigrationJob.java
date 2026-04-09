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
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BlacklistedSiteMigrationJob {

    private static final String COLLECTION = "blacklisted_sites";
    private static final String DB = "oidc_db";

    @Bean
    public Job blacklistedSiteMigrationJob(JobRepository jobRepository, Step blacklistedSiteStep) {
        return new JobBuilder("blacklistedSiteMigrationJob", jobRepository)
            .start(blacklistedSiteStep)
            .build();
    }

    @Bean
    public Step blacklistedSiteStep(JobRepository jobRepository,
                                     PlatformTransactionManager txManager,
                                     DataSource dataSource,
                                     @Qualifier("oidcMongoClient") MongoClient oidcMongoClient,
                                     MigrationReport report,
                                     MigrationProperties props) {
        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("blacklistedSiteReader")
            .dataSource(dataSource)
            .sql("SELECT id, uri FROM blacklisted_site")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                Document doc = new Document();
                doc.put("_id", String.valueOf(id));
                doc.put("uri", rs.getString("uri"));
                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(oidcMongoClient, DB, COLLECTION, report);

        return new StepBuilder("blacklistedSiteStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
