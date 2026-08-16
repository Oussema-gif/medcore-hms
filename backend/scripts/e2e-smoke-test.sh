#!/usr/bin/env bash

set -u

BASE_URL="http://localhost:8080/api/v1"

PASS=0
FAIL=0

log_ok() {
  echo "✓ $1"
  PASS=$((PASS + 1))
}

log_fail() {
  echo "✗ $1"
  echo "  HTTP: $2"
  echo "  Response:"
  echo "$3" | jq . 2>/dev/null || echo "$3"
  FAIL=$((FAIL + 1))
}

require_json_id() {
  local name="$1"
  local response="$2"

  local id
  id="$(echo "$response" | jq -r '.data.id // empty')"

  if [[ -z "$id" ]]; then
    echo "✗ $name did not return data.id"
    echo "$response" | jq . 2>/dev/null || echo "$response"
    exit 1
  fi

  echo "$id"
}

request() {
  local method="$1"
  local url="$2"
  local body="${3:-}"

  if [[ -n "$body" ]]; then
    curl -sS \
      -X "$method" \
      "$BASE_URL$url" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "$body" \
      -w $'\n%{http_code}'
  else
    curl -sS \
      -X "$method" \
      "$BASE_URL$url" \
      -H "Authorization: Bearer $TOKEN" \
      -w $'\n%{http_code}'
  fi
}

echo
echo "======================================================"
echo "              MEDCORE HMS E2E SMOKE TEST"
echo "======================================================"
echo

# ------------------------------------------------------
# 1. LOGIN
# ------------------------------------------------------

LOGIN_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "medcore12"
  }')"

TOKEN="$(echo "$LOGIN_RESPONSE" | jq -r '.tokens.accessToken // empty')"

if [[ -z "$TOKEN" ]]; then
  echo "✗ Authentication failed"
  echo "$LOGIN_RESPONSE" | jq . 2>/dev/null || echo "$LOGIN_RESPONSE"
  exit 1
fi

log_ok "Authentication"


# ------------------------------------------------------
# 2. CREATE DOCTOR USER ACCOUNT
# ------------------------------------------------------

STAMP="$(date +%s)"

DOCTOR_USERNAME="smoketest_doctor_${STAMP}"
DOCTOR_EMAIL="smoketest.doctor.${STAMP}@medcore.local"

USER_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/users" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$DOCTOR_USERNAME\",
    \"displayName\": \"Smoke Test Doctor\",
    \"email\": \"$DOCTOR_EMAIL\",
    \"password\": \"medcore123\",
    \"role\": \"DOCTOR\"
  }")"

DOCTOR_USER_ID="$(require_json_id "Doctor user account" "$USER_RESPONSE")"

log_ok "Create doctor user account → ID $DOCTOR_USER_ID"


# ------------------------------------------------------
# 3. CREATE PATIENT
# ------------------------------------------------------

PATIENT_MRN="SMOKE-${STAMP}"

PATIENT_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/patients" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"medicalRecordNumber\": \"$PATIENT_MRN\",
    \"firstName\": \"Smoke\",
    \"lastName\": \"Patient\",
    \"dateOfBirth\": \"1995-05-15\",
    \"sex\": \"OTHER\",
    \"email\": \"smoke.patient.${STAMP}@medcore.local\",
    \"phoneNumber\": \"+21670000000\",
    \"addressLine1\": \"MedCore Test Street\",
    \"city\": \"Monastir\",
    \"state\": \"Monastir\",
    \"postalCode\": \"5000\",
    \"country\": \"Tunisia\",
    \"emergencyContactName\": \"Smoke Contact\",
    \"emergencyContactPhone\": \"+21671111111\",
    \"bloodGroup\": \"O+\",
    \"allergies\": \"None\"
  }")"

PATIENT_ID="$(require_json_id "Patient" "$PATIENT_RESPONSE")"

log_ok "Create patient → ID $PATIENT_ID"


# ------------------------------------------------------
# 4. CREATE DOCTOR
# ------------------------------------------------------

DOCTOR_REG="SMOKE-DOC-${STAMP}"

DOCTOR_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/doctors" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"userAccountId\": $DOCTOR_USER_ID,
    \"departmentId\": 1,
    \"professionalRegistrationNumber\": \"$DOCTOR_REG\",
    \"firstName\": \"Smoke\",
    \"lastName\": \"Doctor\",
    \"specialty\": \"General Medicine\",
    \"phoneNumber\": \"+21672222222\",
    \"email\": \"$DOCTOR_EMAIL\",
    \"consultationDurationMinutes\": 30
  }")"

