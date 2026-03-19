package app.system.fidelity.messaging.service;

import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.core.persistence.UserRepositoryPort;
import app.system.fidelity.domain.*;
import app.system.fidelity.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyReportDataAggregator {

    private final AppointmentRepositoryPort appointmentRepository;
    private final CustomerRepositoryPort customerRepository;
    private final UserRepositoryPort userRepository;

    public AggregatedReportData aggregateDataForMonth(final YearMonth targetMonth) {
        log.info("Agregando dados para o mês: {}", targetMonth);

        final LocalDateTime targetStart = targetMonth.atDay(1).atStartOfDay();
        final LocalDateTime targetEnd = targetMonth.atEndOfMonth().atTime(23, 59, 59);

        final LocalDateTime historicalStart = targetMonth.minusMonths(11).atDay(1).atStartOfDay();

        return AggregatedReportData.builder()
                .targetMonth(targetMonth)
                .currentMonthData(buildMonthData(targetStart, targetEnd))
                .historicalMonths(buildHistoricalMonths(historicalStart, targetEnd, targetMonth))
                .customerInsights(buildCustomerInsights(targetStart, targetEnd, historicalStart))
                .barberInsights(buildBarberInsights(targetStart, targetEnd, historicalStart))
                .businessTrends(buildBusinessTrends(historicalStart, targetEnd))
                .build();
    }

    private MonthData buildMonthData(final LocalDateTime start, final LocalDateTime end) {
        final BigDecimal revenue = appointmentRepository.sumBarbershopRevenueBetween(start, end);
        final long appointments = appointmentRepository.countWithCustomerBetween(start, end);
        final long newCustomers = customerRepository.countByCreatedAtBetween(start, end);
        final BigDecimal servicesRevenue = appointmentRepository.sumServicesRevenueBetween(start, end);
        final BigDecimal productsRevenue = appointmentRepository.sumProductsRevenueBetween(start, end);

        final BigDecimal avgTicket = appointments > 0
                ? revenue.divide(BigDecimal.valueOf(appointments), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final List<WeekdayRevenue> weekdayData = appointmentRepository.findRevenueByWeekdayFrom(start);
        final Map<String, WeekdayStats> weekdayStats = buildWeekdayStats(weekdayData);

        return MonthData.builder()
                .totalRevenue(revenue)
                .totalAppointments(appointments)
                .newCustomers(newCustomers)
                .averageTicket(avgTicket)
                .servicesRevenue(servicesRevenue)
                .productsRevenue(productsRevenue)
                .weekdayStats(weekdayStats)
                .build();
    }

    private List<HistoricalMonthData> buildHistoricalMonths(
            final LocalDateTime historicalStart,
            final LocalDateTime historicalEnd,
            final YearMonth targetMonth
    ) {
        final List<HistoricalMonthData> months = new ArrayList<>();
        YearMonth current = YearMonth.from(historicalStart);
        final YearMonth end = YearMonth.from(historicalEnd);

        while (!current.isAfter(end)) {
            final LocalDateTime monthStart = current.atDay(1).atStartOfDay();
            final LocalDateTime monthEnd = current.atEndOfMonth().atTime(23, 59, 59);

            final BigDecimal revenue = appointmentRepository.sumBarbershopRevenueBetween(monthStart, monthEnd);
            final long appointments = appointmentRepository.countWithCustomerBetween(monthStart, monthEnd);
            final long newCustomers = customerRepository.countByCreatedAtBetween(monthStart, monthEnd);

            months.add(HistoricalMonthData.builder()
                    .month(current)
                    .revenue(revenue)
                    .appointments(appointments)
                    .newCustomers(newCustomers)
                    .isTargetMonth(current.equals(targetMonth))
                    .build());

            current = current.plusMonths(1);
        }

        return months;
    }

    private CustomerInsightsData buildCustomerInsights(
            final LocalDateTime targetStart,
            final LocalDateTime targetEnd,
            final LocalDateTime historicalStart
    ) {
        final long activeCustomersInMonth = appointmentRepository.countDistinctCustomersBetween(targetStart, targetEnd);

        final long totalCustomersInBase = customerRepository.countAll();

        final List<Appointment> monthAppointments = appointmentRepository.findByCreatedAtBetween(targetStart, targetEnd);
        final Set<UUID> customersInMonth = monthAppointments.stream()
                .map(Appointment::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final long newCustomersInMonth = customerRepository.countByCreatedAtBetween(targetStart, targetEnd);
        final long returningCustomers = customersInMonth.size() - newCustomersInMonth;

        final LocalDateTime churnThreshold = targetEnd.minusDays(60);
        final List<Customer> allCustomers = customerRepository.findAll();
        final long churnedCustomers = allCustomers.stream()
                .filter(c -> c.getLastVisitDate() != null && c.getLastVisitDate().isBefore(churnThreshold))
                .count();

        final Map<UUID, BigDecimal> revenueByCustomer = monthAppointments.stream()
                .filter(a -> a.getCustomerId() != null)
                .collect(Collectors.groupingBy(
                        Appointment::getCustomerId,
                        Collectors.reducing(BigDecimal.ZERO, Appointment::getTotalAmount, BigDecimal::add)
                ));

        final List<TopCustomerInMonth> topCustomers = revenueByCustomer.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(entry -> {
                    final Customer customer = customerRepository.get(entry.getKey()).orElse(null);
                    final long visits = monthAppointments.stream()
                            .filter(a -> entry.getKey().equals(a.getCustomerId()))
                            .count();
                    return TopCustomerInMonth.builder()
                            .totalSpent(entry.getValue())
                            .visitsInMonth(visits)
                            .isNewCustomer(customer != null && customer.getCreatedAt().isAfter(targetStart))
                            .build();
                })
                .collect(Collectors.toList());

        final double avgVisitsPerCustomer = customersInMonth.isEmpty() ? 0.0
                : (double) monthAppointments.size() / customersInMonth.size();

        return CustomerInsightsData.builder()
                .activeCustomersInMonth(activeCustomersInMonth)
                .totalCustomersInBase(totalCustomersInBase)
                .newCustomersInMonth(newCustomersInMonth)
                .returningCustomersInMonth(returningCustomers)
                .churnedCustomers(churnedCustomers)
                .topCustomersInMonth(topCustomers)
                .averageVisitsPerCustomer(avgVisitsPerCustomer)
                .build();
    }

    private BarberInsightsData buildBarberInsights(
            final LocalDateTime targetStart,
            final LocalDateTime targetEnd,
            final LocalDateTime historicalStart
    ) {
        final List<BarberRevenueSummary> monthPerformance = appointmentRepository
                .findTopBarbersByRevenueBetween(targetStart, targetEnd);

        final List<User> barbers = userRepository.findAllBarbers();
        final Map<UUID, String> barberNames = barbers.stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        final List<BarberMonthlyDetail> barberDetails = monthPerformance.stream()
                .map(summary -> {
                    final BigDecimal avgTicket = summary.appointmentsCount() > 0
                            ? summary.revenue().divide(BigDecimal.valueOf(summary.appointmentsCount()), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    final LocalDateTime threeMonthsAgo = targetStart.minusMonths(3);
                    final BigDecimal historicalRevenue = appointmentRepository
                            .sumTotalAmountByBarberIdBetween(summary.barberId(), threeMonthsAgo, targetStart.minusSeconds(1));
                    final long historicalAppointments = appointmentRepository
                            .countByBarberIdBetween(summary.barberId(), threeMonthsAgo, targetStart.minusSeconds(1));

                    final BigDecimal historicalAvgTicket = historicalAppointments > 0
                            ? historicalRevenue.divide(BigDecimal.valueOf(historicalAppointments), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return BarberMonthlyDetail.builder()
                            .barberName(barberNames.getOrDefault(summary.barberId(), "Desconhecido"))
                            .revenue(summary.revenue())
                            .appointments(summary.appointmentsCount())
                            .averageTicket(avgTicket)
                            .historicalAverageTicket(historicalAvgTicket)
                            .build();
                })
                .collect(Collectors.toList());

        final List<String> decliningBarbers = barberDetails.stream()
                .filter(b -> b.getHistoricalAverageTicket().compareTo(BigDecimal.ZERO) > 0
                        && b.getAverageTicket().compareTo(b.getHistoricalAverageTicket()) < 0)
                .map(BarberMonthlyDetail::getBarberName)
                .collect(Collectors.toList());

        return BarberInsightsData.builder()
                .barberDetails(barberDetails)
                .totalActiveBarbers((long) barberDetails.size())
                .decliningPerformanceBarbers(decliningBarbers)
                .build();
    }

    private BusinessTrendsData buildBusinessTrends(
            final LocalDateTime historicalStart,
            final LocalDateTime historicalEnd
    ) {
        final List<HistoricalMonthData> last6Months = buildHistoricalMonths(
                historicalStart.plusMonths(6),
                historicalEnd,
                YearMonth.from(historicalEnd)
        );

        final String trend = calculateTrend(last6Months);

        final Map<String, BigDecimal> monthlyAverages = calculateMonthlyAverages(last6Months);

        final boolean isGrowing = isRevenueGrowing(last6Months);
        final boolean isStable = isRevenueStable(last6Months);

        return BusinessTrendsData.builder()
                .trend(trend)
                .isGrowing(isGrowing)
                .isStable(isStable)
                .monthlyAverages(monthlyAverages)
                .build();
    }

    private Map<String, WeekdayStats> buildWeekdayStats(final List<WeekdayRevenue> weekdayData) {
        final Map<String, WeekdayStats> stats = new LinkedHashMap<>();

        for (final WeekdayRevenue data : weekdayData) {
            final String dayName = getDayName(data.dayOfWeek());
            final BigDecimal avgRevenue = data.appointmentCount() > 0
                    ? data.totalRevenue().divide(BigDecimal.valueOf(data.appointmentCount()), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            stats.put(dayName, WeekdayStats.builder()
                    .totalRevenue(data.totalRevenue())
                    .appointments(data.appointmentCount())
                    .averageTicket(avgRevenue)
                    .build());
        }

        return stats;
    }

    private String calculateTrend(final List<HistoricalMonthData> months) {
        if (months.size() < 3) return "INSUFFICIENT_DATA";

        final BigDecimal firstHalfAvg = months.subList(0, months.size() / 2).stream()
                .map(HistoricalMonthData::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(months.size() / 2), 2, RoundingMode.HALF_UP);

        final BigDecimal secondHalfAvg = months.subList(months.size() / 2, months.size()).stream()
                .map(HistoricalMonthData::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(months.size() - months.size() / 2), 2, RoundingMode.HALF_UP);

        if (secondHalfAvg.compareTo(firstHalfAvg.multiply(BigDecimal.valueOf(1.1))) > 0) {
            return "STRONG_GROWTH";
        } else if (secondHalfAvg.compareTo(firstHalfAvg.multiply(BigDecimal.valueOf(1.05))) > 0) {
            return "MODERATE_GROWTH";
        } else if (secondHalfAvg.compareTo(firstHalfAvg.multiply(BigDecimal.valueOf(0.95))) < 0) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }

    private Map<String, BigDecimal> calculateMonthlyAverages(final List<HistoricalMonthData> months) {
        return months.stream()
                .collect(Collectors.groupingBy(
                        m -> m.getMonth().getMonth().name(),
                        Collectors.averagingDouble(m -> m.getRevenue().doubleValue())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> BigDecimal.valueOf(e.getValue())
                ));
    }

    private boolean isRevenueGrowing(final List<HistoricalMonthData> months) {
        if (months.size() < 3) return false;

        final List<BigDecimal> revenues = months.stream()
                .map(HistoricalMonthData::getRevenue)
                .collect(Collectors.toList());

        int growthCount = 0;
        for (int i = 1; i < revenues.size(); i++) {
            if (revenues.get(i).compareTo(revenues.get(i - 1)) > 0) {
                growthCount++;
            }
        }

        return growthCount >= revenues.size() * 0.6;
    }

    private boolean isRevenueStable(final List<HistoricalMonthData> months) {
        if (months.size() < 3) return false;

        final BigDecimal avg = months.stream()
                .map(HistoricalMonthData::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(months.size()), 2, RoundingMode.HALF_UP);

        final double variance = months.stream()
                .mapToDouble(m -> Math.pow(m.getRevenue().subtract(avg).doubleValue(), 2))
                .average()
                .orElse(0.0);

        final double stdDev = Math.sqrt(variance);
        final double coefficientOfVariation = avg.compareTo(BigDecimal.ZERO) > 0
                ? stdDev / avg.doubleValue()
                : 0.0;

        return coefficientOfVariation < 0.15;
    }

    private String getDayName(final int dayOfWeek) {
        return switch (dayOfWeek) {
            case 0 -> "SUNDAY";
            case 1 -> "MONDAY";
            case 2 -> "TUESDAY";
            case 3 -> "WEDNESDAY";
            case 4 -> "THURSDAY";
            case 5 -> "FRIDAY";
            case 6 -> "SATURDAY";
            default -> "UNKNOWN";
        };
    }


    @Data
    @Builder
    public static class AggregatedReportData {
        private YearMonth targetMonth;
        private MonthData currentMonthData;
        private List<HistoricalMonthData> historicalMonths;
        private CustomerInsightsData customerInsights;
        private BarberInsightsData barberInsights;
        private BusinessTrendsData businessTrends;
    }

    @Data
    @Builder
    public static class MonthData {
        private BigDecimal totalRevenue;
        private Long totalAppointments;
        private Long newCustomers;
        private BigDecimal averageTicket;
        private BigDecimal servicesRevenue;
        private BigDecimal productsRevenue;
        private Map<String, WeekdayStats> weekdayStats;
    }

    @Data
    @Builder
    public static class WeekdayStats {
        private BigDecimal totalRevenue;
        private Long appointments;
        private BigDecimal averageTicket;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class HistoricalMonthData {
        private YearMonth month;
        private BigDecimal revenue;
        private Long appointments;
        private Long newCustomers;
        private Boolean isTargetMonth;
    }

    @Data
    @Builder
    public static class CustomerInsightsData {
        private Long activeCustomersInMonth;
        private Long totalCustomersInBase;
        private Long newCustomersInMonth;
        private Long returningCustomersInMonth;
        private Long churnedCustomers;
        private List<TopCustomerInMonth> topCustomersInMonth;
        private Double averageVisitsPerCustomer;
    }

    @Data
    @Builder
    public static class TopCustomerInMonth {
        private BigDecimal totalSpent;
        private Long visitsInMonth;
        private Boolean isNewCustomer;
    }

    @Data
    @Builder
    public static class BarberInsightsData {
        private List<BarberMonthlyDetail> barberDetails;
        private Long totalActiveBarbers;
        private List<String> decliningPerformanceBarbers;
    }

    @Data
    @Builder
    public static class BarberMonthlyDetail {
        private String barberName;
        private BigDecimal revenue;
        private Long appointments;
        private BigDecimal averageTicket;
        private BigDecimal historicalAverageTicket;
    }

    @Data
    @Builder
    public static class BusinessTrendsData {
        private String trend;
        private Boolean isGrowing;
        private Boolean isStable;
        private Map<String, BigDecimal> monthlyAverages;
    }
}