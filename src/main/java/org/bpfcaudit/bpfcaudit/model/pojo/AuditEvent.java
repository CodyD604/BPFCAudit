package org.bpfcaudit.bpfcaudit.model.pojo;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class AuditEvent {
    public Result result;
    public long subscription;
}
