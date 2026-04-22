package app.system.fidelity.storage.scheduler;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.storage.DatabaseBackupPort;
import app.system.fidelity.core.storage.GoogleDriveStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseBackupScheduler {

    private final DatabaseBackupPort databaseBackupPort;
    private final GoogleDriveStoragePort googleDriveStoragePort;

    @Scheduled(cron = "0 0 3 * * *", zone = "America/Fortaleza")
    public void executeDailyBackup() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Fortaleza"));
        log.info("Iniciando backup automático diário - {}", now);

        File backupFile = null;

        try {
            log.info("Etapa 1/3: Criando dump do banco de dados");
            final Context backupContext = new Context();
            backupFile = databaseBackupPort.execute(backupContext);

            if (backupFile == null || !backupFile.exists()) {
                throw new RuntimeException("Arquivo de backup não foi criado");
            }

            final long fileSizeMB = backupFile.length() / (1024 * 1024);
            log.info("Dump criado com sucesso: {} ({} MB)", backupFile.getName(), fileSizeMB);

            log.info("Etapa 2/3: Fazendo upload para Google Drive");
            final Context driveContext = new Context();
            driveContext.putProperty("backupFile", backupFile);

            final String fileId = googleDriveStoragePort.execute(driveContext);

            log.info("Upload concluído. Google Drive File ID: {}", fileId);

            log.info("Etapa 3/3: Removendo arquivo local temporário");
            if (backupFile.delete()) {
                log.info("Arquivo local removido: {}", backupFile.getName());
            } else {
                log.warn("Não foi possível remover arquivo local: {}", backupFile.getName());
            }

            log.info("Backup automático concluído com sucesso!");
            log.info("Arquivo: {}", backupFile.getName());
            log.info("Tamanho: {} MB", fileSizeMB);
            log.info("Google Drive ID: {}", fileId);

        } catch (Exception e) {
            log.error("ERRO no backup automático: {}", e.getMessage(), e);

            if (backupFile != null && backupFile.exists()) {
                try {
                    if (backupFile.delete()) {
                        log.info("Arquivo local de backup removido após erro");
                    }
                } catch (Exception cleanupError) {
                    log.warn("Erro ao limpar arquivo local após falha: {}", cleanupError.getMessage());
                }
            }
        }
    }

    public void executeManualBackup() {
        log.info("Executando backup manual (não agendado)");
        executeDailyBackup();
    }
}