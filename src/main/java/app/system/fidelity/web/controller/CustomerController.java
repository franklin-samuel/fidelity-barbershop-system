package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateCustomerPort;
import app.system.fidelity.core.business.UpdateCustomerPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.Customer;

import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.CustomerMapper;
import app.system.fidelity.web.model.request.CustomerRequest;
import app.system.fidelity.web.model.request.CustomerUpdateRequest;
import app.system.fidelity.web.model.response.CustomerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepositoryPort repository;
    private final CreateCustomerPort createCustomerPort;
    private final UpdateCustomerPort  updateCustomerPort;
    private final CustomerMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody final CustomerRequest request
    ) {

        final Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();

        final Context context = new Context(customer);
        final Customer savedCustomer = createCustomerPort.execute(context);

        final CustomerResponse response = mapper.mapToResponse(savedCustomer);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cliente criado com sucesso"));


    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> list() {

        final List<Customer> customers = repository.findAll();

        final List<CustomerResponse> responses = customers.stream()
                .map(mapper::mapToResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> search(
            @RequestParam("q") final String searchTerm
    ) {

        final List<Customer> customers = repository.findByNameOrEmailOrPhoneNumber(searchTerm);

        final List<CustomerResponse> responses = customers.stream()
                .map(mapper::mapToResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable final UUID id,
            @Valid @RequestBody final CustomerUpdateRequest request
    ) {

        final Customer customerForm = Customer.builder()
                .id(id)
                .name(request.name())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .build();

        final Context context = new Context(customerForm);

        final Customer updatedCustomer = updateCustomerPort.execute(context);

        final CustomerResponse response = mapper.mapToResponse(updatedCustomer);

        return ResponseEntity.ok(ApiResponse.success(response, "Cliente atualizado com sucesso"));

    }

}
