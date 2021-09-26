package org.bpfcaudit.bpfcaudit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@NoArgsConstructor
public class Service {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    private String name;

    public Service(String name) {
        this.name = name;
    }
}
