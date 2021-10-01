package org.bpfcaudit.bpfcaudit.model;

import com.toedter.spring.hateoas.jsonapi.JsonApiRelationships;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bpfcaudit.bpfcaudit.model.pojo.CaptureRO;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = Capture.CAPTURES)
public class Capture {
    public static final String CAPTURES = "captures";

    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private CaptureStatus status;
    private String startTime;
    private String endTime;
    private String completionMessage;
    @OneToOne(fetch = FetchType.EAGER)
    @JsonApiRelationships("services")
    @JoinColumn(name="service_id")
    private Service service;

    public Capture(CaptureRO captureRO, Service service) {
        // TODO: start time, check that start time < end time
        this.endTime = captureRO.getEndTime();
        this.status = CaptureStatus.IN_PROGRESS;
        this.service = service;
    }

    public void setFailure(String reason) {
        this.status = CaptureStatus.FAILED;
        this.completionMessage = reason;
    }

    // TODO: may need to override toString(), seems to cause stack overflow
}
