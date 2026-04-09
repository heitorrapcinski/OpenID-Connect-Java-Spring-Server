package org.mitre.migration.config;

import org.mitre.migration.report.MigrationReport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationReportConfig {

    @Bean
    public MigrationReport migrationReport() {
        return new MigrationReport();
    }
}
