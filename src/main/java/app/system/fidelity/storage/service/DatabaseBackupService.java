package app.system.fidelity.storage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DatabaseBackupService {

    @Value("${spring.datasource.host}")
    private String dbHost;

    @Value("${spring.datasource.port}")
    private String dbPort;

    @Value("${spring.datasource.name}")
    private String dbName;

    @Value("${spring.datasource.hikari.username}")
    private String dbUsername;

    @Value("${spring.datasource.hikari.password}")
    private String dbPassword;

    private static final String BACKUP_DIR = "/tmp/db-backups";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final int MAX_BACKUP_DAYS = 7;

    public File createDatabaseDump() throws IOException, InterruptedException {
        log.info("Iniciando backup do banco de dados: {}", dbName);

        createBackupDirectory();

        cleanOldBackups();

        final String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        final String fileName = String.format("backup_%s_%s.sql", dbName, timestamp);
        final File backupFile = new File(BACKUP_DIR, fileName);

        final List<String> command = buildPgDumpCommand(backupFile.getAbsolutePath());

        log.info("Executando comando: {}", String.join(" ", command));

        final ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.environment().put("PGPASSWORD", dbPassword);

        processBuilder.redirectErrorStream(true);

        final Process process = processBuilder.start();

        final StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("pg_dump output: {}", line);
            }
        }

        final boolean finished = process.waitFor(10, TimeUnit.MINUTES);

        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Timeout ao executar pg_dump - processo cancelado após 10 minutos");
        }

        final int exitCode = process.exitValue();

        if (exitCode != 0) {
            log.error("Erro ao executar pg_dump. Exit code: {}. Output: {}", exitCode, output);
            throw new RuntimeException("Falha ao criar backup do banco de dados. Exit code: " + exitCode);
        }

        if (!backupFile.exists() || backupFile.length() == 0) {
            throw new RuntimeException("Arquivo de backup não foi criado ou está vazio");
        }

        final long fileSizeMB = backupFile.length() / (1024 * 1024);
        log.info("Backup criado com sucesso: {} ({} MB)", backupFile.getName(), fileSizeMB);

        return backupFile;
    }

    private void createBackupDirectory() throws IOException {
        final Path backupPath = Paths.get(BACKUP_DIR);
        if (!Files.exists(backupPath)) {
            Files.createDirectories(backupPath);
            log.info("Diretório de backups criado: {}.", BACKUP_DIR);
        }
    }

    private void cleanOldBackups() throws IOException {
        final Path backupPath = Paths.get(BACKUP_DIR);

        if (!Files.exists(backupPath)) {
            return;
        }

        final LocalDateTime cutoffDate = LocalDateTime.now().minusDays(MAX_BACKUP_DAYS);

        Files.list(backupPath)
                .filter(path -> path.toString().endsWith(".sql"))
                .filter(path -> {
                    try {
                        final LocalDateTime fileTime = LocalDateTime.ofInstant(
                                Files.getLastModifiedTime(path).toInstant(),
                                java.time.ZoneId.systemDefault()
                        );
                        return fileTime.isBefore(cutoffDate);
                    } catch (IOException e) {
                        log.warn("Erro ao verificar data do arquivo: {}", path, e);
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.info("Backup antigo removido: {}", path.getFileName());
                    } catch (IOException e) {
                        log.warn("Erro ao deletar backup antigo: {}", path, e);
                    }
                });
    }

    private List<String> buildPgDumpCommand(final String outputFilePath) {
        final List<String> command = new ArrayList<>();

        command.add("pg_dump");
        command.add("--host=" + dbHost);
        command.add("--port=" + dbPort);
        command.add("--username=" + dbUsername);
        command.add("--dbname=" + dbName);
        command.add("--format=plain");
        command.add("--verbose");
        command.add("--file=" + outputFilePath);

        command.add("--clean");
        command.add("--if-exists");
        command.add("--no-owner");
        command.add("--no-privileges");

        return command;
    }

    public List<File> listBackups() throws IOException {
        final Path backupPath = Paths.get(BACKUP_DIR);

        if (!Files.exists(backupPath)) {
            return new ArrayList<>();
        }

        return Files.list(backupPath)
                .filter(path -> path.toString().endsWith(".sql"))
                .map(Path::toFile)
                .sorted((a, b) -> Long.compare(b.lastModified(), a.lastModified()))
                .toList();
    }

    public String getBackupDirectory() {
        return BACKUP_DIR;
    }

}
