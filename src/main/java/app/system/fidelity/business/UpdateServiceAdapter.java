package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.UpdateServicePort;
import app.system.fidelity.core.persistence.ServiceRepositoryPort;
import app.system.fidelity.domain.Service;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Transactional
@AllArgsConstructor
public class UpdateServiceAdapter implements UpdateServicePort {

    private final ServiceRepositoryPort repository;

    @Override
    public Service execute(final Context context) {
        final Service form = context.getData(Service.class);

        if (form == null) {
            throw new BusinessException("Por favor, informe os dados do serviço.");
        }

        final Service service = repository.get(form.getId())
                .orElseThrow(() -> new BusinessException("Serviço não encontrado."));

        if (form.getName() != null && !form.getName().isBlank()) {
            service.setName(form.getName().trim());
        }

        if (form.getPrice() != null) {
            service.setPrice(form.getPrice());
        }

        if (form.getCommissionPercentage() != null) {
            service.setCommissionPercentage(form.getCommissionPercentage());
        }

        service.setModifiedAt(LocalDateTime.now());

        return repository.save(service);
    }
}