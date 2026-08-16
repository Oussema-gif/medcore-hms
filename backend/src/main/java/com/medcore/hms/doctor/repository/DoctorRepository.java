package com.medcore.hms.doctor.repository;

import com.medcore.hms.doctor.domain.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByIdAndActiveTrue(Long id);

    Page<Doctor> findAllByActiveTrue(Pageable pageable);

    Optional<Doctor> findByProfessionalRegistrationNumber(
            String professionalRegistrationNumber);

    boolean existsByProfessionalRegistrationNumber(
            String professionalRegistrationNumber);
}
