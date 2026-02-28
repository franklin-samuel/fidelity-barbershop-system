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

    @Value("${resend.from.email}")
    private String fromEmail;

    @Value("${app.messaging.barbershop-name}")
    private String barbershopName;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendWelcomeEmail(
            final String toEmail,
            final String customerName,
            final int requiredCuts
    ) {
        try {
            final String subject = String.format("Bem-vindo à %s! 🎉", barbershopName);
            final String htmlContent = buildWelcomeEmailHtml(customerName, requiredCuts);

            sendEmail(toEmail, subject, htmlContent, null, null);

            log.info("Email de boas-vindas enviado com sucesso para {}", toEmail);

        } catch (Exception e) {
            log.error("Erro ao enviar email de boas-vindas para {}: {}", toEmail, e.getMessage(), e);
        }
    }

    public void sendMonthlyReportEmail(
            final List<String> toEmails,
            final byte[] pdfAttachment,
            final YearMonth reportMonth
    ) {
        if (toEmails == null || toEmails.isEmpty()) {
            log.warn("Nenhum destinatário fornecido para relatório mensal");
            return;
        }

        try {
            final String subject = buildMonthlyReportSubject(reportMonth);
            final String htmlContent = buildMonthlyReportHtml(reportMonth);
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
        return String.format("[%s] Relatório Mensal - %s/%d",
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

    private String buildWelcomeEmailHtml(final String customerName, final int requiredCuts) {
        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bem-vindo à Na Garagem Barbearia</title>
</head>
<body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
<table width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f4f4f4; padding: 40px 0;">
    <tr>
        <td align="center">
            <table width="600" cellpadding="0" cellspacing="0" border="0" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                
                <!-- Header -->
                <tr>
                    <td style="background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%); padding: 40px 30px; border-radius: 8px 8px 0 0;">
                        <h1 style="margin: 0; color: #ffffff; font-size: 32px; text-align: center; font-weight: 700; letter-spacing: -0.5px;">
                            Bem-vindo!
                        </h1>
                        <p style="margin: 15px 0 0 0; color: #ffffff; font-size: 18px; text-align: center; opacity: 0.95; font-weight: 500;">
                            """ + barbershopName + """
                        </p>
                    </td>
                </tr>
                
                <!-- Body -->
                <tr>
                    <td style="padding: 40px 30px;">
                        <!-- Saudação personalizada -->
                        <p style="margin: 0 0 20px 0; font-size: 18px; color: #333333; line-height: 1.6;">
                            Olá, <strong>""" + customerName + """
</strong>! 👋
                        </p>
                        
                        <p style="margin: 0 0 20px 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            É um prazer ter você com a gente! Estamos muito felizes em recebê-lo na família <strong>""" + barbershopName + """
</strong>.
                        </p>
                        
                        <!-- Oferta especial -->
                        <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; margin: 30px 0; border-radius: 8px; text-align: center;">
                            <h2 style="margin: 0 0 15px 0; color: #ffffff; font-size: 24px; font-weight: 700;">
                                Oferta Especial!
                            </h2>
                            <p style="margin: 0 0 10px 0; color: #ffffff; font-size: 18px; line-height: 1.6;">
                                No seu <strong>""" + requiredCuts + """
º</strong> corte, você ganha
                            </p>
                            <p style="margin: 0; color: #ffd700; font-size: 36px; font-weight: 700; text-shadow: 2px 2px 4px rgba(0,0,0,0.2);">
                                50% de DESCONTO
                            </p>
                        </div>
                        
                        <p style="margin: 0 0 20px 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            A cada atendimento, você acumula pontos. Quando completar <strong>""" + requiredCuts + """
</strong> cortes, automaticamente ganha metade do valor de volta no próximo!
                        </p>
                        
                        <!-- Informações de contato -->
                        <div style="background-color: #f8f9fa; padding: 25px; margin: 30px 0; border-radius: 8px; border-left: 4px solid #667eea;">
                            <h3 style="margin: 0 0 15px 0; color: #333333; font-size: 18px; font-weight: 600;">
                                Nossas Informações
                            </h3>
                            
                            <p style="margin: 0 0 10px 0; font-size: 15px; color: #555555; line-height: 1.8;">
                                <strong>Endereço:</strong><br>
                                R. Carmindo Quadros, 46<br>
                                Nova Parnamirim, Parnamirim - RN<br>
                                CEP: 59152-770
                            </p>
                            
                            <p style="margin: 15px 0 10px 0; font-size: 15px; color: #555555; line-height: 1.8;">
                                <strong>WhatsApp:</strong><br>
                                <a href="https://wa.me/5584988797773" style="color: #667eea; text-decoration: none; font-weight: 500;">
                                    +55 (84) 98879-7773
                                </a>
                            </p>
                            
                            <p style="margin: 15px 0 10px 0; font-size: 15px; color: #555555; line-height: 1.8;">
                                <strong>Horário:</strong><br>
                                Segunda a Sábado: 8h às 19h30
                            </p>
                            
                            <p style="margin: 15px 0 0 0; font-size: 15px; color: #555555; line-height: 1.8;">
                                <strong>Instagram:</strong><br>
                                <a href="https://instagram.com/sbnagaragem" style="color: #667eea; text-decoration: none; font-weight: 500;">
                                    @sbnagaragem
                                </a>
                            </p>
                        </div>
                        
                        <!-- CTA -->
                        <table width="100%" cellpadding="0" cellspacing="0" border="0" style="margin: 30px 0;">
                            <tr>
                                <td align="center">
                                    <a href="https://wa.me/5584988797773?text=Ol%C3%A1!%20Acabei%20de%20me%20cadastrar%20e%20gostaria%20de%20agendar%20meu%20primeiro%20corte!" 
                                       style="display: inline-block; padding: 16px 40px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #ffffff; text-decoration: none; border-radius: 6px; font-size: 16px; font-weight: 600; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);">
                                        Agende seu primeiro corte
                                    </a>
                                </td>
                            </tr>
                        </table>
                        
                        <p style="margin: 30px 0 0 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            Estamos ansiosos para te atender!
                        </p>
                        
                        <p style="margin: 15px 0 0 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            Abraço forte,<br>
                            <strong>Equipe """ + barbershopName + """
</strong>
                        </p>
                    </td>
                </tr>
                
                <!-- Footer -->
                <tr>
                    <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-radius: 0 0 8px 8px; border-top: 1px solid #e9ecef;">
                        <p style="margin: 0 0 10px 0; font-size: 13px; color: #6c757d;">
                            Siga a gente no Instagram: 
                            <a href="https://instagram.com/sbnagaragem" style="color: #667eea; text-decoration: none; font-weight: 600;">
                                @sbnagaragem
                            </a>
                        </p>
                        <p style="margin: 10px 0 0 0; font-size: 12px; color: #6c757d;">
                            R. Carmindo Quadros, 46 - Nova Parnamirim, Parnamirim - RN
                        </p>
                        <p style="margin: 5px 0 0 0; font-size: 12px; color: #6c757d;">
                            """ + barbershopName + """
 © 2024 - Todos os direitos reservados
                        </p>
                    </td>
                </tr>
                
            </table>
        </td>
    </tr>
</table>
</body>
</html>
""";
    }

    private String buildMonthlyReportHtml(final YearMonth reportMonth) {
        final String monthYear = formatMonthYear(reportMonth);

        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Relatório Mensal</title>
</head>
<body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
<table width="100%" cellpadding="0" cellspacing="0" border="0" style="background-color: #f4f4f4; padding: 40px 0;">
    <tr>
        <td align="center">
            <table width="600" cellpadding="0" cellspacing="0" border="0" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                
                <!-- Header -->
                <tr>
                    <td style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px 30px; border-radius: 8px 8px 0 0;">
                        <h1 style="margin: 0; color: #ffffff; font-size: 28px; text-align: center; font-weight: 600;">
                            Relatório Mensal
                        </h1>
                        <p style="margin: 10px 0 0 0; color: #ffffff; font-size: 16px; text-align: center; opacity: 0.9;">
                            """ + monthYear + """
                        </p>
                    </td>
                </tr>
                
                <!-- Body -->
                <tr>
                    <td style="padding: 40px 30px;">
                        <p style="margin: 0 0 20px 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            Olá! 👋
                        </p>
                        
                        <p style="margin: 0 0 20px 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            O relatório mensal de <strong>""" + barbershopName + """
</strong> está pronto!
                        </p>
                        
                        <p style="margin: 0 0 30px 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            Este documento contém uma análise completa do desempenho do mês, incluindo:
                        </p>
                        
                        <ul style="margin: 0 0 30px 0; padding-left: 20px; color: #333333; font-size: 15px; line-height: 1.8;">
                            <li>Análise de receitas e crescimento</li>
                            <li>Desempenho detalhado de cada barbeiro</li>
                            <li>Estatísticas de atendimentos e novos clientes</li>
                            <li>Insights estratégicos para otimizar a agenda</li>
                            <li>Recomendações de promoções para dias fracos</li>
                        </ul>
                        
                        <div style="background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 30px 0; border-radius: 4px;">
                            <p style="margin: 0; font-size: 14px; color: #555555; line-height: 1.6;">
                                <strong>Dica:</strong> Use os insights do relatório para planejar promoções estratégicas e otimizar sua equipe nos dias de menor movimento.
                            </p>
                        </div>
                        
                        <p style="margin: 30px 0 0 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            O PDF está anexado neste email.
                        </p>
                        
                        <p style="margin: 20px 0 0 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            Qualquer dúvida, estamos à disposição!
                        </p>
                        
                        <p style="margin: 10px 0 0 0; font-size: 16px; color: #333333; line-height: 1.6;">
                            Abraço,<br>
                            <strong>Equipe """ + barbershopName + """
</strong>
                        </p>
                    </td>
                </tr>
                
                <!-- Footer -->
                <tr>
                    <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-radius: 0 0 8px 8px; border-top: 1px solid #e9ecef;">
                        <p style="margin: 0 0 10px 0; font-size: 13px; color: #6c757d;">
                            Este é um email automático enviado no primeiro dia de cada mês.
                        </p>
                        <p style="margin: 10px 0 0 0; font-size: 13px; color: #6c757d;">
                            """ + barbershopName + """
 - Sistema de Fidelidade
                        </p>
                        <p style="margin: 10px 0 0 0; font-size: 12px; color: #6c757d;">
                            R. Carmindo Quadros, 46 - Nova Parnamirim, Parnamirim - RN
                        </p>
                    </td>
                </tr>
                
            </table>
        </td>
    </tr>
</table>
</body>
</html>
""";
    }
}