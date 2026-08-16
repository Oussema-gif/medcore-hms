package com.medcore.hms.doctoravailability.repository;

import com.medcore.hms.doctoravailability.domain.DoctorAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface DoctorAvailabilityRepository
        extends JpaRepository<DoctorAvailability, Long> {

    Page<DoctorAvailability> findAllByDoctorIdAndActiveTrue(
            Long doctorId,
            Pageable pageable);

    @Query("""
            select count(availability) > 0
            from DoctorAvailability availability
            where availability.doctorId = :doctorId
              and availability.dayOfWeek = :dayOfWeek
              and availability.active = true
              and availability.effectiveFrom <= :appointmentDate
              and (
                  availability.effectiveUntil is null
                  or availability.effectiveUntil >= :appointmentDate
              )
              and availability.startTime <= :startTime
              and availability.endTime >= :endTime
            """)
    boolean existsCoveringInterval(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") Short dayOfWeek,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}
