package app.system.fidelity.messaging.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.messaging.barbershop-name:Barbearia}")
    private String barbershopName;

    public void sendMonthlyReportEmail(
            final List<String> toEmails,
            final byte[] pdfAttachment,
            final YearMonth reportMonth
    ) {
        if (toEmails == null || toEmails.isEmpty()) {
            log.warn("Nenhum destinatário fornecido para envio de relatório mensal");
            return;
        }

        try {
            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmails.toArray(new String[0]));
            helper.setSubject(buildSubject(reportMonth));

            final String htmlContent = buildHtmlContent(reportMonth);
            helper.setText(htmlContent, true);

            final String filename = buildPdfFilename(reportMonth);
            helper.addAttachment(filename, () -> new java.io.ByteArrayInputStream(pdfAttachment));

            mailSender.send(message);

            log.info("Relatório mensal enviado com sucesso para {} destinatários", toEmails.size());

        } catch (MessagingException e) {
            log.error("Erro ao enviar email com relatório mensal", e);
            throw new RuntimeException("Falha ao enviar email com relatório mensal", e);
        }
    }

    private String buildSubject(final YearMonth reportMonth) {
        final String monthName = reportMonth.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        final int year = reportMonth.getYear();

        return String.format("[%s] Relatório Mensal - %s/%d",
                barbershopName,
                monthName.substring(0, 1).toUpperCase() + monthName.substring(1),
                year);
    }

    private String buildHtmlContent(final YearMonth reportMonth) {
        final Context context = new Context();
        context.setVariable("barbershopName", barbershopName);
        context.setVariable("monthYear", formatMonthYear(reportMonth));

        return templateEngine.process("monthly-report-email", context);
    }

    private String buildPdfFilename(final YearMonth reportMonth) {
        return String.format("relatorio-mensal-%s-%d.pdf",
                reportMonth.getMonth().toString().toLowerCase(),
                reportMonth.getYear());
    }

    private String formatMonthYear(final YearMonth yearMonth) {
        final String monthName = yearMonth.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        return String.format("%s de %d",
                monthName.substring(0, 1).toUpperCase() + monthName.substring(1),
                yearMonth.getYear());
    }
}