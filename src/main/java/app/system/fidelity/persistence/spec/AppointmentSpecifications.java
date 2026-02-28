package app.system.fidelity.persistence.spec;

import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.PaymentMethod;
import app.system.fidelity.persistence.model.AppointmentEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppointmentSpecifications {

    private AppointmentSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<AppointmentEntity> startDateGreaterThanOrEqual(final LocalDateTime startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
        };
    }

    public static Specification<AppointmentEntity> endDateLessThanOrEqual(final LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }

    public static Specification<AppointmentEntity> typeEquals(final AppointmentType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    public static Specification<AppointmentEntity> barberIdEquals(final UUID barberId) {
        return (root, query, criteriaBuilder) -> {
            if (barberId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("barberId"), barberId);
        };
    }

    public static Specification<AppointmentEntity> customerIdEquals(final UUID customerId) {
        return (root, query, criteriaBuilder) -> {
            if (customerId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("customerId"), customerId);
        };
    }

    public static Specification<AppointmentEntity> paymentMethodEquals(final PaymentMethod paymentMethod) {
        return (root, query, criteriaBuilder) -> {
            if (paymentMethod == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("paymentMethod"), paymentMethod);
        };
    }

    public static Specification<AppointmentEntity> searchAnything(final String searchAnything) {
        return (root, query, criteriaBuilder) -> {
            if (searchAnything == null || searchAnything.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            final String searchPattern = "%" + searchAnything.toLowerCase() + "%";
            final List<Predicate> predicates = new ArrayList<>();

            try {
                final UUID uuid = UUID.fromString(searchAnything);
                predicates.add(criteriaBuilder.equal(root.get("id"), uuid));
                predicates.add(criteriaBuilder.equal(root.get("barberId"), uuid));
                predicates.add(criteriaBuilder.equal(root.get("customerId"), uuid));
                predicates.add(criteriaBuilder.equal(root.get("serviceId"), uuid));
                predicates.add(criteriaBuilder.equal(root.get("productId"), uuid));
            } catch (IllegalArgumentException ignored) {
                // Não é um UUID válido, ignora essa busca
            }

            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(criteriaBuilder.function("CAST", String.class, root.get("type"))),
                    searchPattern
            ));

            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(criteriaBuilder.function("CAST", String.class, root.get("paymentMethod"))),
                    searchPattern
            ));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

}
