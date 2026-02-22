package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetAnalyticsDataPort;
import app.system.fidelity.domain.AnalyticsData;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.AnalyticsMapper;
import app.system.fidelity.web.model.response.AnalyticsDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final GetAnalyticsDataPort getAnalyticsDataPort;
    private final AnalyticsMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<AnalyticsDataResponse>> getAnalytics() {
        final Context context = new Context();
        final AnalyticsData data = getAnalyticsDataPort.execute(context);

        final AnalyticsDataResponse response = mapper.mapToResponse(data);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}