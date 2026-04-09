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

@Configuration
public class UserInfoMigrationJob {

    private static final String COLLECTION = "user_info";
    private static final String DB = "oidc_db";

    @Bean
    public Job userInfoMigrationJob(JobRepository jobRepository, Step userInfoStep) {
        return new JobBuilder("userInfoMigrationJob", jobRepository)
            .start(userInfoStep)
            .build();
    }

    @Bean
    public Step userInfoStep(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              DataSource dataSource,
                              @Qualifier("oidcMongoClient") MongoClient oidcMongoClient,
                              MigrationReport report,
                              MigrationProperties props) {
        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("userInfoReader")
            .dataSource(dataSource)
            .sql("SELECT u.id, u.sub, u.preferred_username, u.name, u.given_name, u.family_name, " +
                 "u.middle_name, u.nickname, u.email, u.email_verified, u.phone_number, " +
                 "u.phone_number_verified, u.gender, u.birthdate, u.zone_info, u.locale, " +
                 "u.updated_time, u.profile, u.picture, u.website, " +
                 "a.formatted, a.street_address, a.locality, a.region, a.postal_code, a.country " +
                 "FROM user_info u LEFT JOIN address a ON u.address_id = a.id")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                String sub = rs.getString("sub");
                if (sub == null) {
                    throw new MigrationSkippableException(String.valueOf(id), "sub is null");
                }

                Document doc = new Document();
                doc.put("_id", sub);
                doc.put("sub", sub);
                doc.put("preferredUsername", rs.getString("preferred_username"));
                doc.put("name", rs.getString("name"));
                doc.put("givenName", rs.getString("given_name"));
                doc.put("familyName", rs.getString("family_name"));
                doc.put("middleName", rs.getString("middle_name"));
                doc.put("nickname", rs.getString("nickname"));
                doc.put("email", rs.getString("email"));
                doc.put("emailVerified", rs.getBoolean("email_verified"));
                doc.put("phoneNumber", rs.getString("phone_number"));
                doc.put("phoneNumberVerified", rs.getBoolean("phone_number_verified"));
                doc.put("gender", rs.getString("gender"));
                doc.put("birthdate", rs.getString("birthdate"));
                doc.put("zoneInfo", rs.getString("zone_info"));
                doc.put("locale", rs.getString("locale"));
                doc.put("updatedTime", rs.getString("updated_time"));
                doc.put("profile", rs.getString("profile"));
                doc.put("picture", rs.getString("picture"));
                doc.put("website", rs.getString("website"));

                // Embedded address subdocument
                String formatted = rs.getString("formatted");
                if (formatted != null || rs.getString("street_address") != null) {
                    Document address = new Document();
                    address.put("formatted", formatted);
                    address.put("streetAddress", rs.getString("street_address"));
                    address.put("locality", rs.getString("locality"));
                    address.put("region", rs.getString("region"));
                    address.put("postalCode", rs.getString("postal_code"));
                    address.put("country", rs.getString("country"));
                    doc.put("address", address);
                }

                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(oidcMongoClient, DB, COLLECTION, report);

        return new StepBuilder("userInfoStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
