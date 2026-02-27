package app.system.fidelity.messaging.scheduler;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.messaging.SendMonthlyReportEmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyReportScheduler {

    private final SendMonthlyReportEmailPort sendMonthlyReportEmailPort;

    /**
     * Executa todo dia 1º de cada mês às 07:00 (timezone America/Fortaleza)
     * Cron: segundo minuto hora dia mês dia-da-semana
     * 0 0 7 1 * * = às 07:00 do dia 1 de todo mês
     */
    @Scheduled(cron = "0 0 7 1 * *", zone = "America/Fortaleza")
    public void sendMonthlyReport() {
        final LocalDateTime now = LocalDateTime.now(ZoneId.of("America/Fortaleza"));
        log.info("Iniciando envio automático de relatório mensal - {}", now);

        try {
            final Context context = new Context();
            sendMonthlyReportEmailPort.execute(context);

            log.info("Envio automático de relatório mensal concluído com sucesso");

        } catch (Exception e) {
            log.error("Erro no envio automático de relatório mensal", e);
        }
    }
}