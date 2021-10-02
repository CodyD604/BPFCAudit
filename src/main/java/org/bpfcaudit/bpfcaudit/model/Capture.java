package org.bpfcaudit.bpfcaudit.model;

import com.toedter.spring.hateoas.jsonapi.JsonApiRelationships;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bpfcaudit.bpfcaudit.model.pojo.CaptureRO;

import javax.persistence.*;
import java.time.Instant;

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

    public Capture(CaptureRO captureRO, Service service) throws Exception {
        Instant startTime = Instant.now();
        Instant endTime = Instant.parse(captureRO.getEndTime());

        if (endTime.isBefore(startTime)) {
            throw new Exception("Cannot create capture, endTime " + endTime +
                    " occurs before current time " + startTime + ".");
        }

        this.startTime = startTime.toString();
        this.endTime = endTime.toString();
        this.status = CaptureStatus.IN_PROGRESS;
        this.service = service;
    }

    public void setFailure(String reason) {
        this.status = CaptureStatus.FAILED;
        this.completionMessage = reason;
    }

    // TODO: may need to override toString(), seems to cause stack overflow
}
