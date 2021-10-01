package org.bpfcaudit.bpfcaudit.api.jsonapi;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import org.bpfcaudit.bpfcaudit.model.Service;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.bpfcaudit.bpfcaudit.model.Capture.CAPTURES;

@Component
public class ServiceModelAssembler {
    public RepresentationModel<?> toJsonApiModel(Service service) {
        JsonApiModelBuilder builder = jsonApiModel()
                .model(service)
                .relationship(CAPTURES, service.getCaptures());

        return builder.build();
    }
}
