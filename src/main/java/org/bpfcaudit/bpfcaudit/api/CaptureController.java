package org.bpfcaudit.bpfcaudit.api;

import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import org.bpfcaudit.bpfcaudit.api.jsonapi.CaptureModelAssembler;
import org.bpfcaudit.bpfcaudit.api.jsonapi.JSONAPIException;
import org.bpfcaudit.bpfcaudit.dal.CaptureRepository;
import org.bpfcaudit.bpfcaudit.dal.ServiceRepository;
import org.bpfcaudit.bpfcaudit.model.Capture;
import org.bpfcaudit.bpfcaudit.model.Service;
import org.bpfcaudit.bpfcaudit.model.pojo.CaptureRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.bpfcaudit.bpfcaudit.model.Capture.CAPTURES;

@RestController
@RequestMapping(value = ApiPath.API_BASE_PATH, produces = JSON_API_VALUE)
public class CaptureController {
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private CaptureRepository captureRepository;
    @Autowired
    private CaptureModelAssembler captureAssembler;

    @PostMapping("/" + ApiPath.V1 + "/" + CAPTURES)
    public ResponseEntity<?> newCapture(
            @RequestBody EntityModel<CaptureRO> captureROModel
    ) throws JSONAPIException {
        CaptureRO captureRO = captureROModel.getContent();
        assert captureRO != null;
        Optional<Service> service = serviceRepository.findById(captureRO.getServiceId());

        if (service.isEmpty()) {
            throw new JSONAPIException(HttpStatus.BAD_REQUEST, "No service found with id " + captureRO.getServiceId());
        }

        // TODO: verify another capture is not IN_PROGRESS for this service

        Capture capture;
        try {
            capture = new Capture(captureRO, service.get());
        } catch (Exception ex) {
            throw new JSONAPIException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        captureRepository.save(capture);

        final RepresentationModel<?> captureRepresentationModel = captureAssembler.toJsonApiModel(capture);

        return ResponseEntity.created(null).body(captureRepresentationModel);
    }
}
