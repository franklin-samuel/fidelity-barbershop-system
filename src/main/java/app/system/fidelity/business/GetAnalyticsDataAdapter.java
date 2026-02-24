package app.system.fidelity.business;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.GetAnalyticsDataPort;
import app.system.fidelity.core.persistence.AppointmentRepositoryPort;
import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.*;
import app.system.fidelity.domain.AnalyticsData.*;
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

        final Long totalCustomers = (long) allCustomers.size();

        final BigDecimal totalRevenue = allAppointments.stream()
                .map(Appointment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final List<Appointment> appointmentsWithCustomer = allAppointments.stream()
                .filter(a -> a.getCustomerId() != null)
                .collect(Collectors.toList());

        final BigDecimal revenueWithCustomer = appointmentsWithCustomer.stream()
                .map(Appointment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal averageTicket = !appointmentsWithCustomer.isEmpty()
                ? revenueWithCustomer.divide(BigDecimal.valueOf(appointmentsWithCustomer.size()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        final long returningCustomers = allCustomers.stream()
                .filter(c -> c.getServiceCount() != null && c.getServiceCount() > 1)
                .count();

        final Double retentionRate = totalCustomers > 0
                ? (returningCustomers * 100.0) / totalCustomers
                : 0.0;

        final Map<String, Long> customersByAgeGroup = calculateAgeGroups(allCustomers);
        final Map<Gender, Long> customersByGender = calculateGenderDistribution(allCustomers);

        final List<ChannelData> acquisitionChannels = calculateAcquisitionChannels(allCustomers);
        final List<StyleData> popularStyles = calculatePopularStyles(allCustomers);
        final Map<PreferredFrequency, Long> preferredFrequency = calculatePreferredFrequency(allCustomers);

        final List<TopCustomer> topCustomers = calculateTopCustomers(allCustomers, allAppointments);
        final Map<String, BigDecimal> avgTicketByAge = calculateAvgTicketByAge(allCustomers, allAppointments);
        final List<ChannelRevenue> channelVsRevenue = calculateChannelRevenue(allCustomers, allAppointments);

        return AnalyticsData.builder()
                .totalCustomers(totalCustomers)
                .averageTicket(averageTicket)
                .totalRevenue(totalRevenue)
                .retentionRate(Math.round(retentionRate * 100.0) / 100.0)
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

    private Map<String, Long> calculateAgeGroups(final List<Customer> customers) {
        final Map<String, Long> ageGroups = new LinkedHashMap<>();
        ageGroups.put("0-10", 0L);
        ageGroups.put("11-17", 0L);
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

            if (age >= 0 && age <= 10) {
                ageGroups.put("0-10", ageGroups.get("0-10") + 1);
            } else if (age >= 11 && age <= 17) {
                ageGroups.put("11-17", ageGroups.get("11-17") + 1);
            } else if (age >= 18 && age <= 25) {
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

    private Map<Gender, Long> calculateGenderDistribution(final List<Customer> customers) {
        final Map<Gender, Long> distribution = new EnumMap<>(Gender.class);

        for (Gender gender : Gender.values()) {
            distribution.put(gender, 0L);
        }

        customers.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getGender() != null ? c.getGender() : Gender.NOT_INFORMED,
                        Collectors.counting()
                ))
                .forEach(distribution::put);

        return distribution;
    }

    private List<ChannelData> calculateAcquisitionChannels(final List<Customer> customers) {
        final long total = customers.size();

        final Map<ReferralSource, Long> grouped = customers.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getReferralSource() != null ? c.getReferralSource() : ReferralSource.NOT_INFORMED,
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> {
                    final ReferralSource channel = entry.getKey();
                    final Long count = entry.getValue();
                    final Double percentage = total > 0 ? (count * 100.0) / total : 0.0;

                    return ChannelData.builder()
                            .channel(channel)
                            .customerCount(count)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<StyleData> calculatePopularStyles(final List<Customer> customers) {
        final long total = customers.stream()
                .filter(c -> c.getPreferredStyle() != null)
                .count();

        return customers.stream()
                .filter(c -> c.getPreferredStyle() != null)
                .collect(Collectors.groupingBy(
                        Customer::getPreferredStyle,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> {
                    final PreferredStyle style = entry.getKey();
                    final Long count = entry.getValue();
                    final Double percentage = total > 0 ? (count * 100.0) / total : 0.0;

                    return StyleData.builder()
                            .style(style)
                            .count(count)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<PreferredFrequency, Long> calculatePreferredFrequency(final List<Customer> customers) {
        final Map<PreferredFrequency, Long> distribution = new EnumMap<>(PreferredFrequency.class);

        for (PreferredFrequency frequency : PreferredFrequency.values()) {
            distribution.put(frequency, 0L);
        }

        customers.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getPreferredFrequency() != null ? c.getPreferredFrequency() : PreferredFrequency.NOT_INFORMED,
                        Collectors.counting()
                ))
                .forEach(distribution::put);

        return distribution;
    }

    private List<TopCustomer> calculateTopCustomers(
            final List<Customer> customers,
            final List<Appointment> appointments
    ) {
        final Map<UUID, Long> appointmentsCountByCustomer = appointments.stream()
                .filter(a -> a.getCustomerId() != null)
                .collect(Collectors.groupingBy(Appointment::getCustomerId, Collectors.counting()));

        return customers.stream()
                .filter(c -> c.getTotalSpent() != null && c.getTotalSpent().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .limit(10)
                .map(c -> {
                    final Long visitsCount = appointmentsCountByCustomer.getOrDefault(c.getId(), 0L);
                    return TopCustomer.builder()
                            .name(c.getName())
                            .totalSpent(c.getTotalSpent())
                            .visitsCount(visitsCount.intValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> calculateAvgTicketByAge(
            final List<Customer> customers,
            final List<Appointment> appointments
    ) {
        final Map<UUID, List<Appointment>> appointmentsByCustomer = appointments.stream()
                .filter(a -> a.getCustomerId() != null)
                .collect(Collectors.groupingBy(Appointment::getCustomerId));

        final Map<String, List<Customer>> customersByAge = new LinkedHashMap<>();
        customersByAge.put("0-10", new ArrayList<>());
        customersByAge.put("11-17", new ArrayList<>());
        customersByAge.put("18-25", new ArrayList<>());
        customersByAge.put("26-35", new ArrayList<>());
        customersByAge.put("36-45", new ArrayList<>());
        customersByAge.put("46-60", new ArrayList<>());
        customersByAge.put("60+", new ArrayList<>());

        for (Customer customer : customers) {
            if (customer.getDateOfBirth() == null) continue;

            final int age = Period.between(customer.getDateOfBirth(), LocalDate.now()).getYears();

            if (age >= 0 && age <= 10) {
                customersByAge.get("0-10").add(customer);
            } else if (age >= 11 && age <= 17) {
                customersByAge.get("11-17").add(customer);
            } else if (age >= 18 && age <= 25) {
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

            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalAppointments = 0;

            for (Customer customer : groupCustomers) {
                final List<Appointment> customerAppointments = appointmentsByCustomer.get(customer.getId());
                if (customerAppointments != null && !customerAppointments.isEmpty()) {
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

        final Map<ReferralSource, List<Customer>> customersByChannel = customers.stream()
                .filter(c -> c.getReferralSource() != null)
                .collect(Collectors.groupingBy(Customer::getReferralSource));

        return customersByChannel.entrySet().stream()
                .map(entry -> {
                    final ReferralSource channel = entry.getKey();
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
                            .channel(channel)
                            .averageTicket(avgTicket)
                            .customerCount((long) channelCustomers.size())
                            .build();
                })
                .sorted((a, b) -> b.getAverageTicket().compareTo(a.getAverageTicket()))
                .collect(Collectors.toList());
    }
}