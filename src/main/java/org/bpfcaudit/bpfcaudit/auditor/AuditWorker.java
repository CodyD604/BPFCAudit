package org.bpfcaudit.bpfcaudit.auditor;

import lombok.Getter;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;

@Component
public class AuditWorker implements Runnable {
    @Getter
    private final ConcurrentHashMap<Integer, LongAdder> ruleHashToRuleCount = new ConcurrentHashMap<>();
    //ConcurrentHashMap<Integer, Rule> ruleHashToRules = new ConcurrentHashMap<>();

    @Override
    public void run() {
        BPFCAuditAdapter bpfcAuditAdapter = new BPFCAuditAdapter(ruleHashToRuleCount);
        WebSocketUpgradeHandler.Builder upgradeHandlerBuilder = new WebSocketUpgradeHandler.Builder();
        WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder.addWebSocketListener(bpfcAuditAdapter).build();

        try {
            Dsl.asyncHttpClient()
                    .prepareGet("ws://0.0.0.0:3030")
                    .setRequestTimeout(5000)
                    .execute(wsHandler)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
