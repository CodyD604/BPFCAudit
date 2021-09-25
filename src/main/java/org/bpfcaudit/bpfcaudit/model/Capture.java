package org.bpfcaudit.bpfcaudit.model;

import javax.persistence.*;

//@Entity
//@Table(name = "captures")
public class Capture {
    @Id
    @GeneratedValue
    private long id;
    public CaptureStatus status;

    public Capture() {
        status = CaptureStatus.IN_PROGRESS;
    }
}
