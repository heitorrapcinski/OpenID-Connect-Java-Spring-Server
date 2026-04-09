package org.mitre.authserver.infrastructure.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "org.mitre.authserver.infrastructure.adapter.out.persistence")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/auth_db}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        return "auth_db";
    }

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }
}
