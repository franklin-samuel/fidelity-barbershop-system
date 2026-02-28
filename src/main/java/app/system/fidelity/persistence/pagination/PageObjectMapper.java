package app.system.fidelity.persistence.pagination;


import app.system.fidelity.domain.pagination.PageObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;

public class PageObjectMapper {

    public static <E, D> PageObject<D> map(final Page<E> page, final Function<E, D> mapper) {
        if (page == null) {
            throw new IllegalArgumentException("Page must not be null");
        }

        if (mapper == null) {
            throw new IllegalArgumentException("Mapper function must not be null");
        }

        final List<D> content = page.getContent()
                .stream()
                .map(mapper)
                .toList();

        final var firstSortOrder = page.getSort().stream().findFirst();

        return PageObject.<D>builder()
                .content(content)
                .size(page.getSize())
                .page(page.getNumber())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .sort(firstSortOrder.map(Sort.Order::getProperty).orElse(null))
                .direction(firstSortOrder.map(sortOrder -> sortOrder.getDirection().name()).orElse(null))
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

}
