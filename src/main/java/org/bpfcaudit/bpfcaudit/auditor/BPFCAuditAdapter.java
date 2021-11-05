package org.bpfcaudit.bpfcaudit.auditor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.bpfcaudit.bpfcaudit.model.pojo.AuditEvent;
import org.bpfcaudit.bpfcaudit.model.pojo.AuditStats;
import org.bpfcaudit.bpfcaudit.model.pojo.Result;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class BPFCAuditAdapter implements WebSocketListener {
    private String subscriptionRequestId;
    private long subscriptionId;
    private WebSocket websocket;
    private static final ThreadLocal<ObjectMapper> objectMapper = ThreadLocal
            .withInitial(() -> new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    private final ConcurrentHashMap<Long, LongAdder> ruleHashToRuleCount;
    private final ConcurrentHashMap<Long, Result> ruleHashToResult;
    private AuditStats auditStats;
    private final Long auditStatsRequestId = 9999L; // This is good enough for testing, should never be more than a few subscriptions

    public BPFCAuditAdapter(ConcurrentHashMap<Long, LongAdder> ruleHashToRuleCount, ConcurrentHashMap<Long, Result> ruleHashToResult) {
        this.ruleHashToRuleCount = ruleHashToRuleCount;
        this.ruleHashToResult = ruleHashToResult;
    }

    @Override
    public void onOpen(WebSocket websocket) {
        this.websocket = websocket;
        subscriptionRequestId = UUID.randomUUID().toString();
        // TODO: filters
        JSONRPC2Request request = new JSONRPC2Request("audit_subscribe", subscriptionRequestId);
        websocket.sendTextFrame(request.toJSONString());
    }

    @Override
    public void onClose(WebSocket websocket, int code, String reason) {
        // WebSocket connection closed
        // TODO: handle close from bpf contain
    }

    @Override
    public void onError(Throwable t) {
        // WebSocket connection error
        // TODO: better logging
        t.printStackTrace();
    }

    @Override
    public void onTextFrame(String payload, boolean finalFragment, int rsv) {
        try {
            JSONRPC2Message msg = JSONRPC2Message.parse(payload);

            if (msg instanceof JSONRPC2Notification) {
                Map<String, Object> namedParams = ((JSONRPC2Notification) msg).getNamedParams();

                // TODO: remove once rule hashes are implemented on BPFContain side. Let subscription act as our hash value for now.
                Long hash = (Long) namedParams.getOrDefault("subscription", null);

                if (hash != null) {
                    ruleHashToRuleCount.computeIfAbsent(hash, k -> new LongAdder()).increment();
                    ruleHashToResult.computeIfAbsent(hash, k -> {
                        // Note: objectMapper's reflection is expensive, so we want to do this as little as possible
                        AuditEvent audit = objectMapper.get().convertValue(namedParams, AuditEvent.class);
                        return audit.result;
                    });
                }
            } else if (msg instanceof JSONRPC2Response) {
                JSONRPC2Response response = (JSONRPC2Response) msg;
                if (subscriptionRequestId.equals(response.getID())) {
                    subscriptionId = (long) response.getResult();
                } else if (auditStatsRequestId.equals(response.getID())) {
                    auditStats = objectMapper.get().convertValue(response.getResult(), AuditStats.class);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public AuditStats onAuditCompletion() throws InterruptedException {
        JSONRPC2Request unsubscribeRequest = new JSONRPC2Request("audit_unsubscribe", List.of(subscriptionId),
                UUID.randomUUID().toString());

        // Audit stats are a global count, so there is no need to provide an id
        JSONRPC2Request auditStatsRequest = new JSONRPC2Request("audit_stats", auditStatsRequestId);
        // TODO: should block here?
        if (websocket != null && websocket.isOpen()) {
            websocket.sendTextFrame(auditStatsRequest.toJSONString());
            // Wait a few seconds so that BPFContain has time to receive and send back the response for our stats request
            TimeUnit.SECONDS.sleep(3);
            websocket.sendTextFrame(unsubscribeRequest.toJSONString());
            websocket.sendCloseFrame();
        }

        return auditStats;
    }
}
