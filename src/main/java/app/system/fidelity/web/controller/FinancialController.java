package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetDailyCashClosingPort;
import app.system.fidelity.domain.DailyCashClosing;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.DailyCashClosingMapper;
import app.system.fidelity.web.model.response.DailyCashClosingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financial")
@RequiredArgsConstructor
public class FinancialController {

    private final GetDailyCashClosingPort getDailyCashClosingPort;
    private final DailyCashClosingMapper mapper;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<DailyCashClosingResponse>> getDailyCashClosing() {
        final Context context = new Context();
        final DailyCashClosing cashClosing = getDailyCashClosingPort.execute(context);

        final DailyCashClosingResponse response = mapper.mapToResponse(cashClosing);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}