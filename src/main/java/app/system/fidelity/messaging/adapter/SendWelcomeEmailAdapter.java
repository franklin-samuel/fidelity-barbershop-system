package app.system.fidelity.messaging.adapter;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.messaging.SendWelcomeEmailPort;
import app.system.fidelity.core.persistence.SettingsRepositoryPort;
import app.system.fidelity.domain.Settings;
import app.system.fidelity.messaging.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendWelcomeEmailAdapter implements SendWelcomeEmailPort {

    private final EmailService emailService;
    private final SettingsRepositoryPort settingsRepository;

    @Async("emailTaskExecutor")
    @Override
    public Void execute(final Context context) {
        try {
            final String customerEmail = context.getProperty("customerEmail", String.class);
            final String customerName = context.getProperty("customerName", String.class);

            if (customerEmail == null || customerEmail.isBlank()) {
                log.warn("Email do cliente não fornecido, pulando envio de boas-vindas");
                return null;
            }

            if (customerName == null || customerName.isBlank()) {
                log.warn("Nome do cliente não fornecido, pulando envio de boas-vindas");
                return null;
            }

            final Settings settings = settingsRepository.findAll().stream()
                    .findFirst()
                    .orElse(null);

            final int requiredCuts = settings != null ? settings.getHaircutsForFree() : 4;

            log.info("Iniciando envio de email de boas-vindas para {} ({})", customerName, customerEmail);

            emailService.sendWelcomeEmail(customerEmail, customerName, requiredCuts);

            log.info("Email de boas-vindas processado para {}", customerEmail);

            return null;

        } catch (Exception e) {
            log.error("Erro ao processar envio de email de boas-vindas", e);
            return null;
        }
    }
}