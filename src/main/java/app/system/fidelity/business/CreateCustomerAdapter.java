package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateCustomerPort;
import app.system.fidelity.core.messaging.SendWelcomeEmailPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.exceptions.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@AllArgsConstructor
public class CreateCustomerAdapter implements CreateCustomerPort {

    private final CustomerRepositoryPort repository;
    private final SendWelcomeEmailPort sendWelcomeEmailPort;

    @Override
    public Customer execute(final Context context) {
        final Customer customer = context.getData(Customer.class);

        if (customer == null) {
            throw new BusinessException("Por favor, insira os dados do cliente.");
        }
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new BusinessException("Por favor, insira o nome do cliente.");
        }
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new BusinessException("Por favor, insira o email do cliente.");
        }
        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().isBlank()) {
            throw new BusinessException("Por favor, insira o telefone do cliente.");
        }

        if(repository.existsByEmail(customer.getEmail())) {
            throw new BusinessException("Já existe um cliente com esse email.");
        }

        final Customer savedCustomer = repository.save(Customer.builder()
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .serviceCount(0)
                .discountsClaimed(0)

                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())

                .referralSource(customer.getReferralSource())
                .preferredFrequency(customer.getPreferredFrequency())
                .preferredStyle(customer.getPreferredStyle())
                .preferredBarberId(customer.getPreferredBarberId())

                .instagramUsername(customer.getInstagramUsername())

                .occupation(customer.getOccupation())

                .lastVisitDate(null)
                .totalSpent(BigDecimal.ZERO)

                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        try {
            final Context emailContext = new Context();
            emailContext.putProperty("customerEmail", savedCustomer.getEmail());
            emailContext.putProperty("customerName", savedCustomer.getName());
            sendWelcomeEmailPort.execute(emailContext);
        } catch (Exception e) {
            // Apenas loga
        }

        return savedCustomer;
    }
}