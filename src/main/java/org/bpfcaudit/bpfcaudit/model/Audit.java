package org.bpfcaudit.bpfcaudit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiRelationships;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bpfcaudit.bpfcaudit.model.pojo.AuditRO;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = Audit.AUDITS)
public class Audit {
    public static final String AUDITS = "audits";

    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private AuditStatus status;
    private String startTime;
    private String endTime;
    private String completionMessage;
    @OneToOne(fetch = FetchType.EAGER)
    @JsonApiRelationships("services")
    @JoinColumn(name="service_id")
    private Service service;
    @OneToMany(mappedBy = "audit", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Rule> rules = new ArrayList<>();

    public Audit(AuditRO auditRO, Service service) throws Exception {
        Instant startTime = Instant.now();
        Instant endTime = Instant.parse(auditRO.getEndTime());

        if (endTime.isBefore(startTime)) {
            throw new Exception("Cannot perform audit, endTime " + endTime +
                    " occurs before current time " + startTime + ".");
        }

        this.startTime = startTime.toString();
        this.endTime = endTime.toString();
        this.status = AuditStatus.IN_PROGRESS;
        this.service = service;
    }

    public void complete(AuditStatus auditStatus, String reason) {
        this.status = auditStatus;
        this.completionMessage = reason;
    }

    // TODO: fixme, lombok impl causes stack overflow
    public String toString() {
        return "audit " + id;
    }
}
