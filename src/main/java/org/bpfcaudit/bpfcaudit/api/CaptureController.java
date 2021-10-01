package org.bpfcaudit.bpfcaudit.api;

import org.bpfcaudit.bpfcaudit.api.jsonapi.CaptureModelAssembler;
import org.bpfcaudit.bpfcaudit.dal.CaptureRepository;
import org.bpfcaudit.bpfcaudit.dal.ServiceRepository;
import org.bpfcaudit.bpfcaudit.model.Capture;
import org.bpfcaudit.bpfcaudit.model.Service;
import org.bpfcaudit.bpfcaudit.model.pojo.CaptureRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.bpfcaudit.bpfcaudit.model.Capture.CAPTURES;

@RestController
@RequestMapping(value = ApiPath.API_BASE_PATH, produces = JSON_API_VALUE)
public class CaptureController {
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private CaptureRepository repository;
    @Autowired
    private CaptureModelAssembler captureAssembler;

    @PostMapping("/" + ApiPath.V1 + "/" + CAPTURES)
    public ResponseEntity<? extends RepresentationModel<?>> newCapture(
            @RequestBody EntityModel<CaptureRO> captureROModel
    ) {
        CaptureRO captureRO = captureROModel.getContent();
        assert captureRO != null;
        Optional<Service> service = serviceRepository.findById(captureRO.getServiceId());

        // TODO: error message, better error handling in general
        if (service.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // TODO: verify another capture is not IN_PROGRESS for this service

        Capture capture = new Capture(captureRO, service.get());
        repository.save(capture);

        final RepresentationModel<?> captureRepresentationModel = captureAssembler.toJsonApiModel(capture);

        return ResponseEntity.created(null).body(captureRepresentationModel);
    }
}
