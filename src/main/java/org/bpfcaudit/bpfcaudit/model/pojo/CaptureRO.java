package org.bpfcaudit.bpfcaudit.model.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CaptureRO {
    private Long id;
    @JsonProperty(required = true)
    private String endTime;
    @JsonProperty(required = true)
    private Long serviceId;
}
