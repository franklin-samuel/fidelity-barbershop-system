package app.system.fidelity.web.pagination;

import app.system.fidelity.domain.pagination.PageObject;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PageWrapperMapper {

    public static <E, D> PageWrapper<D> map(
            final PageObject<E> pageObject,
            final Function<E, D> mapper,
            final String urlBase,
            final Map<String, Object> filters
    ) {
        if (pageObject == null) {
            throw new IllegalArgumentException("PageObject must not be null");
        }

        if (mapper == null) {
            throw new IllegalArgumentException("Mapper must not be null");
        }

        final List<D> content = pageObject.getContent()
                .stream()
                .map(mapper)
                .toList();

        return PageWrapper.<D>builder()
                .content(content)
                .page(pageObject.getPage())
                .size(pageObject.getSize())
                .totalElements(pageObject.getTotalElements())
                .totalPages(pageObject.getTotalPages())
                .sort(pageObject.getSort())
                .direction(pageObject.getDirection())
                .hasNext(pageObject.isHasNext())
                .hasPrevious(pageObject.isHasPrevious())
                .urlBase(urlBase)
                .filters(filters)
                .build();
    }

}
