package app.system.fidelity.storage.scheduler;

import app.system.fidelity.storage.service.DatabaseBackupRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseBackupScheduler {

    private final DatabaseBackupRunner backupRunner;

    @Scheduled(cron = "0 0 3 * * *", zone = "America/Fortaleza")
    public void executeDailyBackup() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Fortaleza"));
        log.info("Iniciando backup automático diário - {}", now);

        backupRunner.runNow("SCHEDULED");
    }
}
