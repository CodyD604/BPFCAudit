package org.bpfcaudit.bpfcaudit.dal;

import org.bpfcaudit.bpfcaudit.model.Capture;
import org.bpfcaudit.bpfcaudit.model.CaptureStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public interface CaptureRepository extends CrudRepository<Capture, Long> {
    @Async
    CompletableFuture<List<Capture>> findByStatus(CaptureStatus status);

    @Async
    CompletableFuture<List<Capture>> findByService_IdAndStatus(Long service_id,
                                                              CaptureStatus status);
}
