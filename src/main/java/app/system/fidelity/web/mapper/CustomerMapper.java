package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.Customer;
import app.system.fidelity.web.model.response.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse mapToResponse(final Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .haircutCount(customer.getHaircutCount())
                .freeHaircutsClaimed(customer.getFreeHaircutsClaimed())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();

    }
}
