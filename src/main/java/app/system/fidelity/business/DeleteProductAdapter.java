package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.DeleteProductPort;
import app.system.fidelity.core.persistence.ProductRepositoryPort;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional
@AllArgsConstructor
public class DeleteProductAdapter implements DeleteProductPort {

    private final ProductRepositoryPort repository;

    @Override
    public Void execute(final Context context) {
        final UUID productId = context.getProperty("productId", UUID.class);

        if (productId == null) {
            throw new BusinessException("Produto não encontrado.");
        }

        repository.get(productId)
                .orElseThrow(() -> new BusinessException("Produto não encontrado."));

        repository.delete(productId);

        return null;
    }
}