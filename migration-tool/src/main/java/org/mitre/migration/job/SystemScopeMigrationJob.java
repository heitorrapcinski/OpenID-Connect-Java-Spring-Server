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
public class SystemScopeMigrationJob {

    private static final String COLLECTION = "system_scopes";
    private static final String DB = "scope_db";

    @Bean
    public Job systemScopeMigrationJob(JobRepository jobRepository,
                                        Step systemScopeStep) {
        return new JobBuilder("systemScopeMigrationJob", jobRepository)
            .start(systemScopeStep)
            .build();
    }

    @Bean
    public Step systemScopeStep(JobRepository jobRepository,
                                 PlatformTransactionManager txManager,
                                 DataSource dataSource,
                                 @Qualifier("scopeMongoClient") MongoClient scopeMongoClient,
                                 MigrationReport report,
                                 MigrationProperties props) {
        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("systemScopeReader")
            .dataSource(dataSource)
            .sql("SELECT id, value, description, icon, default_scope, restricted FROM system_scope")
            .rowMapper((rs, rowNum) -> {
                Document doc = new Document();
                String value = rs.getString("value");
                doc.put("_id", value);
                doc.put("value", value);
                doc.put("description", rs.getString("description"));
                doc.put("icon", rs.getString("icon"));
                doc.put("defaultScope", rs.getBoolean("default_scope"));
                doc.put("restricted", rs.getBoolean("restricted"));
                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(scopeMongoClient, DB, COLLECTION, report);

        return new StepBuilder("systemScopeStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
