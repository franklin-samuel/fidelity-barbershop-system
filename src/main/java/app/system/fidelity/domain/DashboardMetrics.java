package app.system.fidelity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetrics {

    private long totalCustomers;
    private long totalHaircuts;
    private long freeHaircutsGiven;
    private long customersReadyForFreeHaircut;

}
