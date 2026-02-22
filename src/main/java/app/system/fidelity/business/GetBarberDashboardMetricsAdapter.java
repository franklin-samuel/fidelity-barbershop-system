package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetBarberDashboardMetricsPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.DailyRevenue;
import app.system.fidelity.domain.DashboardMetrics;
import app.system.fidelity.domain.enums.Role;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class GetBarberDashboardMetricsAdapter implements GetBarberDashboardMetricsPort {

    private final AppointmentRepositoryPort appointmentRepository;

    @Override
    public DashboardMetrics execute(final Context context) {

        final UUID userId = context.getProperty("userId", UUID.class);

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        final LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        final LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        final LocalDateTime thirtyDaysAgo = now.minusDays(30).toLocalDate().atStartOfDay();

        final List<Appointment> barberAppointments = appointmentRepository.findByBarberId(userId);

        final BigDecimal todayEarnings = barberAppointments.stream()
                .filter(a -> a.getCreatedAt().isAfter(startOfToday))
                .map(Appointment::getBarberTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal weekEarnings = barberAppointments.stream()
                .filter(a -> a.getCreatedAt().isAfter(startOfWeek))
                .map(Appointment::getBarberTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal monthEarnings = barberAppointments.stream()
                .filter(a -> a.getCreatedAt().isAfter(startOfMonth))
                .map(Appointment::getBarberTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final List<DailyRevenue> last30DaysEarnings = calculateDailyEarnings(barberAppointments, thirtyDaysAgo, now);

        final List<Appointment> monthAppointments = barberAppointments.stream()
                .filter(a -> a.getCreatedAt().isAfter(startOfMonth))
                .collect(Collectors.toList());

        final Integer appointmentsCount = monthAppointments.size();

        final BigDecimal monthTotalRevenue = monthAppointments.stream()
                .map(Appointment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal monthAverageTicket = appointmentsCount > 0
                ? monthTotalRevenue.divide(BigDecimal.valueOf(appointmentsCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final BigDecimal monthTotalTips = monthAppointments.stream()
                .map(a -> a.getTip() != null ? a.getTip() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal monthCommission = monthAppointments.stream()
                .map(Appointment::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal monthTips = monthTotalTips;

        return DashboardMetrics.builder()
                .todayEarnings(todayEarnings)
                .weekEarnings(weekEarnings)
                .monthEarnings(monthEarnings)
                .last30DaysEarnings(last30DaysEarnings)
                .monthAppointments(appointmentsCount)
                .monthAverageTicket(monthAverageTicket)
                .monthTotalTips(monthTotalTips)
                .monthCommission(monthCommission)
                .monthTips(monthTips)
                .build();

    }

    private List<DailyRevenue> calculateDailyEarnings(
            final List<Appointment> appointments,
            final LocalDateTime startDate,
            final LocalDateTime endDate
    ) {
        final Map<LocalDate, BigDecimal> dailyMap = appointments.stream()
                .filter(a -> a.getCreatedAt().isAfter(startDate) && a.getCreatedAt().isBefore(endDate))
                .collect(Collectors.groupingBy(
                        a -> a.getCreatedAt().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Appointment::getBarberTotal, BigDecimal::add)
                ));

        final List<DailyRevenue> result = new ArrayList<>();
        LocalDate currentDate = startDate.toLocalDate();
        final LocalDate end = endDate.toLocalDate();

        while (!currentDate.isAfter(end)) {
            result.add(DailyRevenue.builder()
                    .date(currentDate)
                    .amount(dailyMap.getOrDefault(currentDate, BigDecimal.ZERO))
                    .build());
            currentDate = currentDate.plusDays(1);
        }

        return result;
    }

}
