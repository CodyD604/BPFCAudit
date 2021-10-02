package org.bpfcaudit.bpfcaudit.api.jsonapi;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import org.bpfcaudit.bpfcaudit.model.Audit;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;

@Component
public class AuditModelAssembler {
    public RepresentationModel<?> toJsonApiModel(Audit audit) {
        JsonApiModelBuilder builder = jsonApiModel()
                .model(audit);

        return builder.build();
    }
}
