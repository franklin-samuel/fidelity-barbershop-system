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
                .serviceCount(customer.getServiceCount())
                .discountsClaimed(customer.getDiscountsClaimed())
                .createdAt(customer.getCreatedAt())
                .modifiedAt(customer.getModifiedAt())
                .build();
    }
}