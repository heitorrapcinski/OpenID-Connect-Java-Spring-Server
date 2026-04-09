package org.mitre.scopemanager.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB infrastructure configuration.
 * Connection URI is provided via spring.data.mongodb.uri in application.yml — no hardcoded values.
 */
@Configuration
@EnableMongoRepositories(basePackages = "org.mitre.scopemanager.infrastructure.adapter.out.persistence")
@EnableMongoAuditing
public class MongoConfig {
}
