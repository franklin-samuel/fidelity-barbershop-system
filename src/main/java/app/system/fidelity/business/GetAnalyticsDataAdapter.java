package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetAnalyticsDataPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.*;
import app.system.fidelity.domain.enums.Gender;
import app.system.fidelity.domain.enums.PreferredFrequency;
import app.system.fidelity.domain.enums.PreferredStyle;
import app.system.fidelity.domain.enums.ReferralSource;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GetAnalyticsDataAdapter implements GetAnalyticsDataPort {

    private final CustomerRepositoryPort customerRepository;
    private final AppointmentRepositoryPort appointmentRepository;

    @Override
    public AnalyticsData execute(final Context context) {

        final LocalDateTime now = LocalDateTime.now();
        final int currentDay = now.getDayOfMonth();

        final LocalDateTime currentMonthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        final LocalDateTime currentPeriodEnd = now.toLocalDate().atTime(23, 59, 59);

        final LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);
        final LocalDateTime previousPeriodEnd = previousMonthStart.plusDays(currentDay - 1).toLocalDate().atTime(23, 59, 59);

        final LocalDateTime twelveWeeksAgo = now.minusWeeks(12).toLocalDate().atStartOfDay();

        final LocalDateTime sixMonthsAgo = now.minusMonths(6).toLocalDate().atStartOfDay();

        final long newCustomersCount = customerRepository.countByCreatedAtBetween(currentMonthStart, currentPeriodEnd);
        final long newCustomersCountPrevious = customerRepository.countByCreatedAtBetween(previousMonthStart, previousPeriodEnd);
        final long newCustomersGrowth = newCustomersCount - newCustomersCountPrevious;

        final BigDecimal currentRevenue = appointmentRepository.sumTotalAmountWithCustomerBetween(currentMonthStart, currentPeriodEnd);
        final long currentCount = appointmentRepository.countWithCustomerBetween(currentMonthStart, currentPeriodEnd);
        final BigDecimal averageTicketCurrent = currentCount > 0
                ? currentRevenue.divide(BigDecimal.valueOf(currentCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final BigDecimal previousRevenue = appointmentRepository.sumTotalAmountWithCustomerBetween(previousMonthStart, previousPeriodEnd);
        final long previousCount = appointmentRepository.countWithCustomerBetween(previousMonthStart, previousPeriodEnd);
        final BigDecimal averageTicketPrevious = previousCount > 0
                ? previousRevenue.divide(BigDecimal.valueOf(previousCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final BigDecimal ticketGrowthAbsolute = averageTicketCurrent.subtract(averageTicketPrevious);
        final BigDecimal ticketGrowthPercentage = averageTicketPrevious.compareTo(BigDecimal.ZERO) > 0
                ? ticketGrowthAbsolute.divide(averageTicketPrevious, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final Map<String, BigDecimal> revenueByWeekday = calculateRevenueByWeekday(
                appointmentRepository.findRevenueByWeekdayFrom(twelveWeeksAgo),
                twelveWeeksAgo
        );

        final LocalDateTime prevMonthStart = now.minusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
        final LocalDateTime prevMonthEnd = now.withDayOfMonth(1).minusDays(1).toLocalDate().atTime(23, 59, 59);

        final long previousMonthCustomers = appointmentRepository.countDistinctCustomersBetween(
                prevMonthStart.isAfter(sixMonthsAgo) ? prevMonthStart : sixMonthsAgo,
                prevMonthEnd
        );

        final long retainedCustomers = appointmentRepository.countRetainedCustomers(
                prevMonthStart.isAfter(sixMonthsAgo) ? prevMonthStart : sixMonthsAgo,
                prevMonthEnd,
                currentMonthStart,
                currentPeriodEnd
        );

        final Double retentionRate = previousMonthCustomers > 0
                ? Math.round((retainedCustomers * 100.0 / previousMonthCustomers) * 100.0) / 100.0
                : 0.0;

        final long totalCustomers = customerRepository.countAll();
        final BigDecimal totalRevenue = appointmentRepository.sumTotalAmount();

        final long countWithCustomer = appointmentRepository.countWithCustomer();
        final BigDecimal revenueWithCustomer = appointmentRepository.sumTotalAmountWithCustomer();
        final BigDecimal averageTicket = countWithCustomer > 0
                ? revenueWithCustomer.divide(BigDecimal.valueOf(countWithCustomer), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final Map<Gender, Long> customersByGender = customerRepository.countGroupByGender();
        final Map<ReferralSource, Long> referralCounts = customerRepository.countGroupByReferralSource();
        final Map<PreferredFrequency, Long> preferredFrequency = customerRepository.countGroupByPreferredFrequency();
        final Map<PreferredStyle, Long> styleCounts = customerRepository.countGroupByPreferredStyle();

        final List<ChannelData> acquisitionChannels = buildAcquisitionChannels(referralCounts, totalCustomers);
        final List<StyleData> popularStyles = buildPopularStyles(styleCounts);

        final List<Customer> allCustomers = customerRepository.findAll();

        final Map<UUID, CustomerAppointmentSummary> appointmentSummaryByCustomer =
                appointmentRepository.findRevenueGroupByCustomer().stream()
                        .collect(Collectors.toMap(CustomerAppointmentSummary::customerId, s -> s));

        final List<TopCustomer> topCustomers = appointmentSummaryByCustomer.entrySet().stream()
                .sorted((a, b) -> b.getValue().totalRevenue().compareTo(a.getValue().totalRevenue()))
                .limit(10)
                .map(entry -> {
                    final Customer customer = allCustomers.stream()
                            .filter(c -> c.getId().equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);

                    return TopCustomer.builder()
                            .name(customer != null ? customer.getName() : "Cliente Desconhecido")
                            .totalSpent(entry.getValue().totalRevenue())
                            .visitsCount(entry.getValue().appointmentCount().intValue())
                            .build();
                })
                .collect(Collectors.toList());

        final Map<String, Long> customersByAgeGroup = calculateAgeGroups(allCustomers);
        final Map<String, BigDecimal> avgTicketByAge = calculateAvgTicketByAge(allCustomers, appointmentSummaryByCustomer);
        final List<ChannelRevenue> channelVsRevenue = calculateChannelRevenue(allCustomers, appointmentSummaryByCustomer);

        return AnalyticsData.builder()
                .totalCustomers(totalCustomers)
                .averageTicket(averageTicket)
                .totalRevenue(totalRevenue)
                .retentionRate(retentionRate)
                .newCustomersCount(newCustomersCount)
                .newCustomersCountPreviousPeriod(newCustomersCountPrevious)
                .newCustomersGrowthAbsolute(newCustomersGrowth)
                .averageTicketCurrent(averageTicketCurrent)
                .averageTicketPrevious(averageTicketPrevious)
                .averageTicketGrowthAbsolute(ticketGrowthAbsolute)
                .averageTicketGrowthPercentage(ticketGrowthPercentage)
                .revenueByWeekday(revenueByWeekday)
                .customersByAgeGroup(customersByAgeGroup)
                .customerByGender(customersByGender)
                .acquisitionChannels(acquisitionChannels)
                .popularStyles(popularStyles)
                .preferredFrequency(preferredFrequency)
                .topCustomers(topCustomers)
                .avgTicketByAge(avgTicketByAge)
                .channelVsRevenue(channelVsRevenue)
                .build();
    }

    private Map<String, BigDecimal> calculateRevenueByWeekday(
            final List<WeekdayRevenue> weekdayData,
            final LocalDateTime startDate
    ) {
        final Map<String, BigDecimal> result = new LinkedHashMap<>();

        result.put("SUNDAY", BigDecimal.ZERO);
        result.put("MONDAY", BigDecimal.ZERO);
        result.put("TUESDAY", BigDecimal.ZERO);
        result.put("WEDNESDAY", BigDecimal.ZERO);
        result.put("THURSDAY", BigDecimal.ZERO);
        result.put("FRIDAY", BigDecimal.ZERO);
        result.put("SATURDAY", BigDecimal.ZERO);

        if (weekdayData.isEmpty()) {
            return result;
        }

        final Optional<LocalDateTime> firstAppointmentOpt = appointmentRepository.findFirstAppointmentDateFrom(startDate);
        final Optional<LocalDateTime> lastAppointmentOpt = appointmentRepository.findLastAppointmentDateFrom(startDate);

        if (firstAppointmentOpt.isEmpty() || lastAppointmentOpt.isEmpty()) {
            return result;
        }

        final LocalDateTime firstAppointmentDate = firstAppointmentOpt.get();
        final LocalDateTime lastAppointmentDate = lastAppointmentOpt.get();

        final long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                firstAppointmentDate.toLocalDate(),
                lastAppointmentDate.toLocalDate()
        );
        final long weeksInData = Math.max(1, daysBetween / 7);

        final long weeksCount = Math.min(12, weeksInData);

        for (final WeekdayRevenue data : weekdayData) {
            final String dayName = getDayName(data.dayOfWeek());
            final BigDecimal average = data.totalRevenue()
                    .divide(BigDecimal.valueOf(weeksCount), 2, RoundingMode.HALF_UP);
            result.put(dayName, average);
        }

        return result;
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

    private List<ChannelData> buildAcquisitionChannels(
            final Map<ReferralSource, Long> counts,
            final long total
    ) {
        return counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> {
                    final double percentage = total > 0 ? (entry.getValue() * 100.0) / total : 0.0;
                    return ChannelData.builder()
                            .channel(entry.getKey())
                            .customerCount(entry.getValue())
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<StyleData> buildPopularStyles(final Map<PreferredStyle, Long> counts) {
        final long total = counts.values().stream().mapToLong(Long::longValue).sum();
        return counts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> {
                    final double percentage = total > 0 ? (entry.getValue() * 100.0) / total : 0.0;
                    return StyleData.builder()
                            .style(entry.getKey())
                            .count(entry.getValue())
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<String, Long> calculateAgeGroups(final List<Customer> customers) {
        final Map<String, Long> groups = new LinkedHashMap<>();
        groups.put("0-10", 0L);
        groups.put("11-17", 0L);
        groups.put("18-25", 0L);
        groups.put("26-35", 0L);
        groups.put("36-45", 0L);
        groups.put("46-60", 0L);
        groups.put("60+", 0L);
        groups.put("Não informado", 0L);

        for (final Customer c : customers) {
            final String key = resolveAgeGroup(c);
            groups.merge(key, 1L, Long::sum);
        }

        return groups;
    }

    private Map<String, BigDecimal> calculateAvgTicketByAge(
            final List<Customer> customers,
            final Map<UUID, CustomerAppointmentSummary> summaryByCustomer
    ) {
        final Map<String, BigDecimal> totalRevByGroup = new LinkedHashMap<>();
        final Map<String, Long> countByGroup = new LinkedHashMap<>();

        for (final String group : List.of("0-10", "11-17", "18-25", "26-35", "36-45", "46-60", "60+")) {
            totalRevByGroup.put(group, BigDecimal.ZERO);
            countByGroup.put(group, 0L);
        }

        for (final Customer c : customers) {
            if (c.getDateOfBirth() == null) continue;

            final String group = resolveAgeGroup(c);
            if (group.equals("Não informado")) continue;

            final CustomerAppointmentSummary summary = summaryByCustomer.get(c.getId());
            if (summary == null) continue;

            totalRevByGroup.merge(group, summary.totalRevenue(), BigDecimal::add);
            countByGroup.merge(group, summary.appointmentCount(), Long::sum);
        }

        final Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (final String group : totalRevByGroup.keySet()) {
            final long count = countByGroup.get(group);
            result.put(group, count > 0
                    ? totalRevByGroup.get(group).divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO
            );
        }

        return result;
    }

    private List<ChannelRevenue> calculateChannelRevenue(
            final List<Customer> customers,
            final Map<UUID, CustomerAppointmentSummary> summaryByCustomer
    ) {
        final Map<ReferralSource, BigDecimal> revenueByChannel = new LinkedHashMap<>();
        final Map<ReferralSource, Long> appointmentsByChannel = new LinkedHashMap<>();
        final Map<ReferralSource, Long> customersByChannel = new LinkedHashMap<>();

        for (final Customer c : customers) {
            if (c.getReferralSource() == null) continue;

            final ReferralSource source = c.getReferralSource();
            customersByChannel.merge(source, 1L, Long::sum);

            final CustomerAppointmentSummary summary = summaryByCustomer.get(c.getId());
            if (summary == null) continue;

            revenueByChannel.merge(source, summary.totalRevenue(), BigDecimal::add);
            appointmentsByChannel.merge(source, summary.appointmentCount(), Long::sum);
        }

        return revenueByChannel.entrySet().stream()
                .map(entry -> {
                    final ReferralSource source = entry.getKey();
                    final long count = appointmentsByChannel.getOrDefault(source, 0L);
                    final BigDecimal avgTicket = count > 0
                            ? entry.getValue().divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return ChannelRevenue.builder()
                            .channel(source)
                            .averageTicket(avgTicket)
                            .customerCount(customersByChannel.getOrDefault(source, 0L))
                            .build();
                })
                .sorted((a, b) -> b.getAverageTicket().compareTo(a.getAverageTicket()))
                .collect(Collectors.toList());
    }

    private String resolveAgeGroup(final Customer customer) {
        if (customer.getDateOfBirth() == null) return "Não informado";
        final int age = Period.between(customer.getDateOfBirth(), LocalDate.now()).getYears();
        if (age <= 10)  return "0-10";
        if (age <= 17)  return "11-17";
        if (age <= 25)  return "18-25";
        if (age <= 35)  return "26-35";
        if (age <= 45)  return "36-45";
        if (age <= 60)  return "46-60";
        return "60+";
    }
}