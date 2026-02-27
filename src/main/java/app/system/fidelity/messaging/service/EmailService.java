package app.system.fidelity.messaging.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Slf4j
@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.messaging.barbershop-name:Barbearia}")
    private String barbershopName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMonthlyReportEmail(
            final List<String> toEmails,
            final byte[] pdfAttachment,
            final YearMonth reportMonth
    ) {
        if (toEmails == null || toEmails.isEmpty()) {
            log.warn("Nenhum destinatário fornecido");
            return;
        }

        try {
            final String subject = buildSubject(reportMonth);
            final String htmlContent = buildHtmlContent(reportMonth);
            final String filename = buildPdfFilename(reportMonth);

            for (String email : toEmails) {
                sendEmail(email, subject, htmlContent, pdfAttachment, filename);
            }

            log.info("Relatório mensal enviado com sucesso para {} destinatários", toEmails.size());

        } catch (Exception e) {
            log.error("Erro ao enviar email com relatório mensal", e);
            throw new RuntimeException("Falha ao enviar email", e);
        }
    }

    private void sendEmail(String to, String subject, String html, byte[] attachment, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", fromEmail);
        body.put("to", Collections.singletonList(to));
        body.put("subject", subject);
        body.put("html", html);

        if (attachment != null) {
            Map<String, String> attachmentData = new HashMap<>();
            attachmentData.put("filename", filename);
            attachmentData.put("content", Base64.getEncoder().encodeToString(attachment));
            body.put("attachments", Collections.singletonList(attachmentData));
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.resend.com/emails",
                HttpMethod.POST,
                request,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Falha ao enviar email via Resend: " + response.getBody());
        }
    }

    private String buildSubject(final YearMonth reportMonth) {
        final String monthName = reportMonth.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        return String.format("[%s] Relatório Mensal - %s/%d",
                barbershopName,
                monthName.substring(0, 1).toUpperCase() + monthName.substring(1),
                reportMonth.getYear());
    }

    private String buildHtmlContent(final YearMonth reportMonth) {
        final String monthYear = formatMonthYear(reportMonth);

        return "<!DOCTYPE html>" +
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<h1>Relatório Mensal</h1>" +
                "<p><strong>" + monthYear + "</strong></p>" +
                "<p>Olá! 👋</p>" +
                "<p>O relatório mensal de <strong>" + barbershopName + "</strong> está pronto!</p>" +
                "<p>O PDF está anexado neste email.</p>" +
                "<hr>" +
                "<p style='color: #666; font-size: 12px;'>Email automático - " + barbershopName + "</p>" +
                "</div>" +
                "</body>" +
                "</html>";
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