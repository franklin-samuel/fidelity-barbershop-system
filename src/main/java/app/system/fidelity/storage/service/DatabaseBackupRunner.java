package app.system.fidelity.storage.service;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.storage.DatabaseBackupPort;
import app.system.fidelity.core.storage.GoogleDriveStoragePort;
import app.system.fidelity.domain.BackupStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBackupRunner {

    private static final ZoneId ZONE = ZoneId.of("America/Fortaleza");

    private final DatabaseBackupPort databaseBackupPort;
    private final GoogleDriveStoragePort googleDriveStoragePort;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "database-backup");
        thread.setDaemon(true);
        return thread;
    });

    private volatile BackupStatus status = BackupStatus.idle();

    public BackupStatus getStatus() {
        return status;
    }


    public boolean runNow(final String trigger) {
        if (!tryAcquire(trigger)) {
            return false;
        }
        execute(trigger);
        return true;
    }


    public boolean startAsync(final String trigger) {
        if (!tryAcquire(trigger)) {
            return false;
        }
        try {
            executor.submit(() -> execute(trigger));
        } catch (RejectedExecutionException e) {
            running.set(false);
            throw e;
        }
        return true;
    }

    private boolean tryAcquire(final String trigger) {
        if (!running.compareAndSet(false, true)) {
            log.warn("Backup [{}] ignorado: já existe um backup em execução", trigger);
            return false;
        }
        status = BackupStatus.running(trigger, LocalDateTime.now(ZONE));
        return true;
    }

    private void execute(final String trigger) {
        File backupFile = null;

        try {
            log.info("[{}] Etapa 1/3: Criando dump do banco de dados", trigger);
            backupFile = databaseBackupPort.execute(new Context());

            if (backupFile == null || !backupFile.exists()) {
                throw new RuntimeException("Arquivo de backup não foi criado");
            }

            final String fileName = backupFile.getName();
            final long sizeBytes = backupFile.length();
            log.info("[{}] Dump criado com sucesso: {} ({} MB)", trigger, fileName, sizeBytes / (1024 * 1024));

            log.info("[{}] Etapa 2/3: Fazendo upload para Google Drive", trigger);
            final Context driveContext = new Context();
            driveContext.putProperty("backupFile", backupFile);
            final String fileId = googleDriveStoragePort.execute(driveContext);
            log.info("[{}] Upload concluído. Google Drive File ID: {}", trigger, fileId);

            status = status.succeeded(LocalDateTime.now(ZONE), fileName, sizeBytes, fileId);
            log.info("[{}] Backup concluído com sucesso! Arquivo: {} | Tamanho: {} MB | Google Drive ID: {}",
                    trigger, fileName, sizeBytes / (1024 * 1024), fileId);

        } catch (Exception e) {
            log.error("[{}] ERRO no backup: {}", trigger, e.getMessage(), e);
            status = status.failed(
                    LocalDateTime.now(ZONE),
                    backupFile != null ? backupFile.getName() : null,
                    e.getMessage()
            );
        } finally {
            log.info("[{}] Etapa 3/3: Removendo arquivo local temporário", trigger);
            deleteLocalFile(backupFile);
            running.set(false);
        }
    }

    private void deleteLocalFile(final File backupFile) {
        if (backupFile == null || !backupFile.exists()) {
            return;
        }
        try {
            if (backupFile.delete()) {
                log.info("Arquivo local removido: {}", backupFile.getName());
            } else {
                log.warn("Não foi possível remover arquivo local: {}", backupFile.getName());
            }
        } catch (Exception e) {
            log.warn("Erro ao limpar arquivo local: {}", e.getMessage());
        }
    }
}
