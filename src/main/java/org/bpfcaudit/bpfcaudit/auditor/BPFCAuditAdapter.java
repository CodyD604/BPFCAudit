package org.bpfcaudit.bpfcaudit.auditor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.bpfcaudit.bpfcaudit.dal.OldRuleRepository;
import org.bpfcaudit.bpfcaudit.model.OldRule;
import org.bpfcaudit.bpfcaudit.model.pojo.AuditEvent;
import org.bpfcaudit.bpfcaudit.model.pojo.Result;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class BPFCAuditAdapter implements WebSocketListener {
    private String subscriptionRequestId;
    private long subscriptionId;
    private WebSocket websocket;
    private static final ThreadLocal<ObjectMapper> objectMapper = ThreadLocal
            .withInitial(() -> new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    private final ConcurrentHashMap<Long, LongAdder> ruleHashToRuleCount;
    private final ConcurrentHashMap<Long, Result> ruleHashToResult;
    private final LongAdder rulesFoundCount = new LongAdder();
    private static final int FLUSH_EVERY = 1000; // Flush every 1000 events
    public OldRuleRepository repository;

    public BPFCAuditAdapter(ConcurrentHashMap<Long, LongAdder> ruleHashToRuleCount, ConcurrentHashMap<Long, Result> ruleHashToResult, OldRuleRepository oldRuleRepository) {
        this.ruleHashToRuleCount = ruleHashToRuleCount;
        this.ruleHashToResult = ruleHashToResult;
        this.repository = oldRuleRepository;
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
        rulesFoundCount.increment();
        if (rulesFoundCount.intValue() % 1000 == 0) repository.flush();

        try {
            JSONRPC2Message msg = JSONRPC2Message.parse(payload);

            if (msg instanceof JSONRPC2Notification) {
                Map<String, Object> namedParams = ((JSONRPC2Notification) msg).getNamedParams();
                AuditEvent audit = objectMapper.get().convertValue(namedParams, AuditEvent.class);
                OldRule event = new OldRule(audit.result, "123", "myService");
                repository.save(event);
            } else if (msg instanceof JSONRPC2Response
                    && subscriptionRequestId.equals(((JSONRPC2Response) msg).getID())) {
                subscriptionId = (long)((JSONRPC2Response) msg).getResult();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void onAuditCompletion() throws InterruptedException {
        JSONRPC2Request unsubscribeRequest = new JSONRPC2Request("audit_unsubscribe", List.of(subscriptionId),
                UUID.randomUUID().toString());
        repository.flush();
        // TODO: should block here?
        if (websocket != null && websocket.isOpen()) {
            websocket.sendTextFrame(unsubscribeRequest.toJSONString()).await();
            websocket.sendCloseFrame();
        }
    }
}
