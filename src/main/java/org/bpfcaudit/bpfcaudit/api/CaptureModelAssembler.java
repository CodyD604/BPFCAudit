package org.bpfcaudit.bpfcaudit.api;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import org.bpfcaudit.bpfcaudit.model.Capture;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CaptureModelAssembler {
    public EntityModel<Capture> toModel(Capture capture) {
        Link selfLink = linkTo(methodOn(CaptureController.class).findOne(capture.getId())).withSelfRel();

        return EntityModel.of(capture, selfLink);
    }

    public RepresentationModel<?> toJsonApiModel(Capture capture) {
        Link selfLink = linkTo(methodOn(CaptureController.class).findOne(capture.getId())).withSelfRel();

        JsonApiModelBuilder builder = jsonApiModel()
                .model(capture)
                .link(selfLink);

        return builder.build();
    }
}
