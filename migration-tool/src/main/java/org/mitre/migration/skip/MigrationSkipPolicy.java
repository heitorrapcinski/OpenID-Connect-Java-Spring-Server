package org.mitre.migration.skip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class MigrationSkipPolicy implements SkipPolicy {

    private static final Logger log = LoggerFactory.getLogger(MigrationSkipPolicy.class);

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if (t instanceof MigrationSkippableException) {
            log.warn("Skipping record due to: {}", t.getMessage());
            return true;
        }
        // Don't skip unexpected errors
        return false;
    }
}
