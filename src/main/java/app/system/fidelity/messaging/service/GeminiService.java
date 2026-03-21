package app.system.fidelity.messaging.service;

import app.system.fidelity.domain.MonthlyReport;
import app.system.fidelity.messaging.service.MonthlyReportDataAggregator.AggregatedReportData;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiService {

    private final Client client;
    private final MonthlyReportDataAggregator dataAggregator;

    public GeminiService(final MonthlyReportDataAggregator dataAggregator) {
        this.client = new Client();
        this.dataAggregator = dataAggregator;
    }

    public String generateInsights(final MonthlyReport report) {
        try {
            log.info("Gerando insights com contexto completo para: {}", report.getReportMonth());

            final AggregatedReportData aggregatedData = dataAggregator.aggregateDataForMonth(report.getReportMonth());

            final String prompt = buildContextualPrompt(report, aggregatedData);

            final GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.0-flash-exp",
                    prompt,
                    null
            );

            final String insights = response.text();
            log.info("Insights contextuais gerados com sucesso");

            return insights;

        } catch (Exception e) {
            log.error("Erro ao gerar insights: {}", e.getMessage(), e);
            return null;
        }
    }

    private String buildContextualPrompt(
            final MonthlyReport report,
            final AggregatedReportData data
    ) {
        return String.format("""
                Você é um consultor estratégico especializado em gestão de barbearias. Analise os dados abaixo e gere insights PROFUNDOS, ESPECÍFICOS e ACIONÁVEIS.
                
                **IMPORTANTE**: Você tem acesso ao histórico completo da barbearia. Use esses dados para identificar padrões, tendências e anomalias que só ficam visíveis com contexto temporal.
                
                
                --- CONTEXTO DA BARBEARIA
               
                
                **Perfil:**
                - Barbearia pequena/média com %d barbeiro(s) ativo(s)
                - Abre apenas de segunda a sábado
                - Base total de clientes: %d
                - Tendência geral do negócio: %s
                - Negócio está crescendo? %s
                - Negócio está estável? %s
                
                **Histórico de Performance (Últimos 6 Meses):**
                %s
                
                
                --- DADOS DO MÊS ATUAL: %s
               
                
                **Performance Financeira:**
                - Receita Total: R$ %s
                - Receita Mês Anterior: R$ %s
                - Crescimento Absoluto: R$ %s
                - Crescimento Percentual: %s%%
                - Receita de Serviços: R$ %s (%s%% do total)
                - Receita de Produtos: R$ %s (%s%% do total)
                
                **Performance Operacional:**
                - Total de Atendimentos: %d (vs %d no mês anterior)
                - Ticket Médio: R$ %s (vs R$ %s no mês anterior)
                - Novos Clientes: %d (vs %d no mês anterior)
                
                **Clientes:**
                - Clientes ativos este mês: %d (de %d na base total = %s%% de ativação)
                - Novos clientes: %d
                - Clientes recorrentes: %d
                - Clientes em risco de churn (>60 dias sem aparecer): %d
                - Frequência média de visitas por cliente: %.2f vezes/mês
                - Top 5 clientes gastaram: R$ %s (representa %s%% da receita)
                
                **Performance por Dia da Semana:**
                %s
                
                **Performance dos Barbeiros:**
                %s
                
                %s
                
               
                --- INSTRUÇÕES PARA ANÁLISE
               
                
                **1. ANÁLISE CONTEXTUAL (use o histórico!):**
                - Compare o mês atual com os 6 meses anteriores
                - Identifique se o resultado é esperado baseado na tendência
                - Detecte anomalias (resultados muito acima ou abaixo do padrão)
                - Identifique sazonalidades (ex: dezembro sempre é melhor?)
                
                **2. ANÁLISE DE CLIENTES:**
                - Avalie a taxa de ativação da base (quantos %% dos clientes estão ativos)
                - Analise o risco de churn e sugira ações preventivas ESPECÍFICAS
                - Avalie se a frequência de visitas é saudável
                - Analise a dependência dos top clientes (risco se eles saírem)
                
                **3. ANÁLISE OPERACIONAL:**
                - Identifique dias da semana com oportunidades de melhoria
                - Compare ticket médio com histórico e identifique causas de variação
                - Avalie a relação serviços vs produtos (está equilibrada?)
                
                **4. ANÁLISE DE EQUIPE:**
                - Identifique barbeiros com queda de performance (compare com histórico deles)
                - Destaque barbeiros que estão melhorando
                - Sugira ações específicas para equalizar performance
                
                **5. INSIGHTS ACIONÁVEIS:**
                - Seja ESPECÍFICO nos números ("A receita de produtos representa apenas 15%% do total")
                - Seja GENÉRICO nas sugestões ("Crie estratégias no sentido K, J, L para aumentar venda de produtos nos dias X, Y, Z. Por causa de A, B, C" - NÃO diga "Faça promoção 20%% off")
                - Priorize 3-4 ações de maior impacto
                
             
                --- FORMATO DA RESPOSTA
                
                
                Retorne APENAS parágrafos corridos (sem títulos, sem seções, sem bullet points).
                Máximo de 6 parágrafos curtos e diretos.
                Tom: consultivo, natural, como se estivesse conversando com o dono.
                
                Comece direto com a análise. Exemplo de início:
                "Analisando o histórico dos últimos 6 meses, este foi o segundo melhor mês do ano, mas..."
                """,
                data.getBarberInsights().getTotalActiveBarbers(),
                data.getCustomerInsights().getTotalCustomersInBase(),
                translateTrend(data.getBusinessTrends().getTrend()),
                data.getBusinessTrends().getIsGrowing() ? "SIM" : "NÃO",
                data.getBusinessTrends().getIsStable() ? "SIM" : "NÃO",

                formatHistoricalData(data),

                formatMonthYear(report.getReportMonth()),

                formatCurrency(report.getTotalRevenue()),
                formatCurrency(report.getPreviousMonthRevenue()),
                formatCurrency(report.getRevenueGrowthAbsolute()),
                formatPercentage(report.getRevenueGrowthPercentage()),
                formatCurrency(report.getServicesRevenue()),
                calculatePercentage(report.getServicesRevenue(), report.getTotalRevenue()),
                formatCurrency(report.getProductsRevenue()),
                calculatePercentage(report.getProductsRevenue(), report.getTotalRevenue()),

                report.getTotalAppointments(),
                report.getPreviousMonthAppointments(),
                formatCurrency(report.getAverageTicket()),
                formatCurrency(report.getPreviousMonthAverageTicket()),
                report.getNewCustomers(),
                report.getPreviousMonthNewCustomers(),

                data.getCustomerInsights().getActiveCustomersInMonth(),
                data.getCustomerInsights().getTotalCustomersInBase(),
                calculatePercentage(
                        BigDecimal.valueOf(data.getCustomerInsights().getActiveCustomersInMonth()),
                        BigDecimal.valueOf(data.getCustomerInsights().getTotalCustomersInBase())
                ),
                data.getCustomerInsights().getNewCustomersInMonth(),
                data.getCustomerInsights().getReturningCustomersInMonth(),
                data.getCustomerInsights().getChurnedCustomers(),
                data.getCustomerInsights().getAverageVisitsPerCustomer(),
                formatTopCustomersRevenue(data.getCustomerInsights().getTopCustomersInMonth()),
                calculateTopCustomersPercentage(data.getCustomerInsights().getTopCustomersInMonth(), report.getTotalRevenue()),

                formatWeekdayStats(data.getCurrentMonthData().getWeekdayStats()),

                formatBarberPerformance(data.getBarberInsights()),

                formatDecliningBarbers(data.getBarberInsights())
        );
    }

    private String formatHistoricalData(final AggregatedReportData data) {
        return data.getHistoricalMonths().stream()
                .map(m -> String.format("%s/%d: R$ %s (%d atendimentos, %d novos clientes)%s",
                        m.getMonth().getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")),
                        m.getMonth().getYear(),
                        formatCurrency(m.getRevenue()),
                        m.getAppointments(),
                        m.getNewCustomers(),
                        m.getIsTargetMonth() ? " ← MÊS ATUAL" : ""
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatWeekdayStats(final java.util.Map<String, MonthlyReportDataAggregator.WeekdayStats> stats) {
        if (stats == null || stats.isEmpty()) {
            return "Sem dados de dias da semana";
        }

        return stats.entrySet().stream()
                .map(e -> String.format("- %s: R$ %s (%d atendimentos, ticket médio R$ %s)",
                        translateWeekday(e.getKey()),
                        formatCurrency(e.getValue().getTotalRevenue()),
                        e.getValue().getAppointments(),
                        formatCurrency(e.getValue().getAverageTicket())
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatBarberPerformance(final MonthlyReportDataAggregator.BarberInsightsData data) {
        if (data.getBarberDetails().isEmpty()) {
            return "Sem dados de barbeiros";
        }

        return data.getBarberDetails().stream()
                .map(b -> {
                    final String comparison = b.getHistoricalAverageTicket().compareTo(BigDecimal.ZERO) > 0
                            ? String.format("(histórico: R$ %s)", formatCurrency(b.getHistoricalAverageTicket()))
                            : "(novo barbeiro)";

                    return String.format("- %s: R$ %s em receita, %d atendimentos, ticket médio R$ %s %s",
                            b.getBarberName(),
                            formatCurrency(b.getRevenue()),
                            b.getAppointments(),
                            formatCurrency(b.getAverageTicket()),
                            comparison
                    );
                })
                .collect(Collectors.joining("\n"));
    }

    private String formatDecliningBarbers(final MonthlyReportDataAggregator.BarberInsightsData data) {
        if (data.getDecliningPerformanceBarbers().isEmpty()) {
            return "";
        }

        return String.format("**ATENÇÃO - Barbeiros com queda de performance:**\n%s\n",
                String.join(", ", data.getDecliningPerformanceBarbers())
        );
    }

    private String formatTopCustomersRevenue(final java.util.List<MonthlyReportDataAggregator.TopCustomerInMonth> customers) {
        if (customers == null || customers.isEmpty()) {
            return "0,00";
        }

        final BigDecimal total = customers.stream()
                .map(MonthlyReportDataAggregator.TopCustomerInMonth::getTotalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return formatCurrency(total);
    }

    private String calculateTopCustomersPercentage(
            final java.util.List<MonthlyReportDataAggregator.TopCustomerInMonth> customers,
            final BigDecimal totalRevenue
    ) {
        if (customers == null || customers.isEmpty() || totalRevenue.compareTo(BigDecimal.ZERO) == 0) {
            return "0,00";
        }

        final BigDecimal topRevenue = customers.stream()
                .map(MonthlyReportDataAggregator.TopCustomerInMonth::getTotalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return calculatePercentage(topRevenue, totalRevenue);
    }

    private String translateTrend(final String trend) {
        return switch (trend) {
            case "STRONG_GROWTH" -> "CRESCIMENTO FORTE";
            case "MODERATE_GROWTH" -> "CRESCIMENTO MODERADO";
            case "DECLINING" -> "EM QUEDA";
            case "STABLE" -> "ESTÁVEL";
            default -> "DADOS INSUFICIENTES";
        };
    }

    private String translateWeekday(final String weekday) {
        return switch (weekday) {
            case "MONDAY" -> "Segunda";
            case "TUESDAY" -> "Terça";
            case "WEDNESDAY" -> "Quarta";
            case "THURSDAY" -> "Quinta";
            case "FRIDAY" -> "Sexta";
            case "SATURDAY" -> "Sábado";
            case "SUNDAY" -> "Domingo";
            default -> weekday;
        };
    }

    private String formatMonthYear(final java.time.YearMonth yearMonth) {
        final String monthName = yearMonth.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        return String.format("%s de %d",
                monthName.substring(0, 1).toUpperCase() + monthName.substring(1),
                yearMonth.getYear());
    }

    private String formatCurrency(final BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%,.2f", value);
    }

    private String formatPercentage(final BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%.2f", value);
    }

    private String calculatePercentage(final BigDecimal part, final BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return "0,00";
        return String.format("%.1f",
                part.multiply(BigDecimal.valueOf(100))
                        .divide(total, 2, java.math.RoundingMode.HALF_UP)
                        .doubleValue()
        );
    }
}