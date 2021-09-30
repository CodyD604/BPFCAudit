package org.bpfcaudit.bpfcaudit.auditor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.bpfcaudit.bpfcaudit.dal.RuleRepository;
import org.bpfcaudit.bpfcaudit.model.Rule;
import org.bpfcaudit.bpfcaudit.model.pojo.Audit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Component
public class BPFCAuditAdapter implements WebSocketListener {
    @Autowired
    public RuleRepository repository;

    private String subscriptionRequestId;
    private long subscriptionId;
    private WebSocket websocket;
    private static final ThreadLocal<ObjectMapper> objectMapper = ThreadLocal.withInitial(ObjectMapper::new);
    public final BlockingQueue<Rule> eventQueue;

    public BPFCAuditAdapter() {
        this.eventQueue = new LinkedBlockingDeque<>();
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
        System.out.println(payload);
        try {
            JSONRPC2Message msg = JSONRPC2Message.parse(payload);

            if (msg instanceof JSONRPC2Notification) {
                Map<String, Object> namedParams = ((JSONRPC2Notification) msg).getNamedParams();
                Audit audit = objectMapper.get().convertValue(namedParams, Audit.class);
                Rule event = new Rule(audit, "fileAccessor");
                repository.save(event);
            } else if (msg instanceof JSONRPC2Response
                    && subscriptionRequestId.equals(((JSONRPC2Response) msg).getID())) {
                subscriptionId = (long)((JSONRPC2Response) msg).getResult();
                System.out.println("Saved subscription id: " + subscriptionId);
                System.out.println("Total saved events: " + repository.count());
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
