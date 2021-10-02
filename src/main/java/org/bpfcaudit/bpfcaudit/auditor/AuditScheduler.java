package org.bpfcaudit.bpfcaudit.auditor;

import org.bpfcaudit.bpfcaudit.model.Audit;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class AuditScheduler {
    private static final int MAX_CONCURRENT_AUDITS = 1;
    // Each audit has two threads: one to control shutdown and the other to handle audit events
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(MAX_CONCURRENT_AUDITS * 2);

    // TODO: persist rule data
    public static void InitiateAudit(Audit audit) {
        final AuditWorker auditWorker = new AuditWorker();
        Future<?> auditWorkerFuture = scheduler.submit(auditWorker);

        long timeToRun = Math.min(1, Instant.parse(audit.getEndTime())
                .minus(Instant.now().toEpochMilli(), ChronoUnit.MILLIS)
                .toEpochMilli());

        scheduler.schedule(() -> {
            ConcurrentHashMap<Integer, LongAdder> ruleHashToRuleCount = auditWorker.getRuleHashToRuleCount();
            System.out.println("Captured " + ruleHashToRuleCount.get(2) + " events.");
            auditWorkerFuture.cancel(true);
        }, timeToRun, TimeUnit.MILLISECONDS);
    }

    private static void CompleteAudit() {

    }
}
