package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.Product;
import app.system.fidelity.web.model.response.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse mapToResponse(final Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .commissionPercentage(product.getCommissionPercentage())
                .createdAt(product.getCreatedAt())
                .modifiedAt(product.getModifiedAt())
                .build();
    }
}