package org.bpfcaudit.bpfcaudit.model.pojo;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Audit {
    public Result result;
    public long subscription;
}
