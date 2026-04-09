package org.mitre.migration.report;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MigrationReport {

    private final Instant startedAt = Instant.now();
    private Instant completedAt;
    private final Map<String, AtomicLong> migratedCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> skippedCounts = new ConcurrentHashMap<>();
    private final List<ErrorRecord> errors = new ArrayList<>();

    public void recordMigrated(String collection) {
        migratedCounts.computeIfAbsent(collection, k -> new AtomicLong()).incrementAndGet();
    }

    public void recordSkipped(String collection, String recordId, String cause) {
        skippedCounts.computeIfAbsent(collection, k -> new AtomicLong()).incrementAndGet();
        synchronized (errors) {
            errors.add(new ErrorRecord(collection, recordId, cause));
        }
    }

    public void complete() {
        this.completedAt = Instant.now();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("startedAt", startedAt.toString());
        map.put("completedAt", completedAt != null ? completedAt.toString() : null);

        Map<String, Long> counts = new LinkedHashMap<>();
        migratedCounts.forEach((k, v) -> counts.put(k, v.get()));
        map.put("migratedCounts", counts);

        Map<String, Long> skipped = new LinkedHashMap<>();
        skippedCounts.forEach((k, v) -> skipped.put(k, v.get()));
        map.put("skippedCounts", skipped);

        map.put("errors", errors);
        return map;
    }

    public List<ErrorRecord> getErrors() { return errors; }
    public Map<String, AtomicLong> getMigratedCounts() { return migratedCounts; }
    public Map<String, AtomicLong> getSkippedCounts() { return skippedCounts; }

    public record ErrorRecord(String collection, String recordId, String cause) {}
}
