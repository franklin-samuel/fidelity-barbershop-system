package app.system.fidelity.core.persistence;

import app.system.fidelity.core.persistence.commons.BaseRepositoryPort;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.enums.Gender;
import app.system.fidelity.domain.enums.PreferredFrequency;
import app.system.fidelity.domain.enums.PreferredStyle;
import app.system.fidelity.domain.enums.ReferralSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CustomerRepositoryPort extends BaseRepositoryPort<Customer> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Customer> findByNameOrEmailOrPhoneNumber(String searchTerm);

    long countAll();

    long countByServiceCountGreaterThanEqual(int haircutCount);

    List<Customer> findAllById(Set<UUID> ids);

    Map<Gender, Long> countGroupByGender();

    Map<ReferralSource, Long> countGroupByReferralSource();

    Map<PreferredFrequency, Long> countGroupByPreferredFrequency();

    Map<PreferredStyle, Long> countGroupByPreferredStyle();

    List<Customer> findTopCustomersByTotalSpent(int limit);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

}