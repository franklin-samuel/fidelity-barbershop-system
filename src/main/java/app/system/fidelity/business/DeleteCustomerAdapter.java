package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.DeleteCustomerPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteCustomerAdapter implements DeleteCustomerPort {

    private final CustomerRepositoryPort customerRepositoryPort;

    public Void execute(final Context context) {

        final UUID customerId = context.getProperty("id", UUID.class);
        final UUID authenticatedUserId = context.getProperty("authenticatedUserId", UUID.class);

        if (authenticatedUserId == null) {
            throw new BusinessException("Usuário não autenticado.");
        }

        final Customer customer = customerRepositoryPort.get(customerId)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado!"));


        customerRepositoryPort.delete(customerId);

        return null;

    }

}