DOCTOR_ID="$(require_json_id "Doctor" "$DOCTOR_RESPONSE")"

log_ok "Create doctor → ID $DOCTOR_ID"


# ------------------------------------------------------
# 5. CREATE DOCTOR AVAILABILITY
# Monday 2026-08-17, 09:00 → 17:00 UTC
# ------------------------------------------------------

AVAILABILITY_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/doctor-availability" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"doctorId\": $DOCTOR_ID,
    \"dayOfWeek\": 1,
    \"startTime\": \"09:00:00\",
    \"endTime\": \"17:00:00\",
    \"effectiveFrom\": \"2026-08-17\",
    \"effectiveUntil\": null
  }")"

AVAILABILITY_ID="$(require_json_id "Doctor availability" "$AVAILABILITY_RESPONSE")"

log_ok "Create doctor availability → ID $AVAILABILITY_ID"


# ------------------------------------------------------
# 6. CREATE APPOINTMENT
# Monday 2026-08-17, 10:00 → 10:30 UTC
# ------------------------------------------------------

APPOINTMENT_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/appointments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"patientId\": $PATIENT_ID,
    \"doctorId\": $DOCTOR_ID,
    \"departmentId\": 1,
    \"scheduledStartAt\": \"2026-08-17T10:00:00Z\",
    \"scheduledEndAt\": \"2026-08-17T10:30:00Z\",
    \"reason\": \"Smoke test consultation\",
    \"schedulingNotes\": \"Automated MedCore E2E test\"
  }")"

APPOINTMENT_ID="$(require_json_id "Appointment" "$APPOINTMENT_RESPONSE")"

log_ok "Create appointment → ID $APPOINTMENT_ID"


# ------------------------------------------------------
# 7. CHECK APPOINTMENT
# ------------------------------------------------------

CHECKIN_RESPONSE="$(request \
  PATCH \
  "/appointments/$APPOINTMENT_ID/check-in")"

CHECKIN_CODE="$(echo "$CHECKIN_RESPONSE" | tail -n1)"
CHECKIN_BODY="$(echo "$CHECKIN_RESPONSE" | sed '$d')"

if [[ "$CHECKIN_CODE" == "200" ]]; then
  log_ok "Appointment check-in"
else
  log_fail "Appointment check-in" "$CHECKIN_CODE" "$CHECKIN_BODY"
fi


# ------------------------------------------------------
# 8. CREATE MEDICAL RECORD
# ------------------------------------------------------

MEDICAL_RECORD_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/medical-records" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"patientId\": $PATIENT_ID,
    \"doctorId\": $DOCTOR_ID,
    \"appointmentId\": $APPOINTMENT_ID,
    \"encounterAt\": \"2026-08-17T10:00:00Z\",
    \"symptoms\": \"Automated smoke-test symptoms\",
    \"clinicalNotes\": \"Automated MedCore end-to-end medical record test.\"
  }")"

MEDICAL_RECORD_ID="$(require_json_id "Medical record" "$MEDICAL_RECORD_RESPONSE")"

log_ok "Create medical record → ID $MEDICAL_RECORD_ID"


# ------------------------------------------------------
# 9. CREATE VITAL SIGNS
# ------------------------------------------------------

VITAL_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/vital-signs" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"medicalRecordId\": $MEDICAL_RECORD_ID,
    \"recordedAt\": \"2026-08-17T10:05:00Z\",
    \"temperatureCelsius\": 36.8,
    \"systolicBp\": 120,
    \"diastolicBp\": 80,
    \"heartRate\": 72,
    \"respiratoryRate\": 16,
    \"oxygenSaturation\": 98,
    \"weightKg\": 72.5,
    \"heightCm\": 178
  }")"

VITAL_ID="$(require_json_id "Vital signs" "$VITAL_RESPONSE")"

log_ok "Create vital signs → ID $VITAL_ID"


# ------------------------------------------------------
# 10. CREATE PRIMARY DIAGNOSIS
# ------------------------------------------------------

DIAGNOSIS_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/diagnoses" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"medicalRecordId\": $MEDICAL_RECORD_ID,
    \"codeSystem\": \"ICD-10\",
    \"diagnosisCode\": \"Z00.00\",
    \"description\": \"Routine medical examination\",
    \"diagnosisType\": \"PRIMARY\",
    \"primary\": true
  }")"

