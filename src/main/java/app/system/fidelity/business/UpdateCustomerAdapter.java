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

        if (customerForm.getName() != null && !customerForm.getName().isBlank()) {
            customer.setName(customerForm.getName().trim());
        }

        if (customerForm.getEmail() != null && !customerForm.getEmail().isBlank()) {
            customer.setEmail(customerForm.getEmail().trim());
        }

        if (customerForm.getPhoneNumber() != null && !customerForm.getPhoneNumber().isBlank()) {
            customer.setPhoneNumber(customerForm.getPhoneNumber().trim());
        }

        final boolean hasEmail = customer.getEmail() != null && !customer.getEmail().isBlank();
        final boolean hasPhone = customer.getPhoneNumber() != null && !customer.getPhoneNumber().isBlank();

        if (!hasEmail && !hasPhone) {
            throw new BusinessException("O cliente deve ter pelo menos email ou telefone cadastrado.");
        }

        customer.setModifiedAt(LocalDateTime.now());

        return repository.save(customer);
    }

}