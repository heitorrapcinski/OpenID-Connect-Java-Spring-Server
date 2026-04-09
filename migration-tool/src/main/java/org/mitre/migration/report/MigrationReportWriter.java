package org.mitre.migration.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class MigrationReportWriter {

    private static final Logger log = LoggerFactory.getLogger(MigrationReportWriter.class);
    private final ObjectMapper objectMapper;

    public MigrationReportWriter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void write(MigrationReport report, String outputPath) {
        report.complete();
        try {
            Path path = Path.of(outputPath);
            objectMapper.writeValue(path.toFile(), report.toMap());
            log.info("Migration report written to {}", path.toAbsolutePath());
            logSummary(report);
        } catch (IOException e) {
            log.error("Failed to write migration report to {}: {}", outputPath, e.getMessage());
        }
    }

    private void logSummary(MigrationReport report) {
        log.info("=== Migration Summary ===");
        report.getMigratedCounts().forEach((col, count) ->
            log.info("  {}: {} documents migrated", col, count.get()));
        report.getSkippedCounts().forEach((col, count) ->
            log.warn("  {}: {} records skipped (errors)", col, count.get()));
        if (!report.getErrors().isEmpty()) {
            log.warn("  Total errors: {}", report.getErrors().size());
        }
    }
}
