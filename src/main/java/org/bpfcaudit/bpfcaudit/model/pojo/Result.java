package org.bpfcaudit.bpfcaudit.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigInteger;

@JsonIgnoreProperties(value = { "comm", "data" })
@EqualsAndHashCode
@ToString
public class Result {
    @JsonProperty("container")
    public BigInteger containerId;
    public String[] decision;
    public long nsPid;
    public long pid;
    public Rule rule;
}
