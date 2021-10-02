package org.bpfcaudit.bpfcaudit.model;

import org.bpfcaudit.bpfcaudit.model.pojo.Result;

import javax.persistence.*;

@Entity
@Table(name = "rules")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    public String[] decision;
    public int policyLine;
    public long count;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="audit_id")
    private Audit audit;

    public Rule(Result result, long count, Audit audit) {
        this.decision = result.decision;
        this.policyLine = result.rule.lineNumber;
        this.count = count;
        this.audit = audit;
    }
}
