package org.bpfcaudit.bpfcaudit.auditor;

import org.bpfcaudit.bpfcaudit.dal.AuditRepository;
import org.bpfcaudit.bpfcaudit.dal.OldRuleRepository;
import org.bpfcaudit.bpfcaudit.dal.RuleRepository;
import org.bpfcaudit.bpfcaudit.model.Audit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AuditScheduler {
    // TODO: confirm corePoolSize understanding is correct
    // Each audit has two threads: one to control shutdown and the other to handle audit events
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Set<Long> serviceIdsWithRunningAudit = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // TODO: investigate consequences of this if any. Would ideally inject the repositories to each audit worker
    @Autowired
    private RuleRepository ruleRepository;
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private OldRuleRepository oldRuleRepository;

    // TODO: test and handle running multiple audits at once
    public void InitiateAudit(Audit audit) throws Exception {
        Long serviceId = audit.getService().getId();
        // TODO: find a better way to enforce this invariant. As is is a race condition. Need something atomic.
        // Though there should be no ill consequences of multiple running captures for the same service.
        if (serviceIdsWithRunningAudit.contains(serviceId)) {
            throw new Exception("Audit already in progress for service " + serviceId + ".");
        }
        serviceIdsWithRunningAudit.add(serviceId);

        final AuditWorker auditWorker = new AuditWorker(audit.getId(), this.ruleRepository, this.auditRepository, this.oldRuleRepository);
        Future<?> auditWorkerFuture = scheduler.submit(auditWorker);

        long timeToRun = Math.max(1, Instant.parse(audit.getEndTime())
                .minus(Instant.now().toEpochMilli(), ChronoUnit.MILLIS)
                .toEpochMilli());

        scheduler.schedule(() -> {
            // TODO: verify that worker has started before scheduling cancellation
            try {
                auditWorker.onAuditSuccess();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                serviceIdsWithRunningAudit.remove(serviceId);
            }
            auditWorkerFuture.cancel(true);
        }, timeToRun, TimeUnit.MILLISECONDS);
    }
}
