package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetBarberDashboardMetricsPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.DailyRevenue;
import app.system.fidelity.domain.DashboardMetrics;
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
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GetBarberDashboardMetricsAdapter implements GetBarberDashboardMetricsPort {

    private final AppointmentRepositoryPort appointmentRepository;

    @Override
    public DashboardMetrics execute(final Context context) {

        final UUID barberId = context.getProperty("userId", UUID.class);

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        final LocalDateTime endOfToday = now.toLocalDate().atTime(23, 59, 59);
        final LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        final LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        final LocalDateTime thirtyDaysAgo = now.minusDays(30).toLocalDate().atStartOfDay();

        final BigDecimal todayEarnings = appointmentRepository
                .sumBarberTotalByBarberIdBetween(barberId, startOfToday, endOfToday);

        final BigDecimal weekEarnings = appointmentRepository
                .sumBarberTotalByBarberIdBetween(barberId, startOfWeek, endOfToday);

        final BigDecimal monthEarnings = appointmentRepository
                .sumBarberTotalByBarberIdBetween(barberId, startOfMonth, endOfToday);

        final long appointmentsCount = appointmentRepository
                .countByBarberIdBetween(barberId, startOfMonth, endOfToday);

        final BigDecimal monthTotalRevenue = appointmentRepository
                .sumTotalAmountByBarberIdBetween(barberId, startOfMonth, endOfToday);

        final BigDecimal monthAverageTicket = appointmentsCount > 0
                ? monthTotalRevenue.divide(BigDecimal.valueOf(appointmentsCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final BigDecimal monthTotalTips = appointmentRepository
                .sumTipsByBarberIdBetween(barberId, startOfMonth, endOfToday);

        final BigDecimal monthCommission = appointmentRepository
                .sumCommissionByBarberIdBetween(barberId, startOfMonth, endOfToday);

        final List<Appointment> last30Days = appointmentRepository
                .findByBarberIdAndCreatedAtBetween(barberId, thirtyDaysAgo, endOfToday);

        final List<DailyRevenue> last30DaysEarnings = calculateDailyEarnings(last30Days, thirtyDaysAgo, endOfToday);

        return DashboardMetrics.builder()
                .todayEarnings(todayEarnings)
                .weekEarnings(weekEarnings)
                .monthEarnings(monthEarnings)
                .last30DaysEarnings(last30DaysEarnings)
                .monthAppointments((int) appointmentsCount)
                .monthAverageTicket(monthAverageTicket)
                .monthTotalTips(monthTotalTips)
                .monthCommission(monthCommission)
                .monthTips(monthTotalTips)
                .build();
    }

    private List<DailyRevenue> calculateDailyEarnings(
            final List<Appointment> appointments,
            final LocalDateTime startDate,
            final LocalDateTime endDate
    ) {
        final Map<LocalDate, BigDecimal> dailyMap = new HashMap<>();
        for (final Appointment a : appointments) {
            final LocalDate date = a.getCreatedAt().toLocalDate();
            dailyMap.merge(date, a.getBarberTotal(), BigDecimal::add);
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
}