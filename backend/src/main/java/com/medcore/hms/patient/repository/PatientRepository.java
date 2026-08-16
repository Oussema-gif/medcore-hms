package com.medcore.hms.patient.repository;

import com.medcore.hms.patient.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByIdAndDeletedAtIsNull(Long id);

    Page<Patient> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Patient> findByMedicalRecordNumber(String medicalRecordNumber);

    boolean existsByMedicalRecordNumber(String medicalRecordNumber);

    @Query("select count(p) from Patient p where p.active = true and p.deletedAt is null")
    long countActivePatients();
}