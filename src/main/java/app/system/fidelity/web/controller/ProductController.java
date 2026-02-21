package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateProductPort;
import app.system.fidelity.core.business.DeleteProductPort;
import app.system.fidelity.core.business.UpdateProductPort;
import app.system.fidelity.core.persistence.ProductRepositoryPort;
import app.system.fidelity.domain.Product;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.ProductMapper;
import app.system.fidelity.web.model.request.ProductRequest;
import app.system.fidelity.web.model.request.ProductUpdateRequest;
import app.system.fidelity.web.model.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepositoryPort repository;
    private final CreateProductPort createProductPort;
    private final UpdateProductPort updateProductPort;
    private final DeleteProductPort deleteProductPort;
    private final ProductMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> list() {
        final List<ProductResponse> responses = repository.findAll()
                .stream()
                .map(mapper::mapToResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody final ProductRequest request
    ) {
        final Product form = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .commissionPercentage(request.getCommissionPercentage())
                .build();

        final Context context = new Context(form);
        final Product savedProduct = createProductPort.execute(context);

        final ProductResponse response = mapper.mapToResponse(savedProduct);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Produto criado com sucesso"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable final UUID id,
            @Valid @RequestBody final ProductUpdateRequest request
    ) {
        final Product form = Product.builder()
                .id(id)
                .name(request.name())
                .price(request.price())
                .commissionPercentage(request.commissionPercentage())
                .build();

        final Context context = new Context(form);
        final Product updatedProduct = updateProductPort.execute(context);

        final ProductResponse response = mapper.mapToResponse(updatedProduct);

        return ResponseEntity.ok(ApiResponse.success(response, "Produto atualizado com sucesso"));
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final UUID id) {
        final Context context = new Context();
        context.putProperty("productId", id);

        deleteProductPort.execute(context);

        return ResponseEntity.ok(ApiResponse.success("Produto deletado com sucesso"));
    }
}