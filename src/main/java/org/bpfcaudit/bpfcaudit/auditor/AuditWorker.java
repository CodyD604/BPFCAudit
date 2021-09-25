package org.bpfcaudit.bpfcaudit.auditor;

import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class AuditWorker {
    @Autowired
    private BPFCAuditAdapter bpfcAuditAdapter;

    public void run() throws ExecutionException, InterruptedException {
        WebSocketUpgradeHandler.Builder upgradeHandlerBuilder = new WebSocketUpgradeHandler.Builder();
        WebSocketUpgradeHandler wsHandler = upgradeHandlerBuilder.addWebSocketListener(bpfcAuditAdapter).build();

        Dsl.asyncHttpClient()
                .prepareGet("ws://0.0.0.0:3030")
                .setRequestTimeout(5000)
                .execute(wsHandler)
                .get();

        /*
        BlockingQueue<Event> eventQueue = bpfcAuditAdapter.eventQueue;

        while (true) {
            eventQueue.drainTo();
        }

         */
    }
}
