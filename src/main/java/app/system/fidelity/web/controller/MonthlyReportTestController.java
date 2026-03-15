package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.messaging.GenerateMonthlyReportPort;
import app.system.fidelity.core.messaging.SendMonthlyReportEmailPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.MonthlyReport;
import app.system.fidelity.domain.User;
import app.system.fidelity.messaging.service.EmailService;
import app.system.fidelity.messaging.service.GeminiService;
import app.system.fidelity.messaging.service.PdfGeneratorService;
import app.system.fidelity.web.commons.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test/monthly-report")
@RequiredArgsConstructor
public class MonthlyReportTestController {

    private final GenerateMonthlyReportPort generateMonthlyReportPort;
    private final GeminiService geminiService;
    private final PdfGeneratorService pdfGeneratorService;
    private final EmailService emailService;
    private final UserRepositoryPort userRepository;
    private final SendMonthlyReportEmailPort sendMonthlyReportEmailPort;

    /**
     * Teste completo: Gera relatório + PDF + Insights IA + Envia email para todos admins
     *
     * GET http://localhost:8080/api/test/monthly-report/send-full
     *
     * Este é o teste mais completo - igual ao que o cronjob faz.
     */
    @GetMapping("/send-full")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendFullReport() {
        try {
            // Buscar admins
            final List<String> adminEmails = userRepository.findAllAdmins()
                    .stream()
                    .map(User::getEmail)
                    .toList();

            if (adminEmails.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("Nenhum administrador encontrado no sistema"));
            }

            sendMonthlyReportEmailPort.execute(new Context());

            final Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("admins_count", adminEmails.size());
            result.put("admins_emails", adminEmails);
            result.put("message", "Relatório com insights de IA enviado com sucesso!");

            return ResponseEntity.ok(ApiResponse.success(result,
                    "Relatório mensal com insights enviado para " + adminEmails.size() + " administrador(es)"));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Erro ao enviar relatório: " + e.getMessage()));
        }
    }

    /**
     * Teste: Envia email para um email específico (não precisa ser admin)
     *
     * POST http://localhost:8080/api/test/monthly-report/send-to-email
     * Body: { "email": "seu-email@teste.com" }
     *
     * Útil para testar sem criar usuários admin.
     */
    @PostMapping("/send-to-email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendToSpecificEmail(
            @RequestBody Map<String, String> request
    ) {
        try {
            final String email = request.get("email");

            if (email == null || email.isBlank()) {
                return ResponseEntity.ok(ApiResponse.error("Por favor, informe o email"));
            }

            final MonthlyReport report = generateMonthlyReportPort.execute(new Context());

            String aiInsights = null;
            try {
                aiInsights = geminiService.generateInsights(report);
            } catch (Exception e) {
                // Continua sem insights se falhar
            }

            final byte[] pdfContent = pdfGeneratorService.generateMonthlyReportPdf(report);

            emailService.sendMonthlyReportEmail(
                    List.of(email),
                    pdfContent,
                    report.getReportMonth(),
                    aiInsights,
                    report.getTotalRevenue(),
                    report.getRevenueGrowthPercentage(),
                    report.getTotalAppointments(),
                    report.getAverageTicket(),
                    report.getNewCustomers()
            );

            final Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("email", email);
            result.put("report_month", report.getReportMonth().toString());
            result.put("ai_insights_generated", aiInsights != null);
            result.put("message", "Email com insights enviado com sucesso!");

            return ResponseEntity.ok(ApiResponse.success(result,
                    "Relatório enviado para: " + email));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Erro ao enviar email: " + e.getMessage()));
        }
    }

    /**
     * Teste: Apenas gera e visualiza os insights da IA (sem enviar email)
     *
     * GET http://localhost:8080/api/test/monthly-report/preview-insights
     *
     * Útil para testar se a IA está gerando insights corretamente.
     */
    @GetMapping("/preview-insights")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewInsights() {
        try {
            final MonthlyReport report = generateMonthlyReportPort.execute(new Context());

            final String insights = geminiService.generateInsights(report);

            final Map<String, Object> result = new HashMap<>();
            result.put("report_month", report.getReportMonth().toString());
            result.put("insights", insights);
            result.put("total_revenue", report.getTotalRevenue());
            result.put("growth_percentage", report.getRevenueGrowthPercentage());

            return ResponseEntity.ok(ApiResponse.success(result,
                    "Insights gerados com sucesso pela IA"));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Erro ao gerar insights: " + e.getMessage()));
        }
    }

    /**
     * Teste: Apenas visualiza os dados do relatório (sem gerar PDF, insights ou enviar email)
     *
     * GET http://localhost:8080/api/test/monthly-report/preview-data
     *
     * Útil para verificar se os cálculos estão corretos.
     */
    @GetMapping("/preview-data")
    public ResponseEntity<ApiResponse<MonthlyReport>> previewReportData() {
        try {
            final MonthlyReport report = generateMonthlyReportPort.execute(new Context());

            return ResponseEntity.ok(ApiResponse.success(report,
                    "Dados do relatório gerados com sucesso"));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Erro ao gerar dados: " + e.getMessage()));
        }
    }

    /**
     * Teste: Baixa o PDF diretamente no navegador (sem enviar email)
     *
     * GET http://localhost:8080/api/test/monthly-report/download-pdf
     *
     * O PDF será baixado automaticamente.
     */
    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadPdf() {
        try {
            final MonthlyReport report = generateMonthlyReportPort.execute(new Context());

            final byte[] pdfContent = pdfGeneratorService.generateMonthlyReportPdf(report);

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    String.format("relatorio-mensal-%s-%d.pdf",
                            report.getReportMonth().getMonth().toString().toLowerCase(),
                            report.getReportMonth().getYear()));

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(pdfContent);

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(null);
        }
    }

    /**
     * Teste: Verifica configurações e destinatários
     *
     * GET http://localhost:8080/api/test/monthly-report/check-config
     *
     * Verifica se está tudo configurado corretamente.
     */
    @GetMapping("/check-config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkConfiguration() {
        try {
            final List<User> admins = userRepository.findAllAdmins();

            final Map<String, Object> config = new HashMap<>();
            config.put("admins_count", admins.size());
            config.put("admins", admins.stream()
                    .map(admin -> Map.of(
                            "id", admin.getId(),
                            "name", admin.getName(),
                            "email", admin.getEmail()
                    ))
                    .toList());

            if (admins.isEmpty()) {
                config.put("warning", "Nenhum administrador encontrado! Cadastre pelo menos um admin.");
            }

            return ResponseEntity.ok(ApiResponse.success(config,
                    "Configuração verificada"));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Erro ao verificar configuração: " + e.getMessage()));
        }
    }

    /**
     * Informações sobre os endpoints disponíveis
     *
     * GET http://localhost:8080/api/test/monthly-report/help
     */
    @GetMapping("/help")
    public ResponseEntity<ApiResponse<Map<String, String>>> help() {
        final Map<String, String> endpoints = new HashMap<>();

        endpoints.put("GET /test/monthly-report/send-full",
                "Teste completo: Envia email com insights de IA para TODOS os admins (igual ao cronjob)");

        endpoints.put("POST /test/monthly-report/send-to-email",
                "Envia com insights de IA para um email específico (Body: {\"email\": \"teste@email.com\"})");

        endpoints.put("GET /test/monthly-report/preview-insights",
                "Gera e visualiza apenas os insights da IA (sem enviar email)");

        endpoints.put("GET /test/monthly-report/preview-data",
                "Apenas visualiza os dados do relatório (JSON)");

        endpoints.put("GET /test/monthly-report/download-pdf",
                "Baixa o PDF diretamente (sem enviar email)");

        endpoints.put("GET /test/monthly-report/check-config",
                "Verifica configurações e lista admins");

        endpoints.put("GET /test/monthly-report/help",
                "Esta mensagem de ajuda");

        return ResponseEntity.ok(ApiResponse.success(endpoints,
                "Endpoints de teste disponíveis - Agora com IA! 🚀"));
    }
}