package org.bpfcaudit.bpfcaudit.model;

import org.bpfcaudit.bpfcaudit.model.pojo.AuditEvent;
import org.bpfcaudit.bpfcaudit.model.pojo.Result;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "rules")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;
    @Transient
    public BigInteger containerId;
    public String[] decision;
    public long nsPid;
    public long pid;
    @Transient
    public BigInteger policyId;
    public String serviceName;
    public String ruleName;
    public int policyLine;

    public Rule(AuditEvent auditPOJO, String serviceName) {
        Result result = auditPOJO.result;
        this.containerId = result.containerId;
        this.decision = result.decision;
        this.nsPid = result.nsPid;
        this.pid = result.pid;
        this.policyId = result.rule.policyId;
        this.ruleName = result.rule.ruleName;
        this.policyLine = result.rule.lineNumber;
        this.serviceName = serviceName;
    }
}
