package org.bpfcaudit.bpfcaudit.auditor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.bpfcaudit.bpfcaudit.model.pojo.AuditEvent;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class BPFCAuditAdapter implements WebSocketListener {
    private String subscriptionRequestId;
    private long subscriptionId;
    private WebSocket websocket;
    private static final ThreadLocal<ObjectMapper> objectMapper = ThreadLocal.withInitial(ObjectMapper::new);
    private final ConcurrentHashMap<Integer, LongAdder> ruleHashToRuleCount;
    private final Integer tempHash = 2;

    public BPFCAuditAdapter(ConcurrentHashMap<Integer, LongAdder> ruleHashToRuleCount) {
        this.ruleHashToRuleCount = ruleHashToRuleCount;
    }

    @Override
    public void onOpen(WebSocket websocket) {
        this.websocket = websocket;
        subscriptionRequestId = UUID.randomUUID().toString();
        JSONRPC2Request request = new JSONRPC2Request("audit_subscribe", subscriptionRequestId);
        websocket.sendTextFrame(request.toJSONString());
    }

    @Override
    public void onClose(WebSocket websocket, int code, String reason) {
        // WebSocket connection closed
    }

    @Override
    public void onError(Throwable t) {
        // WebSocket connection error
        t.printStackTrace();
    }

    @Override
    public void onTextFrame(String payload, boolean finalFragment, int rsv) {
        try {
            JSONRPC2Message msg = JSONRPC2Message.parse(payload);

            if (msg instanceof JSONRPC2Notification) {
                Map<String, Object> namedParams = ((JSONRPC2Notification) msg).getNamedParams();
                AuditEvent audit = objectMapper.get().convertValue(namedParams, AuditEvent.class);
                ruleHashToRuleCount.computeIfAbsent(tempHash, k -> new LongAdder()).increment();
            } else if (msg instanceof JSONRPC2Response
                    && subscriptionRequestId.equals(((JSONRPC2Response) msg).getID())) {
                subscriptionId = (long)((JSONRPC2Response) msg).getResult();
                System.out.println("Saved subscription id: " + subscriptionId);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // TODO: may not be necessary
    @PreDestroy
    public void onDestroy() {
        System.out.println("Sending audit unsubscribe message...");
        JSONRPC2Request unsubscribeRequest = new JSONRPC2Request("audit_unsubscribe", List.of(subscriptionId),
                UUID.randomUUID().toString());
        websocket.sendTextFrame(unsubscribeRequest.toJSONString());
    }
}
