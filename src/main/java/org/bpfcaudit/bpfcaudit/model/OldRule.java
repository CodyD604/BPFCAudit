package org.bpfcaudit.bpfcaudit.model;
import org.bpfcaudit.bpfcaudit.model.pojo.Result;
import javax.persistence.*;

@Entity
@Table(name = "oldRule")
public class OldRule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;
    public String[] decision;
    public String serviceName;
    public String ruleName;

    public OldRule(Result result, String lineNumber, String serviceName) {
        this.decision = result.decision;
        this.serviceName = serviceName;
    }
}