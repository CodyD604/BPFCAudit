package org.bpfcaudit.bpfcaudit.api;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import org.bpfcaudit.bpfcaudit.api.jsonapi.ServiceModelAssembler;
import org.bpfcaudit.bpfcaudit.dal.ServiceRepository;
import org.bpfcaudit.bpfcaudit.model.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.bpfcaudit.bpfcaudit.model.Service.SERVICES;
import static org.bpfcaudit.bpfcaudit.model.Capture.CAPTURES;

@RestController
@RequestMapping(value = ApiPath.API_BASE_PATH, produces = JSON_API_VALUE)
public class ServiceController {
    @Autowired
    private ServiceRepository repository;
    @Autowired
    private ServiceModelAssembler serviceAssembler;

    // TODO: includes for policy
    @GetMapping("/" + ApiPath.V1 + "/" + SERVICES)
    public ResponseEntity<RepresentationModel<?>> findAll(
        @RequestParam(value = "page[number]", defaultValue = "0", required = false) int page,
        @RequestParam(value = "page[size]", defaultValue = "25", required = false) int size
    ) {
        final PageRequest pageRequest = PageRequest.of(page, size);

        final Page<Service> pagedResult = repository.findAll(pageRequest);

        List<? extends RepresentationModel<?>> serviceResources =
                StreamSupport.stream(pagedResult.spliterator(),false)
                .map(service -> serviceAssembler.toJsonApiModel(service))
                .collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(
                        pagedResult.getSize(),
                        pagedResult.getNumber(),
                        pagedResult.getTotalElements(),
                        pagedResult.getTotalPages());

        final PagedModel<? extends RepresentationModel<?>> pagedModel =
                PagedModel.of(serviceResources, pageMetadata);

        final JsonApiModelBuilder jsonApiModelBuilder = jsonApiModel().model(pagedModel);

        for (Service service : pagedResult.getContent()) {
            jsonApiModelBuilder.included(service.getCaptures());
        }

        final RepresentationModel<?> jsonApiModel = jsonApiModelBuilder.build();

        return ResponseEntity.ok(jsonApiModel);
    }

    // TODO: includes for policy
    @GetMapping("/" + ApiPath.V1 + "/" + SERVICES + "/{id}")
    public ResponseEntity<? extends  RepresentationModel<?>> findOne(
            @PathVariable Long id,
            @RequestParam(value = "included", required = false) String[] included
    ) {
        Optional<Service> serviceWrapped = repository.findById(id);
        if (serviceWrapped.isEmpty()) return ResponseEntity.notFound().build();

        Service service = serviceWrapped.get();
        RepresentationModel<?> serviceModel = serviceAssembler.toJsonApiModel(serviceWrapped.get());

        final JsonApiModelBuilder jsonApiModelBuilder = jsonApiModel().model(serviceModel);

        if (included != null) {
            List<String> includedList = Arrays.asList(included);
            if (includedList.contains(CAPTURES)) {
                jsonApiModelBuilder.included(service.getCaptures());
            }
        }

        return ResponseEntity.ok(jsonApiModelBuilder.build());
    }

    @PostMapping("/" + ApiPath.V1 + "/" + SERVICES)
    public ResponseEntity<? extends RepresentationModel<?>> newService(@RequestBody EntityModel<Service> serviceModel) {
        Service service = serviceModel.getContent();
        assert service != null;
        repository.save(service);

        return ResponseEntity.created(null).body(serviceAssembler.toJsonApiModel(service));
    }
}
