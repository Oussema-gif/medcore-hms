package com.medcore.hms.appointment.repository;

import com.medcore.hms.appointment.domain.Appointment;
import com.medcore.hms.appointment.domain.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    Page<Appointment> findAllByDoctorId(
            Long doctorId,
            Pageable pageable);

    Page<Appointment> findAllByPatientId(
            Long patientId,
            Pageable pageable);

    Page<Appointment> findAllByStatus(
            AppointmentStatus status,
            Pageable pageable);

    @Query("""
    select count(a) > 0
    from Appointment a
    where a.doctorId = :doctorId
      and (:appointmentId is null or a.id <> :appointmentId)
      and a.status not in (
          com.medcore.hms.appointment.domain.AppointmentStatus.CANCELLED,
          com.medcore.hms.appointment.domain.AppointmentStatus.NO_SHOW
      )
      and a.scheduledStartAt < :scheduledEndAt
      and a.scheduledEndAt > :scheduledStartAt
    """)
    boolean existsDoctorConflict(
            @Param("doctorId") Long doctorId,
            @Param("appointmentId") Long appointmentId,
            @Param("scheduledStartAt") Instant scheduledStartAt,
            @Param("scheduledEndAt") Instant scheduledEndAt);

    @Query("""
    select count(a) > 0
    from Appointment a
    where a.patientId = :patientId
      and (:appointmentId is null or a.id <> :appointmentId)
      and a.status not in (
          com.medcore.hms.appointment.domain.AppointmentStatus.CANCELLED,
          com.medcore.hms.appointment.domain.AppointmentStatus.NO_SHOW
      )
      and a.scheduledStartAt < :scheduledEndAt
      and a.scheduledEndAt > :scheduledStartAt
    """)
    boolean existsPatientConflict(
            @Param("patientId") Long patientId,
            @Param("appointmentId") Long appointmentId,
            @Param("scheduledStartAt") Instant scheduledStartAt,
            @Param("scheduledEndAt") Instant scheduledEndAt);

    @Query("""
            select count(appointment)
            from Appointment appointment
            where appointment.doctorId = :doctorId
              and appointment.status in (
                  com.medcore.hms.appointment.domain.AppointmentStatus.CHECKED_IN,
                  com.medcore.hms.appointment.domain.AppointmentStatus.IN_PROGRESS
              )
              and appointment.scheduledStartAt >= :dayStart
              and appointment.scheduledStartAt < :dayEnd
            """)
    long countActiveQueueForDoctorOnDay(
            @Param("doctorId") Long doctorId,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd);

    @Query("""
           select count(a) from Appointment a
           where a.scheduledStartAt >= :startOfDay
             and a.scheduledStartAt < :startOfTomorrow
             and a.status not in :excludedStatuses
           """)
    long countTodayAppointments(
            @Param("startOfDay") Instant startOfDay,
            @Param("startOfTomorrow") Instant startOfTomorrow,
            @Param("excludedStatuses") Collection<AppointmentStatus> excludedStatuses);
}