package app.system.fidelity.domain.pagination;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Paging {

    private final int page;

    private final int size;

    private final String sort;

    private final String direction;

    public Paging(final int page, final int size, final String sort, final String direction) {
        this.page = Math.max(page, 0);
        this.size = size > 0 ? size : 10;
        this.sort = sort != null ? sort : "id";
        this.direction = direction != null ? direction : "ASC";
    }

}