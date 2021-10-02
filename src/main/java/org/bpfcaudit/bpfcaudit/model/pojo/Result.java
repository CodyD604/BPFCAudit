package org.bpfcaudit.bpfcaudit.model.pojo;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Result {
    public String[] decision;
    public Rule rule;
}
