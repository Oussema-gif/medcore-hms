package com.medcore.hms.diagnosis.repository;

import com.medcore.hms.diagnosis.domain.Diagnosis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisRepository
        extends JpaRepository<Diagnosis, Long> {

    Page<Diagnosis> findAllByMedicalRecordId(
            Long medicalRecordId,
            Pageable pageable);

    boolean existsByMedicalRecordIdAndPrimaryTrue(
            Long medicalRecordId);
}
