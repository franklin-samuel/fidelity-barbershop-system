package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetAnalyticsDataPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.*;
import app.system.fidelity.domain.AnalyticsData.*;
import app.system.fidelity.domain.enums.Gender;
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
        final List<Customer> allCustomers = customerRepository.findAll();
        final List<Appointment> allAppointments = appointmentRepository.findAll();
        final LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        final Long totalCustomers = (long) allCustomers.size();

        final BigDecimal totalRevenue = allAppointments.stream()
                .map(Appointment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal averageTicket = totalCustomers > 0 && allAppointments.size() > 0
                ? totalRevenue.divide(BigDecimal.valueOf(allAppointments.size()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final long activeCustomersLast30Days = allCustomers.stream()
                .filter(c -> c.getLastVisitDate() != null && c.getLastVisitDate().isAfter(thirtyDaysAgo))
                .count();

        final Double retentionRate = totalCustomers > 0
                ? (activeCustomersLast30Days * 100.0) / totalCustomers
                : 0.0;

        final Map<String, Long> customersByAgeGroup = calculateAgeGroups(allCustomers);
        final List<NeighborhoodData> topNeighborhoods = calculateTopNeighborhoods(allCustomers);
        final Map<Gender, Long> customersByGender = calculateGenderDistribution(allCustomers);

        final List<ChannelData> acquisitionChannels = calculateAcquisitionChannels(allCustomers);
        final List<StyleData> popularStyles = calculatePopularStyles(allCustomers);
        final Map<String, Long> preferredFrequency = calculatePreferredFrequency(allCustomers);

        final List<TopCustomer> topCustomers = calculateTopCustomers(allCustomers);
        final Map<String, BigDecimal> avgTicketByAge = calculateAvgTicketByAge(allCustomers);
        final List<ChannelRevenue> channelVsRevenue = calculateChannelRevenue(allCustomers, allAppointments);

        return AnalyticsData.builder()
                .totalCustomers(totalCustomers)
                .averageTicket(averageTicket)
                .totalRevenue(totalRevenue)
                .retentionRate(Math.round(retentionRate * 100.0) / 100.0)
                .customersByAgeGroup(customersByAgeGroup)
                .topNeighborhoods(topNeighborhoods)
                .customerByGender(customersByGender)
                .acquisitionChannels(acquisitionChannels)
                .popularStyles(popularStyles)
                .preferredFrequency(preferredFrequency)
                .topCustomers(topCustomers)
                .avgTicketByAge(avgTicketByAge)
                .channelVsRevenue(channelVsRevenue)
                .build();
    }

    private Map<String, Long> calculateAgeGroups(final List<Customer> customers) {
        final Map<String, Long> ageGroups = new LinkedHashMap<>();
        ageGroups.put("18-25", 0L);
        ageGroups.put("26-35", 0L);
        ageGroups.put("36-45", 0L);
        ageGroups.put("46-60", 0L);
        ageGroups.put("60+", 0L);
        ageGroups.put("Não informado", 0L);

        for (Customer customer : customers) {
            if (customer.getDateOfBirth() == null) {
                ageGroups.put("Não informado", ageGroups.get("Não informado") + 1);
                continue;
            }

            final int age = Period.between(customer.getDateOfBirth(), LocalDate.now()).getYears();

            if (age >= 18 && age <= 25) {
                ageGroups.put("18-25", ageGroups.get("18-25") + 1);
            } else if (age >= 26 && age <= 35) {
                ageGroups.put("26-35", ageGroups.get("26-35") + 1);
            } else if (age >= 36 && age <= 45) {
                ageGroups.put("36-45", ageGroups.get("36-45") + 1);
            } else if (age >= 46 && age <= 60) {
                ageGroups.put("46-60", ageGroups.get("46-60") + 1);
            } else if (age > 60) {
                ageGroups.put("60+", ageGroups.get("60+") + 1);
            }
        }

        return ageGroups;
    }

    private List<NeighborhoodData> calculateTopNeighborhoods(final List<Customer> customers) {
        return customers.stream()
                .filter(c -> c.getNeighborhood() != null && !c.getNeighborhood().isBlank())
                .collect(Collectors.groupingBy(Customer::getNeighborhood, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(entry -> NeighborhoodData.builder()
                        .neighborhood(entry.getKey())
                        .customerCount(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<String, Long> calculateGenderDistribution(final List<Customer> customers) {
        return customers.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getGender() != null ? c.getGender().name() : "NÃO_INFORMADO",
                        Collectors.counting()
                ));
    }

    private List<ChannelData> calculateAcquisitionChannels(final List<Customer> customers) {
        final long total = customers.size();

        return customers.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getReferralSource() != null ? c.getReferralSource().name() : "NAO_INFORMADO",
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> ChannelData.builder()
                        .channel(ReferralSource.valueOf(entry.getKey()))
                        .customerCount(entry.getValue())
                        .percentage(total > 0 ? (entry.getValue() * 100.0) / total : 0.0)
                        .build())
                .collect(Collectors.toList());
    }

    private List<StyleData> calculatePopularStyles(final List<Customer> customers) {
        final long total = customers.stream()
                .filter(c -> c.getPreferredStyle() != null)
                .count();

        return customers.stream()
                .filter(c -> c.getPreferredStyle() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getPreferredStyle().name(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> StyleData.builder()
                        .style(PreferredStyle.valueOf(entry.getKey()))
                        .count(entry.getValue())
                        .percentage(total > 0 ? (entry.getValue() * 100.0) / total : 0.0)
                        .build())
                .collect(Collectors.toList());
    }

    private Map<String, Long> calculatePreferredFrequency(final List<Customer> customers) {
        return customers.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getPreferredFrequency() != null ? c.getPreferredFrequency().name() : "NAO_INFORMADO",
                        Collectors.counting()
                ));
    }

    private List<TopCustomer> calculateTopCustomers(final List<Customer> customers) {
        return customers.stream()
                .filter(c -> c.getTotalSpent() != null && c.getTotalSpent().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .limit(10)
                .map(c -> TopCustomer.builder()
                        .name(c.getName())
                        .totalSpent(c.getTotalSpent())
                        .visitsCount(c.getServiceCount())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> calculateAvgTicketByAge(final List<Customer> customers) {
        final Map<String, List<Customer>> customersByAge = new LinkedHashMap<>();
        customersByAge.put("18-25", new ArrayList<>());
        customersByAge.put("26-35", new ArrayList<>());
        customersByAge.put("36-45", new ArrayList<>());
        customersByAge.put("46-60", new ArrayList<>());
        customersByAge.put("60+", new ArrayList<>());

        for (Customer customer : customers) {
            if (customer.getDateOfBirth() == null || customer.getTotalSpent() == null) continue;

            final int age = Period.between(customer.getDateOfBirth(), LocalDate.now()).getYears();

            if (age >= 18 && age <= 25) {
                customersByAge.get("18-25").add(customer);
            } else if (age >= 26 && age <= 35) {
                customersByAge.get("26-35").add(customer);
            } else if (age >= 36 && age <= 45) {
                customersByAge.get("36-45").add(customer);
            } else if (age >= 46 && age <= 60) {
                customersByAge.get("46-60").add(customer);
            } else if (age > 60) {
                customersByAge.get("60+").add(customer);
            }
        }

        final Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Customer>> entry : customersByAge.entrySet()) {
            final List<Customer> groupCustomers = entry.getValue();
            if (groupCustomers.isEmpty()) {
                result.put(entry.getKey(), BigDecimal.ZERO);
                continue;
            }

            final BigDecimal totalSpent = groupCustomers.stream()
                    .map(c -> c.getTotalSpent() != null ? c.getTotalSpent() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            final int totalVisits = groupCustomers.stream()
                    .mapToInt(c -> c.getServiceCount() != null ? c.getServiceCount() : 0)
                    .sum();

            final BigDecimal avgTicket = totalVisits > 0
                    ? totalSpent.divide(BigDecimal.valueOf(totalVisits), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            result.put(entry.getKey(), avgTicket);
        }

        return result;
    }

    private List<ChannelRevenue> calculateChannelRevenue(
            final List<Customer> customers,
            final List<Appointment> appointments
    ) {
        final Map<UUID, List<Appointment>> appointmentsByCustomer = appointments.stream()
                .filter(a -> a.getCustomerId() != null)
                .collect(Collectors.groupingBy(Appointment::getCustomerId));

        final Map<String, List<Customer>> customersByChannel = customers.stream()
                .filter(c -> c.getReferralSource() != null)
                .collect(Collectors.groupingBy(c -> c.getReferralSource().name()));

        return customersByChannel.entrySet().stream()
                .map(entry -> {
                    final String channel = entry.getKey();
                    final List<Customer> channelCustomers = entry.getValue();

                    BigDecimal totalRevenue = BigDecimal.ZERO;
                    int totalAppointments = 0;

                    for (Customer customer : channelCustomers) {
                        final List<Appointment> customerAppointments = appointmentsByCustomer.get(customer.getId());
                        if (customerAppointments != null) {
                            totalRevenue = totalRevenue.add(
                                    customerAppointments.stream()
                                            .map(Appointment::getTotalAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            );
                            totalAppointments += customerAppointments.size();
                        }
                    }

                    final BigDecimal avgTicket = totalAppointments > 0
                            ? totalRevenue.divide(BigDecimal.valueOf(totalAppointments), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return ChannelRevenue.builder()
                            .channel(ReferralSource.valueOf(channel))
                            .averageTicket(avgTicket)
                            .customerCount((long) channelCustomers.size())
                            .build();
                })
                .sorted((a, b) -> b.getAverageTicket().compareTo(a.getAverageTicket()))
                .collect(Collectors.toList());
    }
}