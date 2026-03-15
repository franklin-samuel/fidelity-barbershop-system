package app.system.fidelity.messaging.service;

import app.system.fidelity.domain.MonthlyReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent";

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateInsights(final MonthlyReport report) {
        try {
            log.info("Gerando insights com Gemini para o mês: {}", report.getReportMonth());

            final String prompt = buildPrompt(report);
            final String response = callGeminiAPI(prompt);

            log.info("Insights gerados com sucesso");
            return response;

        } catch (Exception e) {
            log.error("Erro ao gerar insights com Gemini: {}", e.getMessage(), e);
            return null;
        }
    }

    private String buildPrompt(final MonthlyReport report) {
        return String.format("""
                Você é um consultor especializado em gestão de barbearias. Analise os dados abaixo e gere insights estratégicos OBJETIVOS e DIRETOS.
                
                **CONTEXTO:**
                - Barbearia pequena/média com até 3 barbeiros
                - Relatório referente a %s
                
                **DADOS DO MÊS ATUAL:**
                - Receita Total: R$ %s
                - Receita Mês Anterior: R$ %s
                - Crescimento: R$ %s (%s%%)
                - Receita de Serviços: R$ %s
                - Receita de Produtos: R$ %s
                - Total de Atendimentos: %d
                - Atendimentos Mês Anterior: %d
                - Ticket Médio: R$ %s
                - Ticket Médio Anterior: R$ %s
                - Novos Clientes: %d
                - Novos Clientes Mês Anterior: %d
                
                **DESEMPENHO DOS BARBEIROS:**
                %s
                
                **DIAS FRACOS IDENTIFICADOS:**
                %s
                
                **INSTRUÇÕES:**
                1. Analise os dados de forma profunda e estratégica
                2. Identifique padrões, tendências e oportunidades
                3. Seja ESPECÍFICO nos números e observações (ex: "A receita de produtos representa apenas 15%% do total")
                4. Seja GENÉRICO nas sugestões (ex: "Crie estratégias para aumentar a venda de produtos" ao invés de "Faça promoção de 20%% off")
                5. Foque em 4 áreas: Faturamento, Retenção de Clientes, Otimização da Equipe, Marketing
                6. Use um tom consultivo e natural, como se estivesse conversando com o dono
                7. Seja direto e objetivo - evite enrolação
                
                **FORMATO DA RESPOSTA:**
                Retorne APENAS o texto dos insights em parágrafos corridos, sem títulos ou seções. Comece direto com a análise.
                Máximo de 6 parágrafos curtos e objetivos.
                """,
                formatMonthYear(report),
                formatCurrency(report.getTotalRevenue()),
                formatCurrency(report.getPreviousMonthRevenue()),
                formatCurrency(report.getRevenueGrowthAbsolute()),
                formatPercentage(report.getRevenueGrowthPercentage()),
                formatCurrency(report.getServicesRevenue()),
                formatCurrency(report.getProductsRevenue()),
                report.getTotalAppointments(),
                report.getPreviousMonthAppointments(),
                formatCurrency(report.getAverageTicket()),
                formatCurrency(report.getPreviousMonthAverageTicket()),
                report.getNewCustomers(),
                report.getPreviousMonthNewCustomers(),
                formatBarbersPerformance(report),
                formatWeakDaysInsights(report)
        );
    }

    private String callGeminiAPI(final String prompt) {
        try {
            final String url = GEMINI_API_URL + "?key=" + apiKey;

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            final Map<String, Object> requestBody = new HashMap<>();

            final Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(Map.of("text", prompt)));

            requestBody.put("contents", List.of(content));

            final Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1000);
            requestBody.put("generationConfig", generationConfig);

            final HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            final ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            }

            throw new RuntimeException("Resposta inválida da API Gemini");

        } catch (Exception e) {
            log.error("Erro na chamada da API Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao chamar API Gemini", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(final Map<String, Object> response) {
        try {
            final List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("Nenhum candidate na resposta");
            }

            final Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            final List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("Nenhum part na resposta");
            }

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Erro ao extrair texto da resposta: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao extrair texto da resposta Gemini", e);
        }
    }

    private String formatMonthYear(final MonthlyReport report) {
        return String.format("%s/%d",
                report.getReportMonth().getMonth().toString(),
                report.getReportMonth().getYear()
        );
    }

    private String formatCurrency(final java.math.BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%,.2f", value);
    }

    private String formatPercentage(final java.math.BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%.2f", value);
    }

    private String formatBarbersPerformance(final MonthlyReport report) {
        if (report.getBarbersPerformance() == null || report.getBarbersPerformance().isEmpty()) {
            return "Nenhum dado de barbeiros disponível";
        }

        final StringBuilder sb = new StringBuilder();
        report.getBarbersPerformance().forEach(barber -> {
            sb.append(String.format("- %s: R$ %s em receita, %d atendimentos, ticket médio R$ %s\n",
                    barber.getBarberName(),
                    formatCurrency(barber.getTotalRevenue()),
                    barber.getAppointmentsCount(),
                    formatCurrency(barber.getAverageTicket())
            ));
        });

        return sb.toString().trim();
    }

    private String formatWeakDaysInsights(final MonthlyReport report) {
        if (report.getWeakDaysInsights() == null || report.getWeakDaysInsights().isEmpty()) {
            return "Nenhum dia fraco identificado - todos os dias tiveram desempenho satisfatório";
        }

        return String.join("\n", report.getWeakDaysInsights());
    }
}