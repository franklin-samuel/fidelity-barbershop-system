package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateProductPort;
import app.system.fidelity.core.persistence.ProductRepositoryPort;
import app.system.fidelity.domain.Product;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Transactional
@AllArgsConstructor
public class CreateProductAdapter implements CreateProductPort {

    private final ProductRepositoryPort repository;

    @Override
    public Product execute(final Context context) {
        final Product form = context.getData(Product.class);

        if (form == null) {
            throw new BusinessException("Por favor, informe os dados do produto.");
        }

        if (form.getName() == null || form.getName().isBlank()) {
            throw new BusinessException("Por favor, informe o nome do produto.");
        }

        if (form.getPrice() == null) {
            throw new BusinessException("Por favor, informe o preço do produto.");
        }

        if (form.getCommissionPercentage() == null) {
            throw new BusinessException("Por favor, informe a porcentagem de comissão do produto.");
        }

        final Product newProduct = Product.builder()
                .name(form.getName().trim())
                .price(form.getPrice())
                .commissionPercentage(form.getCommissionPercentage())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        return repository.save(newProduct);
    }
}