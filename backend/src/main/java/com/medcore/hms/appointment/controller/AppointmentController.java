package com.medcore.hms.appointment.controller;

import com.medcore.hms.appointment.dto.AppointmentResponse;
import com.medcore.hms.appointment.dto.CancelAppointmentRequest;
import com.medcore.hms.appointment.dto.CreateAppointmentRequest;
import com.medcore.hms.appointment.dto.UpdateAppointmentRequest;
import com.medcore.hms.appointment.service.AppointmentService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.api.ApiResponse;
import com.medcore.hms.common.api.PagedApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DOCTOR', 'RECEPTIONIST')")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(
            AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> create(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        AppointmentResponse response =
                appointmentService.create(
                        request,
                        authenticatedUser.getUserAccount().getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Appointment created successfully"));
    }

    @GetMapping
    public PagedApiResponse<AppointmentResponse> findAll(
            @PageableDefault(size = 20, sort = "scheduledStartAt") Pageable pageable) {
        return PagedApiResponse.from(
                appointmentService.findAll(pageable),
                "Appointments retrieved successfully"
        );
    }

    @GetMapping("/{appointmentId}")
    public ApiResponse<AppointmentResponse> getById(
            @PathVariable Long appointmentId) {

        return ApiResponse.success(
                appointmentService.getById(appointmentId),
                "Appointment retrieved successfully");
    }

    @GetMapping("/doctor/{doctorId}")
    public PagedApiResponse<AppointmentResponse> findByDoctorId(
            @PathVariable Long doctorId,
            @PageableDefault(size = 20, sort = "scheduledStartAt")
            Pageable pageable) {

        return PagedApiResponse.from(
                appointmentService.findByDoctorId(
                        doctorId,
                        pageable),
                "Doctor appointments retrieved successfully");
    }

    @GetMapping("/patient/{patientId}")
    public PagedApiResponse<AppointmentResponse> findByPatientId(
            @PathVariable Long patientId,
            @PageableDefault(size = 20, sort = "scheduledStartAt")
            Pageable pageable) {

        return PagedApiResponse.from(
                appointmentService.findByPatientId(
                        patientId,
                        pageable),
                "Patient appointments retrieved successfully");
    }

    @PatchMapping("/{appointmentId}")
    public ApiResponse<AppointmentResponse> update(
            @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                appointmentService.update(
                        appointmentId,
                        request,
                        authenticatedUser.getUserAccount().getId()),
                "Appointment updated successfully");
    }

    @PatchMapping("/{appointmentId}/check-in")
    public ApiResponse<AppointmentResponse> checkIn(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                appointmentService.checkIn(
                        appointmentId,
                        authenticatedUser.getUserAccount().getId()),
                "Patient checked in successfully");
    }

    @PatchMapping("/{appointmentId}/start")
    public ApiResponse<AppointmentResponse> startConsultation(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                appointmentService.startConsultation(
                        appointmentId,
                        authenticatedUser.getUserAccount().getId()),
                "Consultation started successfully");
    }

    @PatchMapping("/{appointmentId}/complete")
    public ApiResponse<AppointmentResponse> complete(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                appointmentService.complete(
                        appointmentId,
                        authenticatedUser.getUserAccount().getId()),
                "Appointment completed successfully");
    }

    @PatchMapping("/{appointmentId}/cancel")
    public ApiResponse<AppointmentResponse> cancel(
            @PathVariable Long appointmentId,
            @Valid @RequestBody CancelAppointmentRequest request,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                appointmentService.cancel(
                        appointmentId,
                        request,
                        authenticatedUser.getUserAccount().getId()),
                "Appointment cancelled successfully");
    }

    @PatchMapping("/{appointmentId}/no-show")
    public ApiResponse<AppointmentResponse> markNoShow(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal
            CustomUserDetailsService.AccountPrincipal authenticatedUser) {

        return ApiResponse.success(
                appointmentService.markNoShow(
                        appointmentId,
                        authenticatedUser.getUserAccount().getId()),
                "Appointment marked as no-show successfully");
    }
}