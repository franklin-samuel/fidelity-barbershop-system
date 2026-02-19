package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateServicePort;
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
public class CreateServiceAdapter implements CreateServicePort {

    private final ServiceRepositoryPort repository;

    @Override
    public Service execute(final Context context) {
        final Service form = context.getData(Service.class);

        if (form == null) {
            throw new BusinessException("Por favor, informe os dados do serviço.");
        }

        if (form.getName() == null || form.getName().isBlank()) {
            throw new BusinessException("Por favor, informe o nome do serviço.");
        }

        if (form.getPrice() == null) {
            throw new BusinessException("Por favor, informe o preço do serviço.");
        }

        if (form.getCommissionPercentage() == null) {
            throw new BusinessException("Por favor, informe a porcentagem de comissão do serviço.");
        }

        final Service newService = Service.builder()
                .name(form.getName().trim())
                .price(form.getPrice())
                .commissionPercentage(form.getCommissionPercentage())
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        return repository.save(newService);
    }
}