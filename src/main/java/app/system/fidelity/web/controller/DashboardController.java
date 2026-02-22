package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetAdminDashboardMetricsPort;
import app.system.fidelity.core.business.GetBarberDashboardMetricsPort;
import app.system.fidelity.domain.DashboardMetrics;
import app.system.fidelity.domain.enums.Role;
import app.system.fidelity.security.model.CustomUserDetails;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.DashboardMapper;
import app.system.fidelity.web.model.response.DashboardMetricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final GetAdminDashboardMetricsPort getAdminDashboardMetricsPort;
    private final GetBarberDashboardMetricsPort getBarberDashboardMetricsPort;
    private final DashboardMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getMetrics(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        final boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.name()));

        final Context context = new Context();
        context.putProperty("userId", userDetails.getUserId());
        context.putProperty("userRole", isAdmin ? Role.ADMIN : Role.BARBER);

        final DashboardMetrics metrics;

        if (isAdmin) {
            metrics = getAdminDashboardMetricsPort.execute(context);
        } else {
            metrics = getBarberDashboardMetricsPort.execute(context);
        }

        final DashboardMetricsResponse response = mapper.mapToResponse(metrics, isAdmin);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}