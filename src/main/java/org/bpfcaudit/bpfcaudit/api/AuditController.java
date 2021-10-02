package org.bpfcaudit.bpfcaudit.api;

import org.bpfcaudit.bpfcaudit.api.jsonapi.AuditModelAssembler;
import org.bpfcaudit.bpfcaudit.api.jsonapi.JSONAPIException;
import org.bpfcaudit.bpfcaudit.auditor.AuditScheduler;
import org.bpfcaudit.bpfcaudit.dal.AuditRepository;
import org.bpfcaudit.bpfcaudit.dal.ServiceRepository;
import org.bpfcaudit.bpfcaudit.model.Audit;
import org.bpfcaudit.bpfcaudit.model.Service;
import org.bpfcaudit.bpfcaudit.model.pojo.AuditRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.bpfcaudit.bpfcaudit.model.Audit.AUDITS;

@RestController
@RequestMapping(value = ApiPath.API_BASE_PATH, produces = JSON_API_VALUE)
public class AuditController {
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private AuditModelAssembler auditAssembler;
    @Autowired
    private AuditScheduler auditScheduler;

    @PostMapping("/" + ApiPath.V1 + "/" + AUDITS)
    public ResponseEntity<?> newAudit(
            @RequestBody EntityModel<AuditRO> auditROModel
    ) throws JSONAPIException {
        AuditRO auditRO = auditROModel.getContent();
        assert auditRO != null;
        Optional<Service> service = serviceRepository.findById(auditRO.getServiceId());

        if (service.isEmpty()) {
            throw new JSONAPIException(HttpStatus.BAD_REQUEST, "No service found with id " + auditRO.getServiceId());
        }

        Audit audit;
        try {
            audit = new Audit(auditRO, service.get());
        } catch (Exception ex) {
            throw new JSONAPIException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        auditRepository.save(audit);

        try {
            auditScheduler.InitiateAudit(audit);
        } catch (Exception ex) {
            // TODO: asnyc
            auditRepository.delete(audit);
            throw new JSONAPIException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        final RepresentationModel<?> auditRepresentationModel = auditAssembler.toJsonApiModel(audit);

        return ResponseEntity.created(null).body(auditRepresentationModel);
    }
}
