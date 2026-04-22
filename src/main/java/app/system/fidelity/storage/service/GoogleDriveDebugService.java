package app.system.fidelity.storage.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveDebugService {

    private final app.system.fidelity.storage.service.GoogleDriveService googleDriveService;

    public Map<String, Object> runAllTests() {
        Map<String, Object> results = new HashMap<>();

        try {
            results.put("test1_listAllFolders", testListAllFolders());
        } catch (Exception e) {
            results.put("test1_listAllFolders", "ERRO: " + e.getMessage());
        }

        try {
            results.put("test2_accessSpecificFolder", testAccessSpecificFolder());
        } catch (Exception e) {
            results.put("test2_accessSpecificFolder", "ERRO: " + e.getMessage());
        }

        try {
            results.put("test3_listSharedWithMe", testListSharedWithMe());
        } catch (Exception e) {
            results.put("test3_listSharedWithMe", "ERRO: " + e.getMessage());
        }

        try {
            results.put("test4_createFileInRoot", testCreateFileInRoot());
        } catch (Exception e) {
            results.put("test4_createFileInRoot", "ERRO: " + e.getMessage());
        }

        try {
            results.put("test5_createFileInTargetFolder", testCreateFileInTargetFolder());
        } catch (Exception e) {
            results.put("test5_createFileInTargetFolder", "ERRO: " + e.getMessage());
        }

        return results;
    }

    private Map<String, Object> testListAllFolders() throws IOException, GeneralSecurityException {
        log.info("=== TESTE 1: Listando todas as pastas acessíveis ===");

        Drive service = getDriveServiceViaReflection();

        FileList result = service.files().list()
                .setQ("mimeType='application/vnd.google-apps.folder'")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setFields("files(id, name, owners(displayName, emailAddress), shared)")
                .setPageSize(100)
                .execute();

        Map<String, Object> testResult = new HashMap<>();
        testResult.put("totalFolders", result.getFiles().size());
        testResult.put("folders", result.getFiles().stream()
                .map(f -> String.format("Nome: %s | ID: %s | Compartilhada: %s",
                        f.getName(), f.getId(), f.getShared()))
                .toList());

        log.info("Total de pastas encontradas: {}", result.getFiles().size());
        result.getFiles().forEach(folder ->
                log.info("Pasta: {} (ID: {}) - Compartilhada: {}",
                        folder.getName(), folder.getId(), folder.getShared())
        );

        return testResult;
    }

    private Map<String, Object> testAccessSpecificFolder() throws IOException, GeneralSecurityException {
        log.info("=== TESTE 2: Acessando pasta específica ===");

        Drive service = getDriveServiceViaReflection();
        String targetFolderId = "11-1SBC_f_kL88tTLhxkCQfZU9bjlxrRy";

        Map<String, Object> testResult = new HashMap<>();

        try {
            File folder = service.files().get(targetFolderId)
                    .setSupportsAllDrives(true)
                    .setFields("id, name, owners(displayName, emailAddress), permissions(emailAddress, role), shared, capabilities(canAddChildren)")
                    .execute();

            testResult.put("success", true);
            testResult.put("folderName", folder.getName());
            testResult.put("folderId", folder.getId());
            testResult.put("shared", folder.getShared());
            testResult.put("canAddChildren", folder.getCapabilities() != null ?
                    folder.getCapabilities().getCanAddChildren() : "unknown");
            testResult.put("owners", folder.getOwners());
            testResult.put("permissions", folder.getPermissions());

            log.info("✓ Pasta acessível: {}", folder.getName());
            log.info("✓ Pode adicionar arquivos: {}",
                    folder.getCapabilities() != null ? folder.getCapabilities().getCanAddChildren() : "unknown");

            if (folder.getPermissions() != null) {
                folder.getPermissions().forEach(perm ->
                        log.info("Permissão: {} - Papel: {}", perm.getEmailAddress(), perm.getRole())
                );
            }

        } catch (Exception e) {
            testResult.put("success", false);
            testResult.put("error", e.getMessage());
            log.error("✗ Não consegue acessar a pasta!", e);
        }

        return testResult;
    }

    private Map<String, Object> testListSharedWithMe() throws IOException, GeneralSecurityException {
        log.info("=== TESTE 3: Listando arquivos/pastas compartilhados comigo ===");

        Drive service = getDriveServiceViaReflection();

        FileList result = service.files().list()
                .setQ("sharedWithMe=true")
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .setFields("files(id, name, mimeType, owners(displayName, emailAddress))")
                .setPageSize(100)
                .execute();

        Map<String, Object> testResult = new HashMap<>();
        testResult.put("totalShared", result.getFiles().size());
        testResult.put("items", result.getFiles().stream()
                .map(f -> String.format("Nome: %s | ID: %s | Tipo: %s",
                        f.getName(), f.getId(),
                        f.getMimeType().contains("folder") ? "PASTA" : "ARQUIVO"))
                .toList());

        log.info("Total de itens compartilhados: {}", result.getFiles().size());
        result.getFiles().forEach(item ->
                log.info("Item compartilhado: {} (ID: {}) - Tipo: {}",
                        item.getName(), item.getId(),
                        item.getMimeType().contains("folder") ? "PASTA" : "ARQUIVO")
        );

        return testResult;
    }

    private Map<String, Object> testCreateFileInRoot() throws IOException, GeneralSecurityException {
        log.info("=== TESTE 4: Criando arquivo de teste na raiz (sem parent) ===");

        Drive service = getDriveServiceViaReflection();

        Map<String, Object> testResult = new HashMap<>();

        try {
            java.io.File tempFile = java.io.File.createTempFile("test_root_", ".txt");
            java.nio.file.Files.writeString(tempFile.toPath(), "Teste de upload na raiz");

            File fileMetadata = new File();
            fileMetadata.setName("test_root_" + System.currentTimeMillis() + ".txt");

            com.google.api.client.http.FileContent mediaContent =
                    new com.google.api.client.http.FileContent("text/plain", tempFile);

            File uploadedFile = service.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute();

            testResult.put("success", true);
            testResult.put("fileId", uploadedFile.getId());
            testResult.put("fileName", uploadedFile.getName());

            log.info("✓ Arquivo criado na raiz com sucesso: {} (ID: {})",
                    uploadedFile.getName(), uploadedFile.getId());

            service.files().delete(uploadedFile.getId()).execute();
            log.info("✓ Arquivo de teste removido");

            tempFile.delete();

        } catch (Exception e) {
            testResult.put("success", false);
            testResult.put("error", e.getMessage());
            log.error("✗ Falha ao criar arquivo na raiz: {}", e.getMessage());
        }

        return testResult;
    }

    private Map<String, Object> testCreateFileInTargetFolder() throws IOException, GeneralSecurityException {
        log.info("=== TESTE 5: Criando arquivo de teste na pasta alvo ===");

        Drive service = getDriveServiceViaReflection();
        String targetFolderId = "11-1SBC_f_kL88tTLhxkCQfZU9bjlxrRy";

        Map<String, Object> testResult = new HashMap<>();

        try {
            java.io.File tempFile = java.io.File.createTempFile("test_folder_", ".txt");
            java.nio.file.Files.writeString(tempFile.toPath(), "Teste de upload na pasta específica");

            File fileMetadata = new File();
            fileMetadata.setName("test_folder_" + System.currentTimeMillis() + ".txt");
            fileMetadata.setParents(java.util.Collections.singletonList(targetFolderId));

            com.google.api.client.http.FileContent mediaContent =
                    new com.google.api.client.http.FileContent("text/plain", tempFile);

            File uploadedFile = service.files()
                    .create(fileMetadata, mediaContent)
                    .setSupportsAllDrives(true)
                    .setFields("id, name")
                    .execute();

            testResult.put("success", true);
            testResult.put("fileId", uploadedFile.getId());
            testResult.put("fileName", uploadedFile.getName());

            log.info("✓ Arquivo criado na pasta alvo com sucesso: {} (ID: {})",
                    uploadedFile.getName(), uploadedFile.getId());

            service.files().delete(uploadedFile.getId())
                    .setSupportsAllDrives(true)
                    .execute();
            log.info("✓ Arquivo de teste removido");

            tempFile.delete();

        } catch (Exception e) {
            testResult.put("success", false);
            testResult.put("error", e.getMessage());
            testResult.put("errorDetails", e.getClass().getName());
            log.error("✗ Falha ao criar arquivo na pasta alvo: {}", e.getMessage(), e);
        }

        return testResult;
    }

    private Drive getDriveServiceViaReflection() throws IOException, GeneralSecurityException {
        try {
            java.lang.reflect.Method method = googleDriveService.getClass()
                    .getDeclaredMethod("getDriveService");
            method.setAccessible(true);
            return (Drive) method.invoke(googleDriveService);
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível acessar getDriveService", e);
        }
    }
}