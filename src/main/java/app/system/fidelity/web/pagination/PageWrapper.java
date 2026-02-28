package app.system.fidelity.web.pagination;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
public class PageWrapper<T> {

    private final List<T> content;

    private final int size;

    private final int page;

    private final long totalElements;

    private final int totalPages;

    private final String sort;

    private final String direction;

    private final boolean hasNext;

    private final boolean hasPrevious;

    private final String urlBase;

    private final Map<String, Object> filters;

    public String getDescriptionPagination() {
        final int from = getTotalElements() > 0 ? (getPage() * getSize() + 1) : 0;
        final int to = Math.min((getPage() + 1) * getSize(), (int) getTotalElements());
        return String.format("%d–%d de %d", from, to, getTotalElements());
    }

    public boolean isAsc() {
        return StringUtils.hasText(direction) && direction.equalsIgnoreCase("asc");
    }

    public boolean isDesc() {
        return StringUtils.hasText(direction) && direction.equalsIgnoreCase("desc");
    }

    public String getUrl() {
        if (!StringUtils.hasText(urlBase)) {
            return "";
        }

        if (filters == null || filters.isEmpty()) {
            return urlBase;
        }

        final String queryParams = filters.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> entry.getKey() + "=" + encodeValue(entry.getValue().toString()))
                .collect(Collectors.joining("&"));

        return urlBase + (urlBase.contains("?") ? "&" : "?") + queryParams;
    }

    private String encodeValue(final String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return value;
        }
    }

}
