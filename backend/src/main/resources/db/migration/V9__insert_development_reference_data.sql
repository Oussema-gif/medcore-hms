INSERT INTO department (
    code,
    name,
    description,
    active
) VALUES
    ('GEN-MED', 'General Medicine', 'General medical consultations and follow-up care.', TRUE),
    ('CARDIO', 'Cardiology', 'Cardiovascular assessment and treatment services.', TRUE),
    ('PEDIATRICS', 'Pediatrics', 'Medical care for infants, children, and adolescents.', TRUE),
    ('LAB', 'Laboratory Services', 'Laboratory sample processing and diagnostic results.', TRUE),
    ('PHARMACY', 'Pharmacy', 'Medicine dispensing and pharmacy inventory operations.', TRUE);

INSERT INTO medicine (
    medicine_code,
    name,
    generic_name,
    dosage_form,
    strength,
    unit,
    reorder_level,
    active
) VALUES
    ('PARA-500-TAB', 'Paracetamol 500 mg tablet', 'Paracetamol', 'TABLET', '500 mg', 'tablet', 100.0000, TRUE),
    ('AMOX-500-CAP', 'Amoxicillin 500 mg capsule', 'Amoxicillin', 'CAPSULE', '500 mg', 'capsule', 50.0000, TRUE),
    ('IBU-400-TAB', 'Ibuprofen 400 mg tablet', 'Ibuprofen', 'TABLET', '400 mg', 'tablet', 50.0000, TRUE),
    ('NS-500-IV', 'Sodium chloride 0.9% infusion', 'Sodium chloride', 'INFUSION', '0.9%', 'bag', 20.0000, TRUE),
    ('OMEP-20-CAP', 'Omeprazole 20 mg capsule', 'Omeprazole', 'CAPSULE', '20 mg', 'capsule', 50.0000, TRUE);
