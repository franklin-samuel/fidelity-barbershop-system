package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateCustomerPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@AllArgsConstructor
public class CreateCustomerAdapter implements CreateCustomerPort {

    private final CustomerRepositoryPort repository;

    @Override
    public Customer execute(final Context context) {
        final Customer customer = context.getData(Customer.class);

        if (customer == null) {
            throw new BusinessException("Por favor, insira os dados do cliente!");
        }
        if (customer.getName() == null) {
            throw new BusinessException("Por favor, insira o nome do cliente.");
        }
        if (customer.getEmail() == null) {
            throw new BusinessException("Por favor, insira o email do cliente.");
        }
        if (customer.getPhoneNumber() == null) {
            throw new BusinessException("Por favor, insira o telefone do cliente.");
        }

        return repository.save(Customer.builder()
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .serviceCount(0)
                .discountsClaimed(0)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }
}