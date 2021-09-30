package org.bpfcaudit.bpfcaudit.api;

import org.bpfcaudit.bpfcaudit.dal.CaptureRepository;
import org.bpfcaudit.bpfcaudit.model.Capture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.bpfcaudit.bpfcaudit.model.Capture.CAPTURES;

@RestController
@RequestMapping(value = RootController.API_BASE_PATH, produces = JSON_API_VALUE)
public class CaptureController {
    @Autowired
    private CaptureRepository repository;
    @Autowired
    private CaptureModelAssembler captureAssembler;

    @GetMapping("/" + RootController.V1 + "/" + CAPTURES + "/{id}")
    public ResponseEntity<EntityModel<Capture>> findOne(
            @PathVariable Long id
    ) {
        return repository.findById(id)
                .map(captureAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/" + RootController.V1 + "/" + CAPTURES)
    public ResponseEntity<EntityModel<Capture>> newCapture(@RequestBody EntityModel<Capture> captureModel) {
        Capture capture = captureModel.getContent();
        assert capture != null;
        repository.save(capture);

        final RepresentationModel<?> captureRepresentationModel = captureAssembler.toJsonApiModel(capture);

        return captureRepresentationModel
                .getLink(IanaLinkRelations.SELF)
                .map(Link::getHref)
                .map(href -> {
                    try {
                        return new URI(href);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(uri -> ResponseEntity.created(uri).body(captureAssembler.toModel(capture)))
                .orElse(ResponseEntity.badRequest().build());
    }
}
