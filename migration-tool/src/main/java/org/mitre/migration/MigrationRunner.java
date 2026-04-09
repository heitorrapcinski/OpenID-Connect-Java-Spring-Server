package org.mitre.migration;

import org.mitre.migration.config.MigrationProperties;
import org.mitre.migration.report.MigrationReport;
import org.mitre.migration.report.MigrationReportWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigrationRunner.class);

    private final JobLauncher jobLauncher;
    private final List<Job> migrationJobs;
    private final MigrationReport migrationReport;
    private final MigrationReportWriter reportWriter;
    private final MigrationProperties properties;

    public MigrationRunner(JobLauncher jobLauncher,
                           List<Job> migrationJobs,
                           MigrationReport migrationReport,
                           MigrationReportWriter reportWriter,
                           MigrationProperties properties) {
        this.jobLauncher = jobLauncher;
        this.migrationJobs = migrationJobs;
        this.migrationReport = migrationReport;
        this.reportWriter = reportWriter;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting migration with {} jobs", migrationJobs.size());

        for (Job job : migrationJobs) {
            JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
            try {
                var execution = jobLauncher.run(job, params);
                log.info("Job '{}' completed with status: {}", job.getName(), execution.getStatus());
            } catch (Exception e) {
                log.error("Job '{}' failed: {}", job.getName(), e.getMessage(), e);
            }
        }

        reportWriter.write(migrationReport, properties.getReport().getOutputPath());
    }
}
