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

@Configuration
public class ClientMigrationJob {

    private static final String COLLECTION = "clients";
    private static final String DB = "client_db";

    @Bean
    public Job clientMigrationJob(JobRepository jobRepository, Step clientStep) {
        return new JobBuilder("clientMigrationJob", jobRepository)
            .start(clientStep)
            .build();
    }

    @Bean
    public Step clientStep(JobRepository jobRepository,
                            PlatformTransactionManager txManager,
                            DataSource dataSource,
                            @Qualifier("clientMongoClient") MongoClient clientMongoClient,
                            MigrationReport report,
                            MigrationProperties props) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        JdbcCursorItemReader<Document> reader = new JdbcCursorItemReaderBuilder<Document>()
            .name("clientReader")
            .dataSource(dataSource)
            .sql("SELECT id, client_id, client_secret, client_name, client_description, client_uri, " +
                 "logo_uri, tos_uri, policy_uri, token_endpoint_auth_method, application_type, subject_type, " +
                 "sector_identifier_uri, jwks_uri, id_token_signed_response_alg, id_token_encrypted_response_alg, " +
                 "id_token_encrypted_response_enc, userinfo_signed_response_alg, userinfo_encrypted_response_alg, " +
                 "userinfo_encrypted_response_enc, request_object_signing_alg, token_endpoint_auth_signing_alg, " +
                 "default_max_age, require_auth_time, access_token_validity_seconds, refresh_token_validity_seconds, " +
                 "id_token_validity_seconds, device_code_validity_seconds, reuse_refresh_tokens, " +
                 "clear_access_tokens_on_refresh, dynamically_registered, allow_introspection, " +
                 "software_id, software_version, created_at FROM client_details")
            .rowMapper((rs, rowNum) -> {
                long id = rs.getLong("id");
                String clientId = rs.getString("client_id");
                if (clientId == null) {
                    throw new MigrationSkippableException(String.valueOf(id), "client_id is null");
                }

                Document doc = new Document();
                doc.put("_id", clientId);
                doc.put("clientId", clientId);
                doc.put("clientSecret", rs.getString("client_secret"));
                doc.put("clientName", rs.getString("client_name"));
                doc.put("clientDescription", rs.getString("client_description"));
                doc.put("clientUri", rs.getString("client_uri"));
                doc.put("logoUri", rs.getString("logo_uri"));
                doc.put("tosUri", rs.getString("tos_uri"));
                doc.put("policyUri", rs.getString("policy_uri"));
                doc.put("tokenEndpointAuthMethod", rs.getString("token_endpoint_auth_method"));
                doc.put("applicationType", rs.getString("application_type"));
                doc.put("subjectType", rs.getString("subject_type"));
                doc.put("sectorIdentifierUri", rs.getString("sector_identifier_uri"));
                doc.put("jwksUri", rs.getString("jwks_uri"));
                doc.put("idTokenSignedResponseAlg", rs.getString("id_token_signed_response_alg"));
                doc.put("idTokenEncryptedResponseAlg", rs.getString("id_token_encrypted_response_alg"));
                doc.put("idTokenEncryptedResponseEnc", rs.getString("id_token_encrypted_response_enc"));
                doc.put("userinfoSignedResponseAlg", rs.getString("userinfo_signed_response_alg"));
                doc.put("userinfoEncryptedResponseAlg", rs.getString("userinfo_encrypted_response_alg"));
                doc.put("userinfoEncryptedResponseEnc", rs.getString("userinfo_encrypted_response_enc"));
                doc.put("requestObjectSigningAlg", rs.getString("request_object_signing_alg"));
                doc.put("tokenEndpointAuthSigningAlg", rs.getString("token_endpoint_auth_signing_alg"));
                doc.put("defaultMaxAge", rs.getObject("default_max_age"));
                doc.put("requireAuthTime", rs.getBoolean("require_auth_time"));
                doc.put("accessTokenValiditySeconds", rs.getObject("access_token_validity_seconds"));
                doc.put("refreshTokenValiditySeconds", rs.getObject("refresh_token_validity_seconds"));
                doc.put("idTokenValiditySeconds", rs.getObject("id_token_validity_seconds"));
                doc.put("deviceCodeValiditySeconds", rs.getObject("device_code_validity_seconds"));
                doc.put("reuseRefreshTokens", rs.getBoolean("reuse_refresh_tokens"));
                doc.put("clearAccessTokensOnRefresh", rs.getBoolean("clear_access_tokens_on_refresh"));
                doc.put("dynamicallyRegistered", rs.getBoolean("dynamically_registered"));
                doc.put("allowIntrospection", rs.getBoolean("allow_introspection"));
                doc.put("softwareId", rs.getString("software_id"));
                doc.put("softwareVersion", rs.getString("software_version"));
                java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
                doc.put("createdAt", createdAt != null ? new java.util.Date(createdAt.getTime()) : null);

                // Sub-queries for @ElementCollection tables
                List<String> scopes = jdbc.queryForList(
                    "SELECT scope FROM client_scope WHERE owner_id = ?", String.class, id);
                doc.put("scope", scopes);

                List<String> grantTypes = jdbc.queryForList(
                    "SELECT grant_type FROM client_grant_type WHERE owner_id = ?", String.class, id);
                doc.put("grantTypes", grantTypes);

                List<String> responseTypes = jdbc.queryForList(
                    "SELECT response_type FROM client_response_type WHERE owner_id = ?", String.class, id);
                doc.put("responseTypes", responseTypes);

                List<String> redirectUris = jdbc.queryForList(
                    "SELECT redirect_uri FROM client_redirect_uri WHERE owner_id = ?", String.class, id);
                doc.put("redirectUris", redirectUris);

                List<String> postLogoutRedirectUris = jdbc.queryForList(
                    "SELECT post_logout_redirect_uri FROM client_post_logout_redirect_uri WHERE owner_id = ?", String.class, id);
                doc.put("postLogoutRedirectUris", postLogoutRedirectUris);

                List<String> requestUris = jdbc.queryForList(
                    "SELECT request_uri FROM client_request_uri WHERE owner_id = ?", String.class, id);
                doc.put("requestUris", requestUris);

                List<String> contacts = jdbc.queryForList(
                    "SELECT contact FROM client_contact WHERE owner_id = ?", String.class, id);
                doc.put("contacts", contacts);

                List<String> defaultAcrValues = jdbc.queryForList(
                    "SELECT default_acr_value FROM client_default_acr_value WHERE owner_id = ?", String.class, id);
                doc.put("defaultAcrValues", defaultAcrValues);

                List<String> claimsRedirectUris = jdbc.queryForList(
                    "SELECT redirect_uri FROM client_claims_redirect_uri WHERE owner_id = ?", String.class, id);
                doc.put("claimsRedirectUris", claimsRedirectUris);

                return doc;
            })
            .build();

        MongoUpsertItemWriter writer = new MongoUpsertItemWriter(clientMongoClient, DB, COLLECTION, report);

        return new StepBuilder("clientStep", jobRepository)
            .<Document, Document>chunk(props.getChunkSize(), txManager)
            .reader(reader)
            .writer(writer)
            .faultTolerant()
            .skipPolicy(new MigrationSkipPolicy())
            .listener(new MigrationSkipListener(report, COLLECTION))
            .build();
    }
}
