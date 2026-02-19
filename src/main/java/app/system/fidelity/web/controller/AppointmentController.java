package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.RegisterAppointmentPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.security.model.CustomUserDetails;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.AppointmentMapper;
import app.system.fidelity.web.model.request.AppointmentRequest;
import app.system.fidelity.web.model.response.AppointmentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final RegisterAppointmentPort registerAppointmentPort;
    private final AppointmentMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> register(
            @Valid @RequestBody final AppointmentRequest request,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        final Appointment form = Appointment.builder()
                .type(request.getType())
                .paymentMethod(request.getPaymentMethod())
                .customerId(request.getCustomerId())
                .serviceId(request.getServiceId())
                .productId(request.getProductId())
                .tip(request.getTip())
                .build();

        final Context context = new Context(form);
        context.putProperty("barberId", userDetails.getUserId());

        final Appointment savedAppointment = registerAppointmentPort.execute(context);

        final AppointmentResponse response = mapper.mapToResponse(savedAppointment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Atendimento registrado com sucesso"));
    }
}