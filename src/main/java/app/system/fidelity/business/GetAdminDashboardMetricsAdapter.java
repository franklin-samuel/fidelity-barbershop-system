package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetAdminDashboardMetricsPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.BarberPerformance;
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
import java.util.*;
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

        final List<Appointment> last30DaysAppointments = appointmentRepository
                .findByCreatedAtBetween(thirtyDaysAgo, endOfToday);

        BigDecimal todayRevenue = BigDecimal.ZERO;
        BigDecimal weekRevenue = BigDecimal.ZERO;
        BigDecimal monthRevenue = BigDecimal.ZERO;
        BigDecimal servicesRevenue = BigDecimal.ZERO;
        BigDecimal productsRevenue = BigDecimal.ZERO;

        final Map<LocalDate, BigDecimal> dailyRevenueMap = new HashMap<>();
        final List<Appointment> currentMonthAppointments = new ArrayList<>();

        for (Appointment appointment : last30DaysAppointments) {
            final BigDecimal revenue = appointment.getBarbershopRevenue();

            if (!appointment.getCreatedAt().isBefore(startOfToday)) {
                todayRevenue = todayRevenue.add(revenue);
            }

            if (!appointment.getCreatedAt().isBefore(startOfWeek)) {
                weekRevenue = weekRevenue.add(revenue);
            }

            if (!appointment.getCreatedAt().isBefore(startOfMonth)) {
                monthRevenue = monthRevenue.add(revenue);
                currentMonthAppointments.add(appointment);

                if (appointment.getServiceId() != null) {
                    servicesRevenue = servicesRevenue.add(revenue);
                } else {
                    productsRevenue = productsRevenue.add(revenue);
                }
            }

            final LocalDate date = appointment.getCreatedAt().toLocalDate();
            dailyRevenueMap.merge(date, revenue, BigDecimal::add);
        }

        final List<DailyRevenue> last30DaysRevenue = calculateDailyRevenue(dailyRevenueMap, thirtyDaysAgo, endOfToday);

        final List<Appointment> previousMonthAppointments = appointmentRepository
                .findByCreatedAtBetween(startOfPreviousMonth, endOfPreviousMonth);

        final BigDecimal previousMonthRevenue = previousMonthAppointments.stream()
                .map(Appointment::getBarbershopRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal monthlyGrowthPercentage = calculateGrowthPercentage(previousMonthRevenue, monthRevenue);

        final List<BarberPerformance> topBarbers = calculateTopBarbers(currentMonthAppointments);

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

    private List<BarberPerformance> calculateTopBarbers(final List<Appointment> appointments) {
        final Map<UUID, List<Appointment>> appointmentsByBarber = appointments.stream()
                .filter(a -> a.getBarberId() != null)
                .collect(Collectors.groupingBy(Appointment::getBarberId));

        if (appointmentsByBarber.isEmpty()) {
            return Collections.emptyList();
        }

        final Set<UUID> barberIds = appointmentsByBarber.keySet();

        final Map<UUID, String> barberNames = userRepositoryPort.findAllById(barberIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        return appointmentsByBarber.entrySet().stream()
                .map(entry -> {
                    final UUID barberId = entry.getKey();
                    final List<Appointment> barberAppointments = entry.getValue();

                    final BigDecimal totalRevenue = barberAppointments.stream()
                            .map(Appointment::getBarbershopRevenue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    final String barberName = barberNames.getOrDefault(barberId, "Barbeiro Desconhecido");

                    return BarberPerformance.builder()
                            .barberName(barberName)
                            .totalRevenue(totalRevenue)
                            .appointmentsCount(barberAppointments.size())
                            .build();
                })
                .sorted((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()))
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<DailyRevenue> calculateDailyRevenue(
            final Map<LocalDate, BigDecimal> dailyRevenueMap,
            final LocalDateTime startDate,
            final LocalDateTime endDate
    ) {
        final List<DailyRevenue> result = new ArrayList<>();
        LocalDate currentDate = startDate.toLocalDate();
        final LocalDate end = endDate.toLocalDate();

        while (!currentDate.isAfter(end)) {
            result.add(DailyRevenue.builder()
                    .date(currentDate)
                    .amount(dailyRevenueMap.getOrDefault(currentDate, BigDecimal.ZERO))
                    .build());
            currentDate = currentDate.plusDays(1);
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