package org.bpfcaudit.bpfcaudit.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = Capture.CAPTURES)
@JsonIgnoreProperties
public class Capture {
    public static final String CAPTURES = "captures";

    @Id
    @GeneratedValue
    private Long id;
    private CaptureStatus status;
    private String startTime;
    @JsonProperty
    private String endTime;


    public Capture() {
        status = CaptureStatus.IN_PROGRESS;
    }
}
