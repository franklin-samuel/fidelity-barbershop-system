package app.system.fidelity.web.controller;

import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.domain.exceptions.BusinessException;
import app.system.fidelity.security.model.CustomUserDetails;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.UserMapper;
import app.system.fidelity.web.model.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {

    private final UserRepositoryPort repository;
    private final UserMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getAuthenticatedUser(
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {

        final User user = repository.get(userDetails.getUserId())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        final UserResponse response = mapper.mapToResponse(user);

        return ResponseEntity.ok(ApiResponse.success(response));

    }

}
