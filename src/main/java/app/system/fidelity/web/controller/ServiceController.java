package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateServicePort;
import app.system.fidelity.core.business.DeleteServicePort;
import app.system.fidelity.core.business.UpdateServicePort;
import app.system.fidelity.core.persistence.ServiceRepositoryPort;
import app.system.fidelity.domain.Service;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.ServiceMapper;
import app.system.fidelity.web.model.request.ServiceRequest;
import app.system.fidelity.web.model.request.ServiceUpdateRequest;
import app.system.fidelity.web.model.response.ServiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/service")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepositoryPort repository;
    private final CreateServicePort createServicePort;
    private final UpdateServicePort updateServicePort;
    private final DeleteServicePort deleteServicePort;
    private final ServiceMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> list() {
        final List<ServiceResponse> responses = repository.findAll()
                .stream()
                .map(mapper::mapToResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceResponse>> create(
            @Valid @RequestBody final ServiceRequest request
    ) {
        final Service form = Service.builder()
                .name(request.getName())
                .price(request.getPrice())
                .commissionPercentage(request.getCommissionPercentage())
                .build();

        final Context context = new Context(form);
        final Service savedService = createServicePort.execute(context);

        final ServiceResponse response = mapper.mapToResponse(savedService);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Serviço criado com sucesso"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> update(
            @PathVariable final UUID id,
            @Valid @RequestBody final ServiceUpdateRequest request
    ) {
        final Service form = Service.builder()
                .id(id)
                .name(request.name())
                .price(request.price())
                .commissionPercentage(request.commissionPercentage())
                .build();

        final Context context = new Context(form);
        final Service updatedService = updateServicePort.execute(context);

        final ServiceResponse response = mapper.mapToResponse(updatedService);

        return ResponseEntity.ok(ApiResponse.success(response, "Serviço atualizado com sucesso"));
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final UUID id) {
        final Context context = new Context();
        context.putProperty("serviceId", id);

        deleteServicePort.execute(context);

        return ResponseEntity.ok(ApiResponse.success("Serviço deletado com sucesso"));
    }
}