package app.system.fidelity.messaging.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    @Value("${app.messaging.barbershop-name}")
    private String barbershopName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final TemplateService templateService;

    public void sendWelcomeEmail(
            final String toEmail,
            final String customerName,
            final int requiredCuts
    ) {
        try {
            final String subject = String.format("Bem-vindo à %s! 🎉", barbershopName);

            final Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customerName);
            variables.put("requiredCuts", requiredCuts);
            variables.put("barbershopName", barbershopName);

            final String htmlContent = templateService.processTemplate("welcome-email", variables);

            sendEmail(toEmail, subject, htmlContent, null, null);

            log.info("Email de boas-vindas enviado com sucesso para {}", toEmail);

        } catch (Exception e) {
            log.error("Erro ao enviar email de boas-vindas para {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendMonthlyReportEmail(
            final List<String> toEmails,
            final byte[] pdfAttachment,
            final YearMonth reportMonth,
            final String aiInsights,
            final BigDecimal totalRevenue,
            final BigDecimal revenueGrowthPercentage,
            final Long totalAppointments,
            final BigDecimal averageTicket,
            final Long newCustomers
    ) {
        if (toEmails == null || toEmails.isEmpty()) {
            log.warn("Nenhum destinatário fornecido para relatório mensal");
            return;
        }

        try {
            final String subject = buildMonthlyReportSubject(reportMonth);

            final Map<String, Object> variables = new HashMap<>();
            variables.put("monthYear", formatMonthYear(reportMonth));
            variables.put("barbershopName", barbershopName);

            variables.put("aiInsights", templateService.formatInsightsToHtml(aiInsights));

            variables.put("totalRevenue", formatCurrency(totalRevenue));
            variables.put("revenueGrowth", formatGrowthPercentage(revenueGrowthPercentage));
            variables.put("isGrowthPositive", revenueGrowthPercentage != null && revenueGrowthPercentage.compareTo(BigDecimal.ZERO) >= 0);
            variables.put("totalAppointments", totalAppointments);
            variables.put("averageTicket", formatCurrency(averageTicket));
            variables.put("newCustomers", newCustomers);

            final String htmlContent = templateService.processTemplate("monthly-insights-email", variables);
            final String filename = buildPdfFilename(reportMonth);

            for (String email : toEmails) {
                sendEmail(email, subject, htmlContent, pdfAttachment, filename);
            }

            log.info("Relatório mensal com insights enviado com sucesso para {} destinatários", toEmails.size());

        } catch (Exception e) {
            log.error("Erro ao enviar email com relatório mensal", e);
            throw new RuntimeException("Falha ao enviar email", e);
        }
    }

    private void sendEmail(
            final String to,
            final String subject,
            final String html,
            final byte[] attachment,
            final String filename
    ) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        final Map<String, Object> body = new HashMap<>();
        body.put("from", fromEmail);
        body.put("to", Collections.singletonList(to));
        body.put("subject", subject);
        body.put("html", html);

        if (attachment != null && filename != null) {
            final Map<String, String> attachmentData = new HashMap<>();
            attachmentData.put("filename", filename);
            attachmentData.put("content", Base64.getEncoder().encodeToString(attachment));
            body.put("attachments", Collections.singletonList(attachmentData));
        }

        final HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        final ResponseEntity<String> response = restTemplate.exchange(
                "https://api.resend.com/emails",
                HttpMethod.POST,
                request,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Falha ao enviar email via Resend: " + response.getBody());
        }
    }

    private String buildMonthlyReportSubject(final YearMonth reportMonth) {
        final String monthName = reportMonth.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        return String.format("[%s] Análise Mensal com Insights - %s/%d",
                barbershopName,
                monthName.substring(0, 1).toUpperCase() + monthName.substring(1),
                reportMonth.getYear());
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

    private String formatCurrency(final BigDecimal value) {
        if (value == null) return "R$ 0,00";
        return String.format("R$ %,.2f", value);
    }

    private String formatGrowthPercentage(final BigDecimal value) {
        if (value == null) return "0,00%";
        final String sign = value.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, value);
    }
}