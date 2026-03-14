package app.system.fidelity.messaging.adapter;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.messaging.GenerateMonthlyReportPort;
import app.system.fidelity.core.messaging.SendMonthlyReportEmailPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.MonthlyReport;
import app.system.fidelity.domain.User;
import app.system.fidelity.messaging.service.EmailService;
import app.system.fidelity.messaging.service.GeminiService;
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
    private final GeminiService geminiService;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;
    private final UserRepositoryPort userRepository;

    @Override
    public Void execute(final Context context) {
        try {
            log.info("Iniciando envio de relatório mensal com insights de IA para administradores");

            final MonthlyReport report = generateMonthlyReportPort.execute(new Context());

            log.info("Gerando insights com Gemini AI...");
            String aiInsights = null;
            try {
                aiInsights = geminiService.generateInsights(report);
                log.info("Insights gerados com sucesso");
            } catch (Exception e) {
                log.error("Falha ao gerar insights com IA: {}. Continuando sem insights.", e.getMessage());
            }

            final byte[] pdfContent = pdfGeneratorService.generateMonthlyReportPdf(report);

            final List<String> adminEmails = userRepository.findAllAdmins()
                    .stream()
                    .map(User::getEmail)
                    .toList();

            if (adminEmails.isEmpty()) {
                log.warn("Nenhum administrador encontrado para envio de relatório mensal");
                return null;
            }

            emailService.sendMonthlyReportEmail(
                    adminEmails,
                    pdfContent,
                    report.getReportMonth(),
                    aiInsights,
                    report.getTotalRevenue(),
                    report.getRevenueGrowthPercentage(),
                    report.getTotalAppointments(),
                    report.getAverageTicket(),
                    report.getNewCustomers()
            );

            log.info("Relatório mensal com insights enviado com sucesso para {} administradores", adminEmails.size());

            return null;

        } catch (Exception e) {
            log.error("Erro ao processar envio de relatório mensal", e);
            return null;
        }
    }
}