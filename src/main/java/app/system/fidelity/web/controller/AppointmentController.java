package app.system.fidelity.web.controller;

import app.system.fidelity.core.Context;
import app.system.fidelity.core.business.DeleteAppointmentPort;
import app.system.fidelity.core.business.GetAppointmentDetailsPagedPort;
import app.system.fidelity.core.business.RegisterAppointmentPort;
import app.system.fidelity.core.business.UpdateAppointmentPort;
import app.system.fidelity.domain.Appointment;
import app.system.fidelity.domain.AppointmentDetail;
import app.system.fidelity.domain.AppointmentFilterList;
import app.system.fidelity.domain.enums.AppointmentType;
import app.system.fidelity.domain.enums.Role;
import app.system.fidelity.domain.pagination.PageObject;
import app.system.fidelity.domain.pagination.Paging;
import app.system.fidelity.security.model.CustomUserDetails;
import app.system.fidelity.web.commons.ApiResponse;
import app.system.fidelity.web.mapper.AppointmentMapper;
import app.system.fidelity.web.model.request.AppointmentFilters;
import app.system.fidelity.web.model.request.AppointmentRequest;
import app.system.fidelity.web.model.request.AppointmentUpdateRequest;
import app.system.fidelity.web.model.response.AppointmentDetailResponse;
import app.system.fidelity.web.model.response.AppointmentPageResponse;
import app.system.fidelity.web.model.response.AppointmentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final RegisterAppointmentPort registerAppointmentPort;
    private final UpdateAppointmentPort updateAppointmentPort;
    private final DeleteAppointmentPort deleteAppointmentPort;
    private final GetAppointmentDetailsPagedPort getAppointmentDetailsPagedPort;
    private final AppointmentMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<AppointmentPageResponse>> listPaged(
            final AppointmentFilters filters,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "30") final int size,
            @RequestParam(defaultValue = "createdAt") final String sort,
            @RequestParam(defaultValue = "desc") final String direction,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        final boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.name()));

        final UUID effectiveBarberId = !isAdmin && filters.getBarberId() == null
                ? userDetails.getUserId()
                : filters.getBarberId();

        final AppointmentFilterList appointmentFilters = AppointmentFilterList.builder()
                .startDate(filters.getStartDate())
                .endDate(filters.getEndDate())
                .type(filters.getType())
                .barberId(effectiveBarberId)
                .customerId(filters.getCustomerId())
                .paymentMethod(filters.getPaymentMethod())
                .searchAnything(filters.getSearchAnything())
                .paging(Paging.builder()
                        .page(page)
                        .size(size)
                        .sort(sort)
                        .direction(direction)
                        .build())
                .build();

        final Context context = new Context(appointmentFilters);
        final PageObject<AppointmentDetail> pageResult = getAppointmentDetailsPagedPort.execute(context);

        final List<AppointmentDetailResponse> content = pageResult.getContent().stream()
                .map(detail -> mapper.mapToDetailResponse(detail, isAdmin))
                .toList();

        final AppointmentPageResponse response = AppointmentPageResponse.builder()
                .content(content)
                .page(pageResult.getPage())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .sort(pageResult.getSort())
                .direction(pageResult.getDirection())
                .hasNext(pageResult.isHasNext())
                .hasPrevious(pageResult.isHasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/service")
    public ResponseEntity<ApiResponse<AppointmentResponse>> registerService(
            @Valid @RequestBody final AppointmentRequest request,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        final Role userRole = userDetails.getAuthorities().stream()
                .map(a -> Role.valueOf(a.getAuthority()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuário sem role definida"));

        final Appointment form = Appointment.builder()
                .barberId(request.getBarberId())
                .type(AppointmentType.SERVICE)
                .paymentMethod(request.getPaymentMethod())
                .customerId(request.getCustomerId())
                .serviceId(request.getServiceId())
                .productId(request.getProductId())
                .tip(request.getTip())
                .build();

        final Context context = new Context(form);
        context.putProperty("authenticatedUserId", userDetails.getUserId());
        context.putProperty("authenticatedUserRole", userRole);

        final Appointment savedAppointment = registerAppointmentPort.execute(context);

        final AppointmentResponse response = mapper.mapToResponse(savedAppointment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Atendimento registrado com sucesso"));
    }

    @PostMapping("/product")
    public ResponseEntity<ApiResponse<AppointmentResponse>> registerProduct(
            @Valid @RequestBody final AppointmentRequest request,
            @AuthenticationPrincipal final CustomUserDetails userDetails
    ) {
        final Role userRole = userDetails.getAuthorities().stream()
                .map(a -> Role.valueOf(a.getAuthority()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuário sem role definida"));

        final Appointment form = Appointment.builder()
                .barberId(request.getBarberId())
                .type(AppointmentType.PRODUCT)
                .paymentMethod(request.getPaymentMethod())
                .customerId(request.getCustomerId())
                .serviceId(request.getServiceId())
                .productId(request.getProductId())
                .tip(request.getTip())
                .build();

        final Context context = new Context(form);
        context.putProperty("authenticatedUserId", userDetails.getUserId());
        context.putProperty("authenticatedUserRole", userRole);

        final Appointment savedAppointment = registerAppointmentPort.execute(context);

        final AppointmentResponse response = mapper.mapToResponse(savedAppointment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Atendimento registrado com sucesso"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> update(
            @PathVariable final UUID id,
            @Valid @RequestBody final AppointmentUpdateRequest request
    ) {
        final Context context = new Context();
        context.putProperty("appointmentId", id);
        context.putProperty("customerId", request.customerId());

        final Appointment updatedAppointment = updateAppointmentPort.execute(context);

        final AppointmentResponse response = mapper.mapToResponse(updatedAppointment);

        return ResponseEntity.ok(ApiResponse.success(response, "Cliente vinculado ao atendimento com sucesso"));
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable final UUID id) {
        final Context context = new Context(id);

        deleteAppointmentPort.execute(context);

        return ResponseEntity.ok(ApiResponse.success("Atendimento deletado com sucesso"));
    }
}