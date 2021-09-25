package org.bpfcaudit.bpfcaudit.model.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

public class Rule {
    @JsonProperty("file")
    public String ruleName;
    public int lineNumber;
    @JsonProperty("policy")
    public BigInteger policyId;
}
