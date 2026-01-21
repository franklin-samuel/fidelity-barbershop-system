package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.UpdateCustomerPort;
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
public class UpdateCustomerAdapter implements UpdateCustomerPort {

    private final CustomerRepositoryPort repository;

    @Override
    public Customer execute(final Context context) {

        Customer customerForm = context.getData(Customer.class);

        if (customerForm == null) {
            throw new BusinessException("Por favor, insira os dados do cliente");
        }

        Customer customer = repository.get(customerForm.getId())
                .orElseThrow(() -> new BusinessException("Cliente não encontrado"));

        customer.setName(customerForm.getName().trim());
        customer.setEmail(customerForm.getEmail().trim());
        customer.setPhoneNumber(customerForm.getPhoneNumber().trim());

        customer.setUpdatedAt(LocalDateTime.now());

        return repository.save(customer);
    }

}
