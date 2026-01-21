package app.system.fidelity.web.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record DashboardMetricsResponse(
        @JsonProperty("total_customers")
        long totalCustomers,

        @JsonProperty("total_haircuts")
        long totalHaircuts,

        @JsonProperty("free_haircuts_given")
        long freeHaircutsGiven,

        @JsonProperty("customers_ready_for_free_haircut")
        long customersReadyForFreeHaircut
) {
}