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
public class PairwiseMigrationJob {

    private static final String COLLECTION = "pairwise_identifiers";
    private static final String DB = "oidc_db";

    @Bean
    public Job pairwiseMigrationJob(JobRepository jobRepository, Step pairwiseStep) {
        return new JobBuilder("pairwiseMigrationJob", jobRepository)
            .start(pairwiseStep)
            .build();
    }

    @Bean
    public Step pairwiseStep(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              DataSource dataSource,
                              @Qualifier("oidcMongoClient") MongoClient oidcMongoClient,
                              MigrationReport report,
                              MigrationProperties props) {
        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("pairwiseReader")
            .dataSource(dataSource)
            .sql("SELECT id, identifier, sub, sector_identifier FROM pairwise_identifier")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                Document doc = new Document();
                doc.put("_id", String.valueOf(id));
                doc.put("identifier", rs.getString("identifier"));
                doc.put("sub", rs.getString("sub"));
                doc.put("sectorIdentifier", rs.getString("sector_identifier"));
                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(oidcMongoClient, DB, COLLECTION, report);

        return new StepBuilder("pairwiseStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
