package com.medcore.hms.dashboard.service;

import com.medcore.hms.appointment.domain.AppointmentStatus;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.dashboard.dto.DashboardSummaryResponse;
import com.medcore.hms.laboratory.domain.LaboratoryRequestStatus;
import com.medcore.hms.laboratory.repository.LaboratoryRequestRepository;
import com.medcore.hms.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final LaboratoryRequestRepository laboratoryRequestRepository;
    private final Clock clock;

    public DashboardService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            LaboratoryRequestRepository laboratoryRequestRepository,
            Clock clock
    ) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.laboratoryRequestRepository = laboratoryRequestRepository;
        this.clock = clock;
    }

    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        Instant startOfDay = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant startOfTomorrow = today.plusDays(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        long activePatients = patientRepository.countActivePatients();
        long todayAppointments = appointmentRepository.countTodayAppointments(
                startOfDay,
                startOfTomorrow,
                EnumSet.of(AppointmentStatus.CANCELLED)
        );
        long pendingLaboratoryWork =
                laboratoryRequestRepository.countByStatusNotIn(
                        EnumSet.of(
                                LaboratoryRequestStatus.RELEASED,
                                LaboratoryRequestStatus.CANCELLED
                        )
                );

        return new DashboardSummaryResponse(
                activePatients,
                todayAppointments,
                pendingLaboratoryWork,
                "UP"
        );
    }
}