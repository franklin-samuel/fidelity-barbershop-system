package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.RegisterHaircutPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.core.persistence.HaircutRepositoryPort;
import app.system.fidelity.core.persistence.SettingsRepositoryPort;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.Haircut;
import app.system.fidelity.domain.Settings;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class RegisterHaircutAdapter implements RegisterHaircutPort {

    private final CustomerRepositoryPort customerRepository;
    private final HaircutRepositoryPort haircutRepository;
    private final SettingsRepositoryPort settingsRepository;

    @Override
    public Customer execute(final Context context) {

        final UUID customerId = context.getProperty("customerId", UUID.class);
        final UUID registeredBy = context.getProperty("registeredBy", UUID.class);

        if (customerId == null) {
            throw new BusinessException("Por favor, informe o cliente");
        }

        if (registeredBy == null) {
            throw new BusinessException("Usuário não autenticado");
        }

        Customer customer = customerRepository.get(customerId)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado"));

        Settings settings = settingsRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Configurações do sistema não encontradas"));

        boolean isFree = customer.getHaircutCount() >= settings.getHaircutsForFree();

        Haircut haircut = Haircut.builder()
                .customerId(customerId)
                .registeredBy(registeredBy)
                .isFree(isFree)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        haircutRepository.save(haircut);

        if (isFree) {
            customer.setHaircutCount(0);
            customer.setFreeHaircutsClaimed(customer.getFreeHaircutsClaimed() + 1);
        } else {
            customer.setHaircutCount(customer.getHaircutCount() + 1);
        }

        customer.setUpdatedAt(LocalDateTime.now());

        return customerRepository.save(customer);
    }
}