DIAGNOSIS_ID="$(require_json_id "Diagnosis" "$DIAGNOSIS_RESPONSE")"

log_ok "Create primary diagnosis → ID $DIAGNOSIS_ID"


# ------------------------------------------------------
# 11. CREATE LABORATORY REQUEST
# ------------------------------------------------------

LAB_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/laboratory-requests" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"patientId\": $PATIENT_ID,
    \"orderingDoctorId\": $DOCTOR_ID,
    \"appointmentId\": $APPOINTMENT_ID,
    \"medicalRecordId\": $MEDICAL_RECORD_ID,
    \"priority\": \"ROUTINE\",
    \"clinicalNotes\": \"Automated laboratory smoke test\",
    \"items\": [
      {
        \"testCode\": \"CBC\",
        \"testName\": \"Complete Blood Count\",
        \"sortOrder\": 1
      }
    ]
  }")"

LAB_ID="$(require_json_id "Laboratory request" "$LAB_RESPONSE")"
LAB_ITEM_ID="$(echo "$LAB_RESPONSE" | jq -r '.data.items[0].id // empty')"

log_ok "Create laboratory request → ID $LAB_ID"


# ------------------------------------------------------
# 12. CREATE LABORATORY RESULT
# ------------------------------------------------------

if [[ -n "$LAB_ITEM_ID" ]]; then

  RESULT_RESPONSE="$(curl -sS \
    -X POST "$BASE_URL/laboratory-results" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"laboratoryRequestItemId\": $LAB_ITEM_ID,
      \"resultValue\": 13.2,
      \"resultText\": \"Normal\",
      \"unit\": \"g/dL\",
      \"referenceRange\": \"12.0-16.0\",
      \"interpretation\": \"Within reference range\"
    }")"

  RESULT_ID="$(require_json_id "Laboratory result" "$RESULT_RESPONSE")"

  log_ok "Create laboratory result → ID $RESULT_ID"

else
  echo "✗ Laboratory request did not return an item ID"
  FAIL=$((FAIL + 1))
fi


# ------------------------------------------------------
# 13. CREATE PRESCRIPTION
# Medicine ID 1 exists in the current seed data
# ------------------------------------------------------

PRESCRIPTION_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/prescriptions" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"patientId\": $PATIENT_ID,
    \"doctorId\": $DOCTOR_ID,
    \"appointmentId\": $APPOINTMENT_ID,
    \"medicalRecordId\": $MEDICAL_RECORD_ID,
    \"expiresAt\": \"2026-09-17T00:00:00Z\",
    \"notes\": \"Automated smoke-test prescription\",
    \"items\": [
      {
        \"medicineId\": 1,
        \"dosage\": \"500 mg\",
        \"route\": \"ORAL\",
        \"frequency\": \"TWICE_DAILY\",
        \"durationDays\": 5,
        \"quantityPrescribed\": 10,
        \"instructions\": \"Take after meals\"
      }
    ]
  }")"

PRESCRIPTION_ID="$(require_json_id "Prescription" "$PRESCRIPTION_RESPONSE")"

log_ok "Create prescription → ID $PRESCRIPTION_ID"


# ------------------------------------------------------
# 14. CREATE INVOICE
# ------------------------------------------------------

INVOICE_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/invoices" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"patientId\": $PATIENT_ID,
    \"appointmentId\": $APPOINTMENT_ID,
    \"currencyCode\": \"USD\",
    \"dueDate\": \"2026-08-20\",
    \"lines\": [
      {
        \"lineNumber\": 1,
        \"itemType\": \"CONSULTATION\",
        \"description\": \"Smoke-test medical consultation\",
        \"quantity\": 1,
        \"unitPrice\": 50,
        \"discountAmount\": 0,
        \"taxAmount\": 0
      }
    ]
  }")"

INVOICE_ID="$(require_json_id "Invoice" "$INVOICE_RESPONSE")"

log_ok "Create invoice → ID $INVOICE_ID"


# ------------------------------------------------------
# 15. ISSUE INVOICE
# ------------------------------------------------------

ISSUE_RESPONSE="$(request \
  POST \
  "/invoices/$INVOICE_ID/issue")"

ISSUE_CODE="$(echo "$ISSUE_RESPONSE" | tail -n1)"
ISSUE_BODY="$(echo "$ISSUE_RESPONSE" | sed '$d')"

if [[ "$ISSUE_CODE" == "200" ]]; then
  log_ok "Issue invoice"
