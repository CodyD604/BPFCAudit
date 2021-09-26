package org.bpfcaudit.bpfcaudit.api;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import org.bpfcaudit.bpfcaudit.model.Service;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ServiceModelAssembler {
    public EntityModel<Service> toModel(Service service) {
        Link selfLink = linkTo(methodOn(ServiceController.class).findOne(service.getId())).withSelfRel();

        return EntityModel.of(service, selfLink);
    }

    public RepresentationModel<?> toJsonApiModel(Service service) {
        Link selfLink = linkTo(methodOn(ServiceController.class).findOne(service.getId())).withSelfRel();

        JsonApiModelBuilder builder = jsonApiModel()
                .model(service)
                .link(selfLink);

        return builder.build();
    }
}
