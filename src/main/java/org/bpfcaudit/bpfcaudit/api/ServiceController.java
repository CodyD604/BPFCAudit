package org.bpfcaudit.bpfcaudit.api;

import org.bpfcaudit.bpfcaudit.dal.ServiceRepository;
import org.bpfcaudit.bpfcaudit.model.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;

@RestController
@RequestMapping(value = RootController.API_BASE_PATH, produces = JSON_API_VALUE)
public class ServiceController {
    private static final String SERVICES = "services";

    @Autowired
    private ServiceRepository repository;
    @Autowired
    private ServiceModelAssembler serviceAssembler;

    @GetMapping("/" + RootController.V1 + "/" + SERVICES + "/{id}")
    public ResponseEntity<EntityModel<Service>> findOne(
            @PathVariable Long id
    ) {
        return repository.findById(id)
                .map(serviceAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/" + RootController.V1 + "/" + SERVICES)
    public ResponseEntity<EntityModel<Service>> newService(@RequestBody EntityModel<Service> serviceModel) {
        Service service = serviceModel.getContent();
        assert service != null;

        repository.save(service);

        
        return "stuff";
    }
}
