package app.system.fidelity.web.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record AppointmentPageResponse(

        List<AppointmentDetailResponse> content,

        int page,

        int size,

        @JsonProperty("total_elements")
        long totalElements,

        @JsonProperty("total_pages")
        int totalPages,

        String sort,

        String direction,

        @JsonProperty("has_next")
        boolean hasNext,

        @JsonProperty("has_previous")
        boolean hasPrevious

) {
}