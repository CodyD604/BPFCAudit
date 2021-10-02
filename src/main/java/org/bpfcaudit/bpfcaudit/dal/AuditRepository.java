package org.bpfcaudit.bpfcaudit.dal;

import org.bpfcaudit.bpfcaudit.model.Audit;
import org.bpfcaudit.bpfcaudit.model.AuditStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public interface AuditRepository extends CrudRepository<Audit, Long> {
    @Async
    CompletableFuture<List<Audit>> findByStatus(AuditStatus status);

    @Async
    CompletableFuture<List<Audit>> findByService_IdAndStatus(Long service_id,
                                                             AuditStatus status);
}
