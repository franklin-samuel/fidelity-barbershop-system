package app.system.fidelity.messaging.adapter;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.messaging.SendBackupFailureAlertPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.User;
import app.system.fidelity.messaging.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendBackupFailureAlertAdapter implements SendBackupFailureAlertPort {

    private final EmailService emailService;
    private final UserRepositoryPort userRepository;

    @Override
    public Void execute(final Context context) {
        try {
            final String trigger = context.getProperty("trigger", String.class);
            final String errorMessage = context.getProperty("errorMessage", String.class);
            final LocalDateTime failedAt = context.getProperty("failedAt", LocalDateTime.class);

            final List<String> adminEmails = userRepository.findAllAdmins()
                    .stream()
                    .map(User::getEmail)
                    .toList();

            if (adminEmails.isEmpty()) {
                log.warn("Nenhum administrador encontrado para envio de alerta de falha de backup");
                return null;
            }

            log.info("Enviando alerta de falha de backup para {} administrador(es)", adminEmails.size());

            emailService.sendBackupFailureAlert(adminEmails, trigger, errorMessage, failedAt);

            log.info("Alerta de falha de backup enviado com sucesso para {} administrador(es)", adminEmails.size());

            return null;

        } catch (Exception e) {
            log.error("Erro ao processar envio de alerta de falha de backup", e);
            return null;
        }
    }
}
