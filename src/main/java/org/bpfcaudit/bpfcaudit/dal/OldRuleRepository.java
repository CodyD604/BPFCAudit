package org.bpfcaudit.bpfcaudit.dal;

import org.bpfcaudit.bpfcaudit.model.OldRule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OldRuleRepository extends CrudRepository<OldRule, Long> { }