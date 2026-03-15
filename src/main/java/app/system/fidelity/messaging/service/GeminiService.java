package app.system.fidelity.messaging.service;

import app.system.fidelity.domain.MonthlyReport;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeminiService {

    private final Client client;

    public GeminiService() {
        this.client = new Client();
    }

    public String generateInsights(final MonthlyReport report) {
        try {
            log.info("Gerando insights com Gemini para o mês: {}", report.getReportMonth());

            final String prompt = buildPrompt(report);

            final GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.0-flash-exp",
                    prompt,
                    null
            );

            final String insights = response.text();

            log.info("Insights gerados com sucesso");
            return insights;

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