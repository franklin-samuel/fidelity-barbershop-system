package app.system.fidelity.messaging.adapter;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.messaging.GenerateMonthlyReportPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GenerateMonthlyReportAdapter implements GenerateMonthlyReportPort {

    private final AppointmentRepositoryPort appointmentRepository;
    private final CustomerRepositoryPort customerRepository;
    private final UserRepositoryPort userRepository;

    @Override
    public MonthlyReport execute(final Context context) {
        final YearMonth reportMonth = YearMonth.now().minusMonths(1);
        final YearMonth previousMonth = reportMonth.minusMonths(1);

        final LocalDateTime reportStart = reportMonth.atDay(1).atStartOfDay();
        final LocalDateTime reportEnd = reportMonth.atEndOfMonth().atTime(23, 59, 59);

        final LocalDateTime previousStart = previousMonth.atDay(1).atStartOfDay();
        final LocalDateTime previousEnd = previousMonth.atEndOfMonth().atTime(23, 59, 59);

        final BigDecimal totalRevenue = appointmentRepository.sumBarbershopRevenueBetween(reportStart, reportEnd);
        final BigDecimal previousMonthRevenue = appointmentRepository.sumBarbershopRevenueBetween(previousStart, previousEnd);
        final BigDecimal revenueGrowthAbsolute = totalRevenue.subtract(previousMonthRevenue);
        final BigDecimal revenueGrowthPercentage = calculateGrowthPercentage(previousMonthRevenue, totalRevenue);

        final BigDecimal servicesRevenue = appointmentRepository.sumServicesRevenueBetween(reportStart, reportEnd);
        final BigDecimal productsRevenue = appointmentRepository.sumProductsRevenueBetween(reportStart, reportEnd);

        final long totalAppointments = appointmentRepository.countWithCustomerBetween(reportStart, reportEnd);
        final long previousMonthAppointments = appointmentRepository.countWithCustomerBetween(previousStart, previousEnd);

        final long newCustomers = customerRepository.countByCreatedAtBetween(reportStart, reportEnd);
        final long previousMonthNewCustomers = customerRepository.countByCreatedAtBetween(previousStart, previousEnd);

        final BigDecimal averageTicket = totalAppointments > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalAppointments), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final BigDecimal previousMonthAverageTicket = previousMonthAppointments > 0
                ? previousMonthRevenue.divide(BigDecimal.valueOf(previousMonthAppointments), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final List<BarberMonthlyPerformance> barbersPerformance = buildBarbersPerformance(reportStart, reportEnd);

        final List<String> weakDaysInsights = generateWeakDaysInsights(reportStart, reportEnd);

        return MonthlyReport.builder()
                .reportMonth(reportMonth)
                .previousMonth(previousMonth)
                .totalRevenue(totalRevenue)
                .previousMonthRevenue(previousMonthRevenue)
                .revenueGrowthAbsolute(revenueGrowthAbsolute)
                .revenueGrowthPercentage(revenueGrowthPercentage)
                .servicesRevenue(servicesRevenue)
                .productsRevenue(productsRevenue)
                .totalAppointments(totalAppointments)
                .previousMonthAppointments(previousMonthAppointments)
                .newCustomers(newCustomers)
                .previousMonthNewCustomers(previousMonthNewCustomers)
                .averageTicket(averageTicket)
                .previousMonthAverageTicket(previousMonthAverageTicket)
                .barbersPerformance(barbersPerformance)
                .weakDaysInsights(weakDaysInsights)
                .build();
    }

    private List<BarberMonthlyPerformance> buildBarbersPerformance(
            final LocalDateTime start,
            final LocalDateTime end
    ) {
        final List<BarberRevenueSummary> summaries = appointmentRepository.findTopBarbersByRevenueBetween(
                start,
                end
        );

        if (summaries.isEmpty()) {
            return List.of();
        }

        final Set<UUID> barberIds = summaries.stream()
                .map(BarberRevenueSummary::barberId)
                .collect(Collectors.toSet());

        final Map<UUID, String> barberNames = userRepository.findAllById(barberIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        return summaries.stream()
                .map(s -> {
                    final BigDecimal avgTicket = s.appointmentsCount() > 0
                            ? s.revenue().divide(BigDecimal.valueOf(s.appointmentsCount()), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return BarberMonthlyPerformance.builder()
                            .barberName(barberNames.getOrDefault(s.barberId(), "Barbeiro Desconhecido"))
                            .totalRevenue(s.revenue())
                            .appointmentsCount(s.appointmentsCount())
                            .averageTicket(avgTicket)
                            .build();
                })
                .toList();
    }

    private List<String> generateWeakDaysInsights(final LocalDateTime start, final LocalDateTime end) {
        final List<WeekdayRevenue> weekdayData = appointmentRepository.findRevenueByWeekdayFrom(start);

        if (weekdayData.isEmpty()) {
            return List.of("Sem dados suficientes para gerar insights sobre dias fracos.");
        }

        final BigDecimal averageRevenue = weekdayData.stream()
                .map(WeekdayRevenue::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(weekdayData.size()), 2, RoundingMode.HALF_UP);

        final BigDecimal threshold = averageRevenue.multiply(BigDecimal.valueOf(0.7));

        final List<String> insights = new ArrayList<>();

        for (final WeekdayRevenue data : weekdayData) {
            if (data.totalRevenue().compareTo(threshold) < 0) {
                final String dayName = getDayNameInPortuguese(data.dayOfWeek());
                final BigDecimal percentageBelowAverage = averageRevenue.subtract(data.totalRevenue())
                        .divide(averageRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                insights.add(String.format(
                        "%s teve receita %.0f%% abaixo da média. Considere criar promoções para este dia.",
                        dayName,
                        percentageBelowAverage
                ));
            }
        }

        if (insights.isEmpty()) {
            insights.add("Todos os dias da semana tiveram desempenho satisfatório!");
        }

        return insights;
    }

    private BigDecimal calculateGrowthPercentage(final BigDecimal previous, final BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String getDayNameInPortuguese(final int dayOfWeek) {
        return switch (dayOfWeek) {
            case 0 -> "Domingo";
            case 1 -> "Segunda-feira";
            case 2 -> "Terça-feira";
            case 3 -> "Quarta-feira";
            case 4 -> "Quinta-feira";
            case 5 -> "Sexta-feira";
            case 6 -> "Sábado";
            default -> "Desconhecido";
        };
    }
}