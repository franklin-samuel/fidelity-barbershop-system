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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class GetAnalyticsDataAdapter implements GetAnalyticsDataPort {

    private final CustomerRepositoryPort customerRepository;
    private final AppointmentRepositoryPort appointmentRepository;

    @Override
    public AnalyticsData execute(final Context context) {

        final long totalCustomers = customerRepository.countAll();
        final BigDecimal totalRevenue = appointmentRepository.sumTotalAmount();

        final long countWithCustomer = appointmentRepository.countWithCustomer();
        final BigDecimal revenueWithCustomer = appointmentRepository.sumTotalAmountWithCustomer();
        final BigDecimal averageTicket = countWithCustomer > 0
                ? revenueWithCustomer.divide(BigDecimal.valueOf(countWithCustomer), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime startOfCurrentMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        final LocalDateTime endOfCurrentMonth = now.toLocalDate().atTime(23, 59, 59);
        final LocalDateTime startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
        final LocalDateTime endOfPreviousMonth = startOfCurrentMonth.minusSeconds(1);

        final long previousMonthCustomers = appointmentRepository
                .countDistinctCustomersBetween(startOfPreviousMonth, endOfPreviousMonth);

        final long retainedCustomers = appointmentRepository.countRetainedCustomers(
                startOfPreviousMonth, endOfPreviousMonth,
                startOfCurrentMonth, endOfCurrentMonth
        );

        final Double retentionRate = previousMonthCustomers > 0
                ? Math.round((retainedCustomers * 100.0 / previousMonthCustomers) * 100.0) / 100.0
                : 0.0;

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

        final List<TopCustomer> topCustomers = customerRepository.findTopCustomersByTotalSpent(10).stream()
                .map(c -> {
                    final CustomerAppointmentSummary summary = appointmentSummaryByCustomer.get(c.getId());
                    final int visits = summary != null ? summary.appointmentCount().intValue() : 0;
                    return TopCustomer.builder()
                            .name(c.getName())
                            .totalSpent(c.getTotalSpent())
                            .visitsCount(visits)
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