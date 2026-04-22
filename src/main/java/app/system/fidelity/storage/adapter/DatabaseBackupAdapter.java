package app.system.fidelity.storage.adapter;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.storage.DatabaseBackupPort;
import app.system.fidelity.storage.service.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseBackupAdapter implements DatabaseBackupPort {

    private final DatabaseBackupService backupService;

    @Override
    public File execute(final Context context) {
        try {
            log.info("Executando backup do banco de dados");

            final File backupFile = backupService.createDatabaseDump();

            log.info("Backup do banco de dados concluído: {}", backupFile.getAbsolutePath());

            return backupFile;

        } catch (Exception e) {
            log.error("Erro ao executar backup do banco de dados", e);
            throw new RuntimeException("Falha ao criar backup do banco de dados: " + e.getMessage(), e);
        }
    }

}
