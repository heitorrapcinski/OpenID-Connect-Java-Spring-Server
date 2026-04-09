package org.mitre.migration.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoMigrationConfig {

    @Value("${spring.data.mongodb.auth-uri}")
    private String authUri;

    @Value("${spring.data.mongodb.client-uri}")
    private String clientUri;

    @Value("${spring.data.mongodb.oidc-uri}")
    private String oidcUri;

    @Value("${spring.data.mongodb.uma-uri}")
    private String umaUri;

    @Value("${spring.data.mongodb.scope-uri}")
    private String scopeUri;

    @Bean(name = "authMongoClient", destroyMethod = "close")
    public MongoClient authMongoClient() {
        return MongoClients.create(authUri);
    }

    @Bean(name = "clientMongoClient", destroyMethod = "close")
    public MongoClient clientMongoClient() {
        return MongoClients.create(clientUri);
    }

    @Bean(name = "oidcMongoClient", destroyMethod = "close")
    public MongoClient oidcMongoClient() {
        return MongoClients.create(oidcUri);
    }

    @Bean(name = "umaMongoClient", destroyMethod = "close")
    public MongoClient umaMongoClient() {
        return MongoClients.create(umaUri);
    }

    @Bean(name = "scopeMongoClient", destroyMethod = "close")
    public MongoClient scopeMongoClient() {
        return MongoClients.create(scopeUri);
    }
}
