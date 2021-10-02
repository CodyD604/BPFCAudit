package org.bpfcaudit.bpfcaudit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiRelationships;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

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
    @OneToMany(mappedBy = "service", fetch = FetchType.EAGER)
    @JsonIgnore
    @JsonApiRelationships("audits")
    private List<Audit> audits = new ArrayList<>();
    // TODO: add policy name/id
}
