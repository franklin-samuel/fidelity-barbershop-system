package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.DeleteServicePort;
import app.system.fidelity.core.persistence.ServiceRepositoryPort;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional
@AllArgsConstructor
public class DeleteServiceAdapter implements DeleteServicePort {

    private final ServiceRepositoryPort repository;

    @Override
    public Void execute(final Context context) {
        final UUID serviceId = context.getProperty("serviceId", UUID.class);

        if (serviceId == null) {
            throw new BusinessException("Serviço não encontrado.");
        }

        repository.get(serviceId)
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        repository.delete(serviceId);

        return null;
    }
}