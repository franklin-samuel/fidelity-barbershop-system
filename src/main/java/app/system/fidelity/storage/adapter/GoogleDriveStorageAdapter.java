package app.system.fidelity.storage.adapter;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.storage.GoogleDriveStoragePort;
import app.system.fidelity.storage.service.GoogleDriveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveStorageAdapter implements GoogleDriveStoragePort {

    private final GoogleDriveService driveService;

    @Override
    public String execute(final Context context) {
        try {
            final File backupFile = context.getProperty("backupFile", File.class);

            if (backupFile == null || !backupFile.exists()) {
                throw new IllegalArgumentException("Arquivo de backup não encontrado");
            }

            log.info("Iniciando upload do backup para Google Drive: {}", backupFile.getName());

            final String fileId = driveService.uploadFile(backupFile);

            log.info("Upload para Google Drive concluído. File ID: {}", fileId);

            return fileId;

        } catch (Exception e) {
            log.error("Erro ao fazer upload para Google Drive", e);
            throw new RuntimeException("Falha ao enviar backup para Google Drive: " + e.getMessage(), e);
        }
    }
}