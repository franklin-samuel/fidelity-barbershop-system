package app.system.fidelity.persistence.repository;

import app.system.fidelity.persistence.model.CustomerEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity> findByEmail(final String email);

    boolean existsByEmail(final String email);

    @Query("SELECT c FROM CustomerEntity c WHERE " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(c.phoneNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<CustomerEntity> searchByNameOrEmailOrPhoneNumber(@Param("searchTerm") String searchTerm);

    long countByServiceCountGreaterThanEqual(Integer haircutCount);

    @Query("SELECT c.gender, COUNT(c) FROM CustomerEntity c GROUP BY c.gender")
    List<Object[]> countGroupByGender();

    @Query("SELECT c.referralSource, COUNT(c) FROM CustomerEntity c GROUP BY c.referralSource")
    List<Object[]> countGroupByReferralSource();

    @Query("SELECT c.preferredFrequency, COUNT(c) FROM CustomerEntity c GROUP BY c.preferredFrequency")
    List<Object[]> countGroupByPreferredFrequency();

    @Query("SELECT c.preferredStyle, COUNT(c) FROM CustomerEntity c WHERE c.preferredStyle IS NOT NULL GROUP BY c.preferredStyle")
    List<Object[]> countGroupByPreferredStyle();

    @Query("SELECT c FROM CustomerEntity c WHERE c.totalSpent IS NOT NULL AND c.totalSpent > 0 ORDER BY c.totalSpent DESC")
    List<CustomerEntity> findTopByTotalSpentDesc(Pageable pageable);

}