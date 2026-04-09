package org.mitre.clientregistry.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "org.mitre.clientregistry.infrastructure.adapter.out.persistence")
@EnableMongoAuditing
public class MongoConfig {
}
