package org.bpfcaudit.bpfcaudit.dal;

import org.bpfcaudit.bpfcaudit.model.Rule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends CrudRepository<Rule, Long> { }
