package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateBarberPort;
import app.system.fidelity.core.business.DeleteBarberPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.security.model.CustomUserDetails;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.UserMapper;
import app.system.fidelity.web.model.request.BarberRequest;
import app.system.fidelity.web.model.request.UserDeleteRequest;
import app.system.fidelity.web.model.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/barber")
@RequiredArgsConstructor
public class BarberController {

    private final CreateBarberPort createBarberPort;
    private final DeleteBarberPort deleteBarberPort;
    private final UserMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody final BarberRequest request
    ) {
        final User form = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        final Context context = new Context(form);
        final User savedBarber = createBarberPort.execute(context);

        final UserResponse response = mapper.mapToResponse(savedBarber);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Barbeiro criado com sucesso"));
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable final UUID id,
            @Valid @RequestBody final UserDeleteRequest request,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        final Context context = new Context();
        context.putProperty("barberId", id);
        context.putProperty("authenticatedUserId", userDetails.getUserId());
        context.putProperty("emailConfirmation", request.emailConfirmation());

        deleteBarberPort.execute(context);

        return ResponseEntity.ok(ApiResponse.success("Barbeiro deletado com sucesso"));
    }
}