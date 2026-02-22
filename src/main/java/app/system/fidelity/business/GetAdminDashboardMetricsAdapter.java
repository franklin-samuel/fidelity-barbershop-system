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
        final LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        final LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        final LocalDateTime startOfPreviousMonth = startOfMonth.minusMonths(1);
        final LocalDateTime endOfPreviousMonth = startOfMonth;
        final LocalDateTime thirtyDaysAgo = now.minusDays(30).toLocalDate().atStartOfDay();

        final List<Appointment> allAppointments = appointmentRepository.findAll();

        final BigDecimal todayRevenue = allAppointments.stream()
                .filter(a -> !a.getCreatedAt().isBefore(startOfToday))
                .map(Appointment::getBarbershopRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal weekRevenue = allAppointments.stream()
                .filter(a -> !a.getCreatedAt().isBefore(startOfWeek))
                .map(Appointment::getBarbershopRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal monthRevenue = allAppointments.stream()
                .filter(a -> !a.getCreatedAt().isBefore(startOfMonth))
                .map(Appointment::getBarbershopRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final List<DailyRevenue> last30DaysRevenue = calculateDailyRevenue(allAppointments, thirtyDaysAgo, now);

        final BigDecimal previousMonthRevenue = allAppointments.stream()
                .filter(a -> !a.getCreatedAt().isBefore(startOfPreviousMonth) && a.getCreatedAt().isBefore(endOfPreviousMonth))
                .map(Appointment::getBarbershopRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal monthlyGrowthPercentage = calculateGrowthPercentage(previousMonthRevenue, monthRevenue);

        final List<BarberPerformance> topBarbers = calculateTopBarbers(allAppointments, startOfMonth);

        final Map<Boolean, BigDecimal> revenueByType = allAppointments.stream()
                .filter(a -> !a.getCreatedAt().isBefore(startOfMonth))
                .collect(Collectors.groupingBy(
                        a -> a.getServiceId() != null,
                        Collectors.reducing(BigDecimal.ZERO, Appointment::getBarbershopRevenue, BigDecimal::add)
                ));

        final BigDecimal servicesRevenue = revenueByType.getOrDefault(true, BigDecimal.ZERO);
        final BigDecimal productsRevenue = revenueByType.getOrDefault(false, BigDecimal.ZERO);

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

    private List<BarberPerformance> calculateTopBarbers(
            final List<Appointment> appointments,
            final LocalDateTime startOfMonth
    ) {
        final Map<UUID, List<Appointment>> appointmentsByBarber = appointments.stream()
                .filter(a -> !a.getCreatedAt().isBefore(startOfMonth))
                .filter(a -> a.getBarberId() != null)
                .collect(Collectors.groupingBy(Appointment::getBarberId));

        final Set<UUID> barberIds = appointmentsByBarber.keySet();
        final Map<UUID, String> barberNames = barberIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> userRepositoryPort.get(id)
                                .map(User::getName)
                                .orElse("Barbeiro Desconhecido")
                ));

        return appointmentsByBarber.entrySet().stream()
                .map(entry -> {
                    final UUID barberId = entry.getKey();
                    final List<Appointment> barberAppointments = entry.getValue();

                    final BigDecimal totalRevenue = barberAppointments.stream()
                            .map(Appointment::getBarbershopRevenue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    final String barberName = barberNames.get(barberId);

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
            final List<Appointment> appointments,
            final LocalDateTime startDate,
            final LocalDateTime endDate
    ) {
        final Map<LocalDate, BigDecimal> dailyMap = appointments.stream()
                .filter(a -> !a.getCreatedAt().isBefore(startDate) && !a.getCreatedAt().isAfter(endDate))
                .collect(Collectors.groupingBy(
                        a -> a.getCreatedAt().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Appointment::getBarbershopRevenue, BigDecimal::add)
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