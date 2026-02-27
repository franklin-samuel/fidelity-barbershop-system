package app.system.fidelity.messaging.adapter;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.messaging.GenerateMonthlyReportPort;
import app.system.fidelity.core.messaging.SendMonthlyReportEmailPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.MonthlyReport;
import app.system.fidelity.domain.User;
import app.system.fidelity.messaging.service.EmailService;
import app.system.fidelity.messaging.service.PdfGeneratorService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class SendMonthlyReportEmailAdapter implements SendMonthlyReportEmailPort {

    private final GenerateMonthlyReportPort generateMonthlyReportPort;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;
    private final UserRepositoryPort userRepository;

    @Override
    public Void execute(final Context context) {
        try {
            log.info("Iniciando envio de relatório mensal para administradores");

            final MonthlyReport report = generateMonthlyReportPort.execute(new Context());

            final byte[] pdfContent = pdfGeneratorService.generateMonthlyReportPdf(report);

            final List<String> adminEmails = userRepository.findAllAdmins()
                    .stream()
                    .map(User::getEmail)
                    .toList();

            if (adminEmails.isEmpty()) {
                log.warn("Nenhum administrador encontrado para envio de relatório mensal");
                return null;
            }

            emailService.sendMonthlyReportEmail(adminEmails, pdfContent, report.getReportMonth());

            log.info("Relatório mensal enviado com sucesso para {} administradores", adminEmails.size());

            return null;

        } catch (Exception e) {
            log.error("Erro ao processar envio de relatório mensal", e);
            return null;
        }
    }
}