package org.bpfcaudit.bpfcaudit.dal;

import org.bpfcaudit.bpfcaudit.model.Service;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends CrudRepository<Service, Long> { }
