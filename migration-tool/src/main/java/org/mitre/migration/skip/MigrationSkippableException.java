package org.mitre.migration.skip;

public class MigrationSkippableException extends RuntimeException {
    private final String recordId;

    public MigrationSkippableException(String recordId, String message) {
        super(message);
        this.recordId = recordId;
    }

    public MigrationSkippableException(String recordId, String message, Throwable cause) {
        super(message, cause);
        this.recordId = recordId;
    }

    public String getRecordId() { return recordId; }
}
