package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.CreateUserPort;
import app.system.fidelity.core.business.DeleteUserPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.security.model.CustomUserDetails;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.UserMapper;
import app.system.fidelity.web.model.request.UserDeleteRequest;
import app.system.fidelity.web.model.request.UserRequest;
import app.system.fidelity.web.model.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepositoryPort repository;
    private final CreateUserPort createUserPort;
    private final DeleteUserPort deleteUserPort;
    private final UserMapper mapper;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody final UserRequest request
    ) {

        final User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        final Context context = new Context(user);
        final User savedUser = createUserPort.execute(context);

        final UserResponse response = mapper.mapToResponse(savedUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Usuário criado com sucesso"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> list() {

        final List<User> users = repository.findAll();

        final List<UserResponse> responses = users.stream()
                .map(mapper::mapToResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable final UUID id,
            @Valid @RequestBody final UserDeleteRequest request,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {

        final Context context = new Context();
        context.putProperty("userId", id);
        context.putProperty("authenticatedUserId", userDetails.getUserId());
        context.putProperty("emailConfirmation", request.emailConfirmation());

        deleteUserPort.execute(context);

        return ResponseEntity.ok(ApiResponse.success("Usuário deletado com sucesso"));
    }
}