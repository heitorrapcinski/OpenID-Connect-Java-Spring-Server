package org.mitre.umaserver.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "org.mitre.umaserver.infrastructure.adapter.out.persistence")
public class MongoConfig {
}
