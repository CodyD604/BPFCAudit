package org.bpfcaudit.bpfcaudit.api.jsonapi;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import org.bpfcaudit.bpfcaudit.model.Capture;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;

@Component
public class CaptureModelAssembler {
    public RepresentationModel<?> toJsonApiModel(Capture capture) {
        JsonApiModelBuilder builder = jsonApiModel()
                .model(capture);

        return builder.build();
    }
}
