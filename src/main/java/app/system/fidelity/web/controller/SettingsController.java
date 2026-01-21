package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.UpdateSettingsPort;
import app.system.fidelity.core.persistence.SettingsRepositoryPort;
import app.system.fidelity.domain.Settings;
import app.system.fidelity.domain.exceptions.BusinessException;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.SettingsMapper;
import app.system.fidelity.web.model.request.SettingsUpdateRequest;
import app.system.fidelity.web.model.response.SettingsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsRepositoryPort repository;
    private final UpdateSettingsPort updateSettingsPort;
    private final SettingsMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> get() {

        Settings settings = repository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Configurações não encontradas"));

        SettingsResponse response = mapper.mapToResponse(settings);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> update(
            @Valid @RequestBody final SettingsUpdateRequest request
    ) {

        Settings settingsForm = Settings.builder()
                .haircuts_for_free(request.haircutsForFree())
                .build();

        Context context = new Context(settingsForm);

        Settings updatedSettings = updateSettingsPort.execute(context);

        SettingsResponse response = mapper.mapToResponse(updatedSettings);

        return ResponseEntity.ok(ApiResponse.success(response, "Configurações atualizadas com sucesso"));
    }
}
