package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.RegisterHaircutPort;
import app.system.fidelity.security.model.CustomUserDetails;
import app.system.fidelity.web.commons.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/haircut")
@RequiredArgsConstructor
public class HaircutController {

    private final RegisterHaircutPort registerHaircutPort;

    @PostMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Void>> register(
            @PathVariable final UUID customerId,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {

        final Context context = new Context();
        context.putProperty("customerId", customerId);
        context.putProperty("registeredBy", userDetails.getUserId());

        registerHaircutPort.execute(context);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Corte registrado com sucesso"));
    }
}