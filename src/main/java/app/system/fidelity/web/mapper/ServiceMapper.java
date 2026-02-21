package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.Service;
import app.system.fidelity.web.model.response.ServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceResponse mapToResponse(final Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .price(service.getPrice())
                .commissionPercentage(service.getCommissionPercentage())
                .createdAt(service.getCreatedAt())
                .modifiedAt(service.getModifiedAt())
                .build();
    }
}