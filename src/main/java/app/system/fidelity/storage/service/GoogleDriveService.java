package app.system.fidelity.storage.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class GoogleDriveService {

    @Value("${google.api.key}")
    private String googleApiKey;

    private static final String APPLICATION_NAME = "Fidelity Backup System";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String BACKUP_FOLDER_NAME = "database-backups";
    private static final int MAX_BACKUP_FILES = 7;

    private Drive driveService;

    private Drive getDriveService() throws IOException, GeneralSecurityException {
        if (driveService != null) {
            return driveService;
        }

        log.info("Inicializando Google Drive Service");

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();


        final GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream("C:/Coding/projects/fidelity-barbershop-system/src/main/resources/nagaragem-494117-eaf5eb82910d.json")
        ).createScoped(Collections.singletonList("https://www.googleapis.com/auth/drive.file"));

        driveService = new Drive.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        log.info("Google Drive Service inicializado com sucesso");

        return driveService;
    }

    public String uploadFile(final java.io.File fileToUpload) throws IOException, GeneralSecurityException {
        log.info("Iniciando upload do arquivo: {} ({} MB)",
                fileToUpload.getName(),
                fileToUpload.length() / (1024 * 1024));

        final Drive service = getDriveService();

        final String folderId = getOrCreateBackupFolder(service);

        final File fileMetadata = new File();
        fileMetadata.setName(fileToUpload.getName());
        fileMetadata.setParents(Collections.singletonList(folderId));

        final FileContent mediaContent = new FileContent("application/sql", fileToUpload);

        final File uploadedFile = service.files()
                .create(fileMetadata, mediaContent)
                .setFields("id, name, size, createdTime")
                .execute();

        log.info("Upload concluído com sucesso. File ID: {}", uploadedFile.getId());

        cleanOldBackupsFromDrive(service, folderId);

        return uploadedFile.getId();
    }


    private String getOrCreateBackupFolder(final Drive service) throws IOException {
        final String query = String.format(
                "name='%s' and mimeType='application/vnd.google-apps.folder' and trashed=false",
                BACKUP_FOLDER_NAME
        );

        final FileList result = service.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        final List<File> folders = result.getFiles();

        if (folders != null && !folders.isEmpty()) {
            final String folderId = folders.get(0).getId();
            log.info("Pasta de backups encontrada: {}", folderId);
            return folderId;
        }

        log.info("Criando nova pasta de backups: {}", BACKUP_FOLDER_NAME);

        final File folderMetadata = new File();
        folderMetadata.setName(BACKUP_FOLDER_NAME);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");

        final File folder = service.files()
                .create(folderMetadata)
                .setFields("id")
                .execute();

        log.info("Pasta de backups criada: {}", folder.getId());

        return folder.getId();
    }

    private void cleanOldBackupsFromDrive(final Drive service, final String folderId) throws IOException {
        log.info("Verificando backups antigos no Google Drive");

        final String query = String.format(
                "'%s' in parents and trashed=false and name contains 'backup_'",
                folderId
        );

        final FileList result = service.files().list()
                .setQ(query)
                .setOrderBy("createdTime desc")
                .setFields("files(id, name, createdTime)")
                .execute();

        final List<File> backupFiles = result.getFiles();

        if (backupFiles == null || backupFiles.size() <= MAX_BACKUP_FILES) {
            log.info("Nenhum backup antigo para remover ({} arquivos encontrados)",
                    backupFiles != null ? backupFiles.size() : 0);
            return;
        }

        final List<File> filesToDelete = backupFiles.subList(MAX_BACKUP_FILES, backupFiles.size());

        for (final File file : filesToDelete) {
            try {
                service.files().delete(file.getId()).execute();
                log.info("Backup antigo removido do Drive: {} ({})",
                        file.getName(),
                        file.getCreatedTime());
            } catch (IOException e) {
                log.warn("Erro ao deletar backup antigo: {}", file.getName(), e);
            }
        }

        log.info("Limpeza concluída. {} backup(s) removido(s)", filesToDelete.size());
    }

    public List<File> listBackups() throws IOException, GeneralSecurityException {
        final Drive service = getDriveService();
        final String folderId = getOrCreateBackupFolder(service);

        final String query = String.format(
                "'%s' in parents and trashed=false and name contains 'backup_'",
                folderId
        );

        final FileList result = service.files().list()
                .setQ(query)
                .setOrderBy("createdTime desc")
                .setFields("files(id, name, size, createdTime)")
                .execute();

        return result.getFiles();
    }

    public File getLatestBackup() throws IOException, GeneralSecurityException {
        final List<File> backups = listBackups();

        if (backups.isEmpty()) {
            return null;
        }

        return backups.get(0);
    }
}