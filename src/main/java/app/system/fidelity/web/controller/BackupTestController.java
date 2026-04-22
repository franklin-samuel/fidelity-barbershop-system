package app.system.fidelity.web.controller;

import app.system.fidelity.storage.scheduler.DatabaseBackupScheduler;
import app.system.fidelity.storage.service.DatabaseBackupService;
import app.system.fidelity.storage.service.GoogleDriveDebugService;
import app.system.fidelity.storage.service.GoogleDriveService;
import app.system.fidelity.web.commons.ApiResponse;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test/backup")
@RequiredArgsConstructor
public class BackupTestController {

    private final DatabaseBackupScheduler backupScheduler;
    private final GoogleDriveDebugService debugService;
    private final DatabaseBackupService backupService;
    private final GoogleDriveService driveService;

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executeManualBackup() {
        try {
            backupScheduler.executeManualBackup();

            final Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("timestamp", LocalDateTime.now());
            result.put("message", "Backup executado com sucesso! Verifique os logs para detalhes.");

            return ResponseEntity.ok(ApiResponse.success(result,
                    "Backup manual executado com sucesso"));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "Erro ao executar backup: " + e.getMessage()));
        }
    }

    @GetMapping("/run-tests")
    public ResponseEntity<Map<String, Object>> runAllTests() {

        Map<String, Object> results = debugService.runAllTests();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/list-local")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listLocalBackups() {
        try {
            final List<java.io.File> backups = backupService.listBackups();

            final List<Map<String, Object>> backupInfo = backups.stream()
                    .map(file -> {
                        final Map<String, Object> info = new HashMap<>();
                        info.put("name", file.getName());
                        info.put("size_mb", file.length() / (1024 * 1024));
                        info.put("created_at", LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(file.lastModified()),
                                ZoneId.systemDefault()
                        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        info.put("path", file.getAbsolutePath());
                        return info;
                    })
                    .collect(Collectors.toList());

            final Map<String, Object> result = new HashMap<>();
            result.put("count", backups.size());
            result.put("directory", backupService.getBackupDirectory());
            result.put("backups", backupInfo);

            return ResponseEntity.ok(ApiResponse.success(result,
                    backups.size() + " backup(s) local(is) encontrado(s)"));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "Erro ao listar backups locais: " + e.getMessage()));
        }
    }

    @GetMapping("/list-drive")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listDriveBackups() {
        try {
            final List<File> backups = driveService.listBackups();

            final List<Map<String, Object>> backupInfo = backups.stream()
                    .map(file -> {
                        final Map<String, Object> info = new HashMap<>();
                        info.put("id", file.getId());
                        info.put("name", file.getName());
                        info.put("size_mb", file.getSize() != null ? file.getSize() / (1024 * 1024) : 0);
                        info.put("created_at", file.getCreatedTime() != null
                                ? LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(file.getCreatedTime().getValue()),
                                ZoneId.systemDefault()
                        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                : "N/A");
                        return info;
                    })
                    .collect(Collectors.toList());

            final Map<String, Object> result = new HashMap<>();
            result.put("count", backups.size());
            result.put("backups", backupInfo);

            return ResponseEntity.ok(ApiResponse.success(result,
                    backups.size() + " backup(s) encontrado(s) no Google Drive"));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "Erro ao listar backups no Google Drive: " + e.getMessage()));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLatestBackup() {
        try {
            final File latestBackup = driveService.getLatestBackup();

            if (latestBackup == null) {
                return ResponseEntity.ok(ApiResponse.success(
                        Collections.emptyMap(),
                        "Nenhum backup encontrado no Google Drive"));
            }

            final Map<String, Object> info = new HashMap<>();
            info.put("id", latestBackup.getId());
            info.put("name", latestBackup.getName());
            info.put("size_mb", latestBackup.getSize() != null ? latestBackup.getSize() / (1024 * 1024) : 0);
            info.put("created_at", latestBackup.getCreatedTime() != null
                    ? LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(latestBackup.getCreatedTime().getValue()),
                    ZoneId.systemDefault()
            ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : "N/A");

            return ResponseEntity.ok(ApiResponse.success(info,
                    "Último backup encontrado"));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "Erro ao buscar último backup: " + e.getMessage()));
        }
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBackupInfo() {
        final Map<String, Object> info = new HashMap<>();

        info.put("local_backup_dir", backupService.getBackupDirectory());
        info.put("max_local_days", 7);
        info.put("max_drive_files", 7);
        info.put("schedule", "Diariamente às 03:00 (America/Fortaleza)");
        info.put("next_execution", "Próximo backup agendado para amanhã às 03:00");

        return ResponseEntity.ok(ApiResponse.success(info,
                "Configuração de backup"));
    }
    
    @GetMapping("/help")
    public ResponseEntity<ApiResponse<Map<String, String>>> help() {
        final Map<String, String> endpoints = new HashMap<>();

        endpoints.put("POST /test/backup/execute",
                "Executa backup completo manualmente (dump + upload para Drive)");

        endpoints.put("GET /test/backup/list-local",
                "Lista backups salvos localmente em /tmp/db-backups");

        endpoints.put("GET /test/backup/list-drive",
                "Lista backups disponíveis no Google Drive");

        endpoints.put("GET /test/backup/latest",
                "Mostra informações sobre o último backup no Drive");

        endpoints.put("GET /test/backup/info",
                "Informações sobre a configuração de backup");

        endpoints.put("GET /test/backup/help",
                "Esta mensagem de ajuda");

        return ResponseEntity.ok(ApiResponse.success(endpoints,
                "Endpoints de teste de backup disponíveis"));
    }
}