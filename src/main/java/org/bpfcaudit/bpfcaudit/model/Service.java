package org.bpfcaudit.bpfcaudit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@NoArgsConstructor
@Table(name = Service.SERVICES)
public class Service {
    public static final String SERVICES = "services";

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    private String name;

    public Service(String name) {
        this.name = name;
    }
}
