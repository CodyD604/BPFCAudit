package org.bpfcaudit.bpfcaudit.api;

import org.bpfcaudit.bpfcaudit.model.Service;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

@Component
public class ServiceModelAssembler {
    public EntityModel<Service> toModel(Service service) {
        return EntityModel.of(service);
    }
}
