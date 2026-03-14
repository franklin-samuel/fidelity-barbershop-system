package app.system.fidelity.messaging.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

@Slf4j
@Service
public class TemplateService {

    private final TemplateEngine templateEngine;

    public TemplateService() {
        this.templateEngine = createTemplateEngine();
    }

    private TemplateEngine createTemplateEngine() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);
        templateResolver.setCheckExistence(true);

        final TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);

        return engine;
    }

    public String processTemplate(final String templateName, final Map<String, Object> variables) {
        try {
            log.info("Tentando processar template: {}", templateName);
            log.info("Variáveis recebidas: {}", variables);

            final Context context = new Context();
            context.setVariables(variables);

            final String result = templateEngine.process(templateName, context);

            log.info("Template processado com sucesso! Tamanho do HTML: {} caracteres", result.length());
            log.debug("HTML gerado (primeiros 200 chars): {}", result.substring(0, Math.min(200, result.length())));

            return result;

        } catch (Exception e) {
            log.error("ERRO ao processar template {}: {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Falha ao processar template de email: " + templateName, e);
        }
    }
}