package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.UpdateProductPort;
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
public class UpdateProductAdapter implements UpdateProductPort {

    private final ProductRepositoryPort repository;

    @Override
    public Product execute(final Context context) {
        final Product form = context.getData(Product.class);

        if (form == null) {
            throw new BusinessException("Por favor, informe os dados do produto.");
        }

        final Product product = repository.get(form.getId())
                .orElseThrow(() -> new BusinessException("Produto não encontrado."));

        if (form.getName() != null && !form.getName().isBlank()) {
            product.setName(form.getName().trim());
        }

        if (form.getPrice() != null) {
            product.setPrice(form.getPrice());
        }

        if (form.getCommissionPercentage() != null) {
            product.setCommissionPercentage(form.getCommissionPercentage());
        }

        product.setModifiedAt(LocalDateTime.now());

        return repository.save(product);
    }
}