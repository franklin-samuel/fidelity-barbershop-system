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
        templateResolver.setCacheable(true);

        final TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);

        return engine;
    }

    /**
     * Processa um template HTML com as variáveis fornecidas
     *
     * @param templateName Nome do template (sem .html)
     * @param variables Mapa de variáveis para o template
     * @return HTML processado
     */
    public String processTemplate(final String templateName, final Map<String, Object> variables) {
        try {
            final Context context = new Context();
            context.setVariables(variables);

            return templateEngine.process(templateName, context);

        } catch (Exception e) {
            log.error("Erro ao processar template {}: {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Falha ao processar template de email", e);
        }
    }
}