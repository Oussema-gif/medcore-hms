package com.medcore.hms.medicine.repository;

import com.medcore.hms.medicine.domain.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findByIdAndDeletedAtIsNull(Long id);

    Page<Medicine> findAllByDeletedAtIsNull(Pageable pageable);

    boolean existsByMedicineCodeIgnoreCase(String medicineCode);

    boolean existsByMedicineCodeIgnoreCaseAndIdNot(
            String medicineCode,
            Long id);
}