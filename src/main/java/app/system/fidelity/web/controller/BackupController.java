package app.system.fidelity.web.controller;

import app.system.fidelity.domain.BackupStatus;
import app.system.fidelity.storage.service.DatabaseBackupRunner;
import app.system.fidelity.web.commons.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backup")
@RequiredArgsConstructor
public class BackupController {

    private final DatabaseBackupRunner backupRunner;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<BackupStatus>> run() {
        if (!backupRunner.startAsync("MANUAL")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Já existe um backup em andamento", "BACKUP_IN_PROGRESS"));
        }

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(
                        backupRunner.getStatus(),
                        "Backup iniciado. Acompanhe em GET /api/backup/status"
                ));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<BackupStatus>> status() {
        return ResponseEntity.ok(ApiResponse.success(backupRunner.getStatus()));
    }
}
