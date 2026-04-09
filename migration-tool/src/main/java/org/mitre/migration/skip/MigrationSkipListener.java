package org.mitre.migration.skip;

import org.bson.Document;
import org.mitre.migration.report.MigrationReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

public class MigrationSkipListener implements SkipListener<Document, Document> {

    private static final Logger log = LoggerFactory.getLogger(MigrationSkipListener.class);

    private final MigrationReport report;
    private final String collectionName;

    public MigrationSkipListener(MigrationReport report, String collectionName) {
        this.report = report;
        this.collectionName = collectionName;
    }

    @Override
    public void onSkipInRead(Throwable t) {
        String recordId = extractRecordId(t);
        String cause = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
        log.warn("Skipped record during read in collection '{}': id={}, cause={}", collectionName, recordId, cause);
        report.recordSkipped(collectionName, recordId, cause);
    }

    @Override
    public void onSkipInProcess(Document item, Throwable t) {
        String recordId = item != null ? String.valueOf(item.get("_id")) : "unknown";
        String cause = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
        log.warn("Skipped record during process in collection '{}': id={}, cause={}", collectionName, recordId, cause);
        report.recordSkipped(collectionName, recordId, cause);
    }

    @Override
    public void onSkipInWrite(Document item, Throwable t) {
        String recordId = item != null ? String.valueOf(item.get("_id")) : "unknown";
        String cause = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
        log.warn("Skipped record during write in collection '{}': id={}, cause={}", collectionName, recordId, cause);
        report.recordSkipped(collectionName, recordId, cause);
    }

    private String extractRecordId(Throwable t) {
        if (t instanceof MigrationSkippableException mse) {
            return mse.getRecordId();
        }
        return "unknown";
    }
}
