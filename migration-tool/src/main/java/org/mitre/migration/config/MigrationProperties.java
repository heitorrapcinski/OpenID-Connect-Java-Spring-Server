package org.mitre.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

    private Report report = new Report();
    private int chunkSize = 100;

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }
    public int getChunkSize() { return chunkSize; }
    public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }

    public static class Report {
        private String outputPath = "migration-report.json";
        public String getOutputPath() { return outputPath; }
        public void setOutputPath(String outputPath) { this.outputPath = outputPath; }
    }
}
