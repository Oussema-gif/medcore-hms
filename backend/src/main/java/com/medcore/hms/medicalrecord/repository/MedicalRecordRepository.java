package com.medcore.hms.medicalrecord.repository;

import com.medcore.hms.medicalrecord.domain.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository
        extends JpaRepository<MedicalRecord, Long> {

    Page<MedicalRecord> findAllByPatientId(
            Long patientId,
            Pageable pageable);

    Page<MedicalRecord> findAllByDoctorId(
            Long doctorId,
            Pageable pageable);
}
