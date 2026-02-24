package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetAdminDashboardMetricsPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.BarberPerformance;
import app.system.fidelity.domain.BarberRevenueSummary;
import app.system.fidelity.domain.DailyRevenue;
import app.system.fidelity.domain.DashboardMetrics;
import app.system.fidelity.domain.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GetAdminDashboardMetricsAdapter implements GetAdminDashboardMetricsPort {

    private final AppointmentRepositoryPort appointmentRepository;
    private final UserRepositoryPort userRepositoryPort;

    @Override
    public DashboardMetrics execute(final Context context) {

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        final LocalDateTime endOfToday = now.toLocalDate().atTime(23, 59, 59);
        final LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        final LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        final LocalDateTime startOfPreviousMonth = startOfMonth.minusMonths(1);
        final LocalDateTime endOfPreviousMonth = startOfMonth.minusSeconds(1);
        final LocalDateTime thirtyDaysAgo = now.minusDays(30).toLocalDate().atStartOfDay();

        final BigDecimal todayRevenue = appointmentRepository.sumBarbershopRevenueBetween(startOfToday, endOfToday);
        final BigDecimal weekRevenue = appointmentRepository.sumBarbershopRevenueBetween(startOfWeek, endOfToday);
        final BigDecimal monthRevenue = appointmentRepository.sumBarbershopRevenueBetween(startOfMonth, endOfToday);
        final BigDecimal previousMonthRevenue = appointmentRepository.sumBarbershopRevenueBetween(startOfPreviousMonth, endOfPreviousMonth);

        final BigDecimal servicesRevenue = appointmentRepository.sumServicesRevenueBetween(startOfMonth, endOfToday);
        final BigDecimal productsRevenue = appointmentRepository.sumProductsRevenueBetween(startOfMonth, endOfToday);

        final BigDecimal monthlyGrowthPercentage = calculateGrowthPercentage(previousMonthRevenue, monthRevenue);

        final List<BarberPerformance> topBarbers = buildTopBarbers(
                appointmentRepository.findTopBarbersByRevenueBetween(startOfMonth, endOfToday)
        );

        final List<Appointment> last30Days = appointmentRepository.findByCreatedAtBetween(thirtyDaysAgo, endOfToday);
        final List<DailyRevenue> last30DaysRevenue = calculateDailyRevenue(last30Days, thirtyDaysAgo, endOfToday);

        return DashboardMetrics.builder()
                .todayRevenue(todayRevenue)
                .weekRevenue(weekRevenue)
                .monthRevenue(monthRevenue)
                .last30DaysRevenue(last30DaysRevenue)
                .currentMonthRevenue(monthRevenue)
                .previousMonthRevenue(previousMonthRevenue)
                .monthlyGrowthPercentage(monthlyGrowthPercentage)
                .topBarbers(topBarbers)
                .servicesRevenue(servicesRevenue)
                .productsRevenue(productsRevenue)
                .build();
    }

    private List<BarberPerformance> buildTopBarbers(final List<BarberRevenueSummary> summaries) {
        if (summaries.isEmpty()) {
            return List.of();
        }

        final Set<UUID> barberIds = summaries.stream()
                .map(BarberRevenueSummary::barberId)
                .collect(Collectors.toSet());

        final Map<UUID, String> barberNames = userRepositoryPort.findAllById(barberIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        return summaries.stream()
                .map(s -> BarberPerformance.builder()
                        .barberName(barberNames.getOrDefault(s.barberId(), "Barbeiro Desconhecido"))
                        .totalRevenue(s.revenue())
                        .appointmentsCount(s.appointmentsCount().intValue())
                        .build())
                .toList();
    }

    private List<DailyRevenue> calculateDailyRevenue(
            final List<Appointment> appointments,
            final LocalDateTime startDate,
            final LocalDateTime endDate
    ) {
        final Map<LocalDate, BigDecimal> dailyMap = new HashMap<>();
        for (final Appointment a : appointments) {
            final LocalDate date = a.getCreatedAt().toLocalDate();
            dailyMap.merge(date, a.getBarbershopRevenue(), BigDecimal::add);
        }

        final List<DailyRevenue> result = new ArrayList<>();
        LocalDate current = startDate.toLocalDate();
        final LocalDate end = endDate.toLocalDate();

        while (!current.isAfter(end)) {
            result.add(DailyRevenue.builder()
                    .date(current)
                    .amount(dailyMap.getOrDefault(current, BigDecimal.ZERO))
                    .build());
            current = current.plusDays(1);
        }

        return result;
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
}