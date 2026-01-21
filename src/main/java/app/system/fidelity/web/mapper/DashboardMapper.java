package app.system.fidelity.web.mapper;

import app.system.fidelity.domain.DashboardMetrics;
import app.system.fidelity.web.model.response.DashboardMetricsResponse;
import org.springframework.stereotype.Component;

@Component
public class DashboardMapper {

    public DashboardMetricsResponse mapToResponse(final DashboardMetrics metrics) {
        return DashboardMetricsResponse.builder()
                .totalCustomers(metrics.getTotalCustomers())
                .totalHaircuts(metrics.getTotalHaircuts())
                .freeHaircutsGiven(metrics.getFreeHaircutsGiven())
                .customersReadyForFreeHaircut(metrics.getCustomersReadyForFreeHaircut())
                .build();
    }
}