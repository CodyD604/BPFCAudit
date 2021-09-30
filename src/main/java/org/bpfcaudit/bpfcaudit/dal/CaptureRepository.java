package org.bpfcaudit.bpfcaudit.dal;

import org.bpfcaudit.bpfcaudit.model.Capture;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaptureRepository extends CrudRepository<Capture, Long> { }
