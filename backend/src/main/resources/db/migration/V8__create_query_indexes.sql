CREATE INDEX idx_user_account__email_status
    ON user_account (email, status);

CREATE INDEX idx_doctor_availability__doctor_effective_day
    ON doctor_availability (doctor_id, effective_from, effective_until, day_of_week, active);

CREATE INDEX idx_appointment__doctor_active_window
    ON appointment (doctor_id, status, scheduled_start_at, scheduled_end_at);

CREATE INDEX idx_appointment__patient_status_start
    ON appointment (patient_id, status, scheduled_start_at);

CREATE INDEX idx_appointment__department_status_start
    ON appointment (department_id, status, scheduled_start_at);

CREATE INDEX idx_medical_record__patient_release_encounter
    ON medical_record (patient_id, patient_visible, released_at, encounter_at);

CREATE INDEX idx_laboratory_request__patient_status_requested
    ON laboratory_request (patient_id, status, requested_at);

CREATE INDEX idx_laboratory_request__release_worklist
    ON laboratory_request (status, released_at, requested_at);

CREATE INDEX idx_laboratory_result__item_release_recorded
    ON laboratory_result (laboratory_request_item_id, released_at, recorded_at);

CREATE INDEX idx_prescription__patient_status_expiry
    ON prescription (patient_id, status, expires_at);

CREATE INDEX idx_inventory_batch__medicine_stock_expiry
    ON inventory_batch (medicine_id, active, quantity_on_hand, expiry_date);

CREATE INDEX idx_stock_movement__type_occurred
    ON stock_movement (movement_type, occurred_at);

CREATE INDEX idx_invoice__patient_status_balance
    ON invoice (patient_id, status, balance_amount);

CREATE INDEX idx_invoice__issue_due_status
    ON invoice (issue_date, due_date, status);

CREATE INDEX idx_payment__invoice_status_paid
    ON payment (invoice_id, status, paid_at);

CREATE INDEX idx_notification__user_created
    ON notification (user_account_id, created_at);

CREATE INDEX idx_audit_log__entity_action_occurred
    ON audit_log (entity_type, entity_id, action, occurred_at);
