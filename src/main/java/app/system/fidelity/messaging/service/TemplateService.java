package app.system.fidelity.messaging.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Slf4j
@Service
public class TemplateService {

    private final SpringTemplateEngine templateEngine;

    public TemplateService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String processTemplate(final String templateName, final Map<String, Object> variables) {
        try {
            log.info("Tentando processar template: {}", templateName);
            log.info("Variáveis recebidas: {}", variables);

            final Context context = new Context();
            context.setVariables(variables);

            final String result = templateEngine.process(templateName, context);

            log.info("Template processado com sucesso! Tamanho do HTML: {} caracteres", result.length());

            return result;

        } catch (Exception e) {
            log.error("ERRO ao processar template {}: {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Falha ao processar template de email: " + templateName, e);
        }
    }

    public String formatInsightsToHtml(final String insights) {
        if (insights == null || insights.isBlank()) {
            return null;
        }

        final String[] paragraphs = insights.split("\n\n");
        final StringBuilder html = new StringBuilder();

        for (final String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                continue;
            }
            html.append("<p class=\"insights-text\">")
                    .append(paragraph.trim().replace("\n", "<br>"))
                    .append("</p>\n");
        }

        return html.toString();
    }
}