else
  log_fail "Issue invoice" "$ISSUE_CODE" "$ISSUE_BODY"
fi


# ------------------------------------------------------
# 16. RECORD PAYMENT
# ------------------------------------------------------

PAYMENT_RESPONSE="$(curl -sS \
  -X POST "$BASE_URL/payments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"invoiceId\": $INVOICE_ID,
    \"amount\": 50,
    \"paymentMethod\": \"CASH\",
    \"paymentReference\": \"SMOKE-$STAMP\"
  }")"

PAYMENT_ID="$(require_json_id "Payment" "$PAYMENT_RESPONSE")"

log_ok "Record payment → ID $PAYMENT_ID"


# ------------------------------------------------------
# 17. VERIFY CREATED RESOURCES
# ------------------------------------------------------

VERIFY_PATIENT="$(request GET "/patients/$PATIENT_ID")"
VERIFY_PATIENT_CODE="$(echo "$VERIFY_PATIENT" | tail -n1)"

if [[ "$VERIFY_PATIENT_CODE" == "200" ]]; then
  log_ok "Verify patient"
else
  log_fail "Verify patient" "$VERIFY_PATIENT_CODE" "$(echo "$VERIFY_PATIENT" | sed '$d')"
fi


VERIFY_DOCTOR="$(request GET "/doctors/$DOCTOR_ID")"
VERIFY_DOCTOR_CODE="$(echo "$VERIFY_DOCTOR" | tail -n1)"

if [[ "$VERIFY_DOCTOR_CODE" == "200" ]]; then
  log_ok "Verify doctor"
else
  log_fail "Verify doctor" "$VERIFY_DOCTOR_CODE" "$(echo "$VERIFY_DOCTOR" | sed '$d')"
fi


VERIFY_APPOINTMENT="$(request GET "/appointments/$APPOINTMENT_ID")"
VERIFY_APPOINTMENT_CODE="$(echo "$VERIFY_APPOINTMENT" | tail -n1)"

if [[ "$VERIFY_APPOINTMENT_CODE" == "200" ]]; then
  log_ok "Verify appointment"
else
  log_fail "Verify appointment" "$VERIFY_APPOINTMENT_CODE" "$(echo "$VERIFY_APPOINTMENT" | sed '$d')"
fi


VERIFY_RECORD="$(request GET "/medical-records/$MEDICAL_RECORD_ID")"
VERIFY_RECORD_CODE="$(echo "$VERIFY_RECORD" | tail -n1)"

if [[ "$VERIFY_RECORD_CODE" == "200" ]]; then
  log_ok "Verify medical record"
else
  log_fail "Verify medical record" "$VERIFY_RECORD_CODE" "$(echo "$VERIFY_RECORD" | sed '$d')"
fi


VERIFY_INVOICE="$(request GET "/invoices/$INVOICE_ID")"
VERIFY_INVOICE_CODE="$(echo "$VERIFY_INVOICE" | tail -n1)"

if [[ "$VERIFY_INVOICE_CODE" == "200" ]]; then
  log_ok "Verify invoice"
else
  log_fail "Verify invoice" "$VERIFY_INVOICE_CODE" "$(echo "$VERIFY_INVOICE" | sed '$d')"
fi


# ------------------------------------------------------
# SUMMARY
# ------------------------------------------------------

echo
echo "======================================================"
echo "                  TEST SUMMARY"
echo "======================================================"
echo "Passed: $PASS"
echo "Failed: $FAIL"
echo

echo "Created resources:"
echo "  User account : $DOCTOR_USER_ID"
echo "  Patient      : $PATIENT_ID"
echo "  Doctor       : $DOCTOR_ID"
echo "  Availability : $AVAILABILITY_ID"
echo "  Appointment  : $APPOINTMENT_ID"
echo "  Record       : $MEDICAL_RECORD_ID"
echo "  Vital signs  : $VITAL_ID"
echo "  Diagnosis    : $DIAGNOSIS_ID"
echo "  Lab request  : $LAB_ID"
echo "  Lab result   : ${RESULT_ID:-N/A}"
echo "  Prescription : $PRESCRIPTION_ID"
echo "  Invoice      : $INVOICE_ID"
echo "  Payment      : $PAYMENT_ID"

echo

if [[ "$FAIL" -eq 0 ]]; then
  echo "🎉 MEDCORE E2E SMOKE TEST PASSED"
  exit 0
else
  echo "❌ MEDCORE E2E SMOKE TEST FAILED"
  exit 1
fi
