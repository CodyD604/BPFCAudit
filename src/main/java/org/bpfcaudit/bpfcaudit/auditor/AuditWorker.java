package org.bpfcaudit.bpfcaudit.auditor;

import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.bpfcaudit.bpfcaudit.dal.AuditRepository;
import org.bpfcaudit.bpfcaudit.dal.OldRuleRepository;
import org.bpfcaudit.bpfcaudit.dal.RuleRepository;
import org.bpfcaudit.bpfcaudit.model.Audit;
import org.bpfcaudit.bpfcaudit.model.AuditStatus;
import org.bpfcaudit.bpfcaudit.model.Rule;
import org.bpfcaudit.bpfcaudit.model.pojo.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;

// TODO: would like this to be a bean if possible
public class AuditWorker implements Runnable {
    private final RuleRepository ruleRepository;
    private final AuditRepository auditRepository;
    private final OldRuleRepository oldRuleRepository;
    private final ConcurrentHashMap<Long, LongAdder> ruleHashToRuleCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Result> ruleHashToResults = new ConcurrentHashMap<>();
    private BPFCAuditAdapter bpfcAuditAdapter;
    private final long auditId;

    public AuditWorker(Long auditId, RuleRepository ruleRepository, AuditRepository auditRepository, OldRuleRepository oldRuleRepository) {
        this.auditId = auditId;
        this.ruleRepository = ruleRepository;
        this.auditRepository = auditRepository;
        this.oldRuleRepository = oldRuleRepository;
    }

    @Override
    public void run() {
        bpfcAuditAdapter = new BPFCAuditAdapter(ruleHashToRuleCount, ruleHashToResults, this.oldRuleRepository);
        WebSocketUpgradeHandler.Builder upgradeHandlerBuilder = new WebSocketUpgradeHandler.Builder();
        WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder.addWebSocketListener(bpfcAuditAdapter).build();

        try {
            Dsl.asyncHttpClient()
                    .prepareGet("ws://0.0.0.0:3030")
                    .setRequestTimeout(5000)
                    .execute(wsHandler)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO: should kill worker
            this.onAuditFailure("BPFContain websocket interrupted " + e.getMessage() + ".");
            e.printStackTrace();
        }
    }

    public void onAuditCompletion() throws InterruptedException {
        // Send unsub message and close socket
        bpfcAuditAdapter.onAuditCompletion();

        // TODO: proper logging
        // Log how many audit events were captured
        /*
        long totalEvents = 0;
        for (Long ruleHash : this.ruleHashToRuleCount.keySet()) {
            totalEvents += this.ruleHashToRuleCount.get(ruleHash).longValue();
        }
         */
        System.out.println("Captured " + oldRuleRepository.count() + " events.");
    }

    // TODO: async
    public void onAuditSuccess() throws InterruptedException {
        this.onAuditCompletion();

        // Update capture, persist audit data
        Optional<Audit> auditWrapped = auditRepository.findById(this.auditId);

        if (auditWrapped.isPresent()) {
            Audit audit = auditWrapped.get();
            audit.complete(AuditStatus.SUCCESSFUL, "Audit successful.");
            auditRepository.save(audit);

            // Persist audit data
            List<Rule> rules = new ArrayList<>(this.ruleHashToResults.keySet().size());
            for (Long ruleHash : this.ruleHashToResults.keySet()) {
                rules.add(new Rule(this.ruleHashToResults.get(ruleHash),
                        this.ruleHashToRuleCount.get(ruleHash).longValue(), audit));
            }
            ruleRepository.saveAll(rules);
        }
    }

    // TODO: async, better error handling for audits
    public void onAuditFailure(String message) {
        // Update capture
        Optional<Audit> auditWrapped = auditRepository.findById(this.auditId);

        if (auditWrapped.isPresent()) {
            Audit audit = auditWrapped.get();
            audit.complete(AuditStatus.FAILED, message);
            auditRepository.save(audit);
        }
    }
}
