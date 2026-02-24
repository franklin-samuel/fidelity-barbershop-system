package app.system.fidelity.persistence.adapter;

import app.system.fidelity.core.persistence.CustomerRepositoryPort;
import app.system.fidelity.domain.Customer;
import app.system.fidelity.domain.enums.Gender;
import app.system.fidelity.domain.enums.PreferredFrequency;
import app.system.fidelity.domain.enums.PreferredStyle;
import app.system.fidelity.domain.enums.ReferralSource;
import app.system.fidelity.persistence.mapper.CustomerMapper;
import app.system.fidelity.persistence.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Optional.of;

@Repository
@RequiredArgsConstructor
@Transactional
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    public Optional<Customer> get(final UUID id) {
        return repository.findById(id).map(mapper::map);
    }

    @Override
    public Customer save(final Customer model) {
        return of(repository.save(mapper.map(model)))
                .map(mapper::map)
                .orElseThrow(() -> new IllegalStateException("Failed to save customer"));
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public Optional<Customer> findByEmail(final String email) {
        return repository.findByEmail(email).map(mapper::map);
    }

    @Override
    public boolean existsByEmail(final String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public List<Customer> findByNameOrEmailOrPhoneNumber(final String searchTerm) {
        return repository.searchByNameOrEmailOrPhoneNumber(searchTerm)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public long countAll() {
        return repository.count();
    }

    @Override
    public long countByServiceCountGreaterThanEqual(final int haircutCount) {
        return repository.countByServiceCountGreaterThanEqual(haircutCount);
    }

    @Override
    public List<Customer> findAllById(final Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findAllById(ids)
                .stream()
                .map(mapper::map)
                .toList();
    }

    @Override
    public Map<Gender, Long> countGroupByGender() {
        final Map<Gender, Long> result = new EnumMap<>(Gender.class);
        for (final Gender g : Gender.values()) {
            result.put(g, 0L);
        }
        repository.countGroupByGender().forEach(row -> {
            final Gender gender = row[0] != null ? (Gender) row[0] : Gender.NOT_INFORMED;
            result.put(gender, (Long) row[1]);
        });
        return result;
    }

    @Override
    public Map<ReferralSource, Long> countGroupByReferralSource() {
        final Map<ReferralSource, Long> result = new LinkedHashMap<>();
        repository.countGroupByReferralSource().forEach(row -> {
            final ReferralSource source = row[0] != null ? (ReferralSource) row[0] : ReferralSource.NOT_INFORMED;
            result.merge(source, (Long) row[1], Long::sum);
        });
        return result;
    }

    @Override
    public Map<PreferredFrequency, Long> countGroupByPreferredFrequency() {
        final Map<PreferredFrequency, Long> result = new EnumMap<>(PreferredFrequency.class);
        for (final PreferredFrequency f : PreferredFrequency.values()) {
            result.put(f, 0L);
        }
        repository.countGroupByPreferredFrequency().forEach(row -> {
            final PreferredFrequency freq = row[0] != null ? (PreferredFrequency) row[0] : PreferredFrequency.NOT_INFORMED;
            result.put(freq, (Long) row[1]);
        });
        return result;
    }

    @Override
    public Map<PreferredStyle, Long> countGroupByPreferredStyle() {
        final Map<PreferredStyle, Long> result = new LinkedHashMap<>();
        repository.countGroupByPreferredStyle().forEach(row -> {
            final PreferredStyle style = (PreferredStyle) row[0];
            result.put(style, (Long) row[1]);
        });
        return result;
    }

    @Override
    public List<Customer> findTopCustomersByTotalSpent(final int limit) {
        return repository.findTopByTotalSpentDesc(PageRequest.of(0, limit))
                .stream()
                .map(mapper::map)
                .toList();
    }

}