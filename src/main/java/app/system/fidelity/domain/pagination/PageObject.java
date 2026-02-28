package app.system.fidelity.domain.pagination;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class PageObject<T> {

    private final List<T> content;

    private final int page;

    private final int size;

    private final long totalElements;

    private final int totalPages;

    private final String sort;

    private final String direction;

    private final boolean hasNext;

    private final boolean hasPrevious;

}
