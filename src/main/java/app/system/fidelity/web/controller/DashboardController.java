package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetDashboardMetricsPort;
import app.system.fidelity.domain.DashboardMetrics;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.DashboardMapper;
import app.system.fidelity.web.model.response.DashboardMetricsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final GetDashboardMetricsPort getDashboardMetricsPort;
    private final DashboardMapper mapper;

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getMetrics() {

        final Context context = new Context();
        final DashboardMetrics metrics = getDashboardMetricsPort.execute(context);

        final DashboardMetricsResponse response = mapper.mapToResponse(metrics);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}