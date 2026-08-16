import { useState, type ReactNode } from "react";
import {
  Link,
  useParams,
  useSearchParams,
} from "react-router-dom";
import {
  ClipboardList,
  FlaskConical,
  HeartPulse,
  Pill,
  Receipt,
  ShieldAlert,
  Stethoscope,
} from "lucide-react";
import { useQuery } from "@tanstack/react-query";

import { getPatient } from "./api";
import type { Patient } from "./types";

import { getMedicalRecordsByPatientId } from "../medical-records/api";
import { MedicalRecordsPanel } from "../medical-records/MedicalRecordsPanel";
import { VitalsPanel } from "../vitals/VitalsPanel";
import { DiagnosisPanel } from "../diagnoses/DiagnosisPanel";
import { LaboratoryRequestPanel } from "../laboratory/LaboratoryRequestPanel";
import { PrescriptionsPanel } from "../prescriptions/PrescriptionsPanel";
import { InvoicesPanel } from "../invoices/InvoicesPanel";

type ClinicalTab =
  | "overview"
  | "records"
  | "vitals"
  | "laboratory"
  | "prescriptions"
  | "billing";

function initials(patient: Patient) {
  return `${patient.firstName[0] ?? ""}${patient.lastName[0] ?? ""}`.toUpperCase();
}

function humanize(value: string) {
  return value
    .toLowerCase()
    .replace(/_/g, " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function formatDate(value: string | null | undefined) {
  if (!value) {
    return "—";
  }

  return new Date(value).toLocaleString();
}

export function PatientClinicalPage() {
  const { patientId } = useParams<{ patientId: string }>();
  const [searchParams] = useSearchParams();

  const [activeTab, setActiveTab] =
    useState<ClinicalTab>("overview");

  const [
    selectedMedicalRecordId,
    setSelectedMedicalRecordId,
  ] = useState<number | null>(null);

  const numericPatientId = Number(patientId);

  const appointmentIdValue = Number(
    searchParams.get("appointmentId"),
  );

  const doctorIdValue = Number(
    searchParams.get("doctorId"),
  );

  const appointmentId =
    Number.isInteger(appointmentIdValue) &&
    appointmentIdValue > 0
      ? appointmentIdValue
      : undefined;

  const queryDoctorId =
    Number.isInteger(doctorIdValue) &&
    doctorIdValue > 0
      ? doctorIdValue
      : undefined;

  const patientQuery = useQuery({
    queryKey: ["patient", numericPatientId],
    queryFn: () => getPatient(numericPatientId),
    enabled:
      Number.isInteger(numericPatientId) &&
      numericPatientId > 0,
  });

  const patient = patientQuery.data?.data;

  /*
   * Billing does not depend on a medical record.
   * Keep the clinical-record query focused on tabs
   * that actually require medical-record context.
   */
  const requiresMedicalRecord =
    activeTab === "records" ||
    activeTab === "vitals" ||
    activeTab === "laboratory" ||
    activeTab === "prescriptions";

  const medicalRecordsQuery = useQuery({
    queryKey: ["medical-records", patient?.id],
    queryFn: () =>
      getMedicalRecordsByPatientId(patient!.id),
    enabled:
      Boolean(patient?.id) &&
      requiresMedicalRecord,
  });

  const records =
    medicalRecordsQuery.data?.data ?? [];

  const medicalRecord =
    records.find(
      (record) =>
        record.id === selectedMedicalRecordId,
    ) ?? records[0];

  const effectiveMedicalRecordId =
    medicalRecord?.id;

  const orderingDoctorId =
    queryDoctorId ?? medicalRecord?.doctorId;

  if (patientQuery.isLoading) {
    return (
      <section className="patient-clinical-page">
        <div className="state-card">
          <div className="state-card-icon">
            <HeartPulse size={25} />
          </div>

          <h3>Loading patient workspace</h3>

          <p>
            Preparing the clinical record and
            patient context...
          </p>
        </div>
      </section>
    );
  }

  if (patientQuery.isError || !patient) {
    return (
      <section className="patient-clinical-page">
        <div className="alert" role="alert">
          Unable to load this patient workspace.
        </div>
      </section>
    );
  }

  return (
    <section className="patient-clinical-page">
      <header className="clinical-hero">
        <div className="clinical-identity">
          <Link
            className="back-button"
            to={`/patients/${patient.id}`}
          >
            <span aria-hidden="true">←</span>
            Patient details
          </Link>

          <div className="identity-top">
            <div
              className="identity-avatar"
              aria-hidden="true"
            >
              {initials(patient)}
            </div>

            <div className="identity-copy">
              <span className="eyebrow">
                Patient command center
              </span>

              <h2>
                {patient.firstName}{" "}
                {patient.lastName}
              </h2>

              <p>
                {patient.medicalRecordNumber}
                {" · "}
                {humanize(patient.sex)}
                {" · born "}
                {patient.dateOfBirth}
              </p>

              <div className="identity-tags">
                <span className="identity-tag">
                  {patient.active
                    ? "Active patient"
                    : "Inactive patient"}
                </span>

                <span className="identity-tag">
                  {patient.bloodGroup ??
                    "Blood group unknown"}
                </span>

                {patient.email && (
                  <span className="identity-tag">
                    {patient.email}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

        <aside className="clinical-safety">
          <div className="safety-title">
            <ShieldAlert size={14} />
            Safety context
          </div>

          <p>
            <strong>Allergies:</strong>{" "}
            {patient.allergies ||
              "No allergies recorded"}
          </p>

          <p>
            <strong>Emergency:</strong>{" "}
            {patient.emergencyContactName ||
              "No contact recorded"}
            {patient.emergencyContactPhone
              ? ` · ${patient.emergencyContactPhone}`
              : ""}
          </p>
        </aside>
      </header>

      <div className="clinical-tabs-wrap">
        <nav
          className="clinical-tabs"
          aria-label="Clinical sections"
          role="tablist"
        >
          <TabButton
            active={activeTab === "overview"}
            onClick={() =>
              setActiveTab("overview")
            }
            icon={<ClipboardList size={15} />}
          >
            Overview
          </TabButton>

          <TabButton
            active={activeTab === "records"}
            onClick={() =>
              setActiveTab("records")
            }
            icon={<ClipboardList size={15} />}
          >
            Records
          </TabButton>

          <TabButton
            active={activeTab === "vitals"}
            onClick={() =>
              setActiveTab("vitals")
            }
            icon={<HeartPulse size={15} />}
          >
            Vitals
          </TabButton>

          <TabButton
            active={activeTab === "laboratory"}
            onClick={() =>
              setActiveTab("laboratory")
            }
            icon={<FlaskConical size={15} />}
          >
            Laboratory
          </TabButton>

          <TabButton
            active={
              activeTab === "prescriptions"
            }
            onClick={() =>
              setActiveTab("prescriptions")
            }
            icon={<Pill size={15} />}
          >
            Medication
          </TabButton>

          <TabButton
            active={activeTab === "billing"}
            onClick={() =>
              setActiveTab("billing")
            }
            icon={<Receipt size={15} />}
          >
            Billing
          </TabButton>
        </nav>
      </div>

      {requiresMedicalRecord &&
        records.length > 0 &&
        medicalRecord && (
          <div className="clinical-record-context">
            <div className="clinical-record-context-copy">
              <span>
                Clinical record context
              </span>

              <strong>
                Record #{effectiveMedicalRecordId}
              </strong>

              <small>
                {formatDate(
                  medicalRecord.encounterAt,
                )}
              </small>
            </div>

            <label className="clinical-record-select">
              <span>Select record</span>

              <select
                value={
                  effectiveMedicalRecordId ?? ""
                }
                onChange={(event) => {
                  const value = Number(
                    event.target.value,
                  );

                  setSelectedMedicalRecordId(
                    value > 0 ? value : null,
                  );
                }}
                aria-label="Select medical record"
              >
                {records.map((record) => (
                  <option
                    key={record.id}
                    value={record.id}
                  >
                    Record #{record.id} ·{" "}
                    {new Date(
                      record.encounterAt,
                    ).toLocaleString()}
                  </option>
                ))}
              </select>
            </label>
          </div>
        )}

      <div className="clinical-content">
        {activeTab === "overview" && (
          <OverviewPanel
            patient={patient}
            records={records}
          />
        )}

        {activeTab === "records" && (
          <div className="clinical-view clinical-view-records">
            <MedicalRecordsPanel
              patientId={patient.id}
              appointmentId={appointmentId}
              doctorId={queryDoctorId}
              onRecordReady={(
                medicalRecordId,
              ) => {
                setSelectedMedicalRecordId(
                  medicalRecordId,
                );
                setActiveTab("vitals");
              }}
            />
          </div>
        )}

        {activeTab === "vitals" && (
          <div className="clinical-view clinical-view-vitals">
            <ClinicalRecordState
              isLoading={
                medicalRecordsQuery.isLoading
              }
              isError={
                medicalRecordsQuery.isError
              }
              hasRecords={
                records.length > 0
              }
              emptyMessage="Create a medical record before recording vitals or diagnoses."
            />

            {effectiveMedicalRecordId !==
              undefined && (
              <div className="clinical-view-stack">
                <VitalsPanel
                  medicalRecordId={
                    effectiveMedicalRecordId
                  }
                />

                <DiagnosisPanel
                  medicalRecordId={
                    effectiveMedicalRecordId
                  }
                />
              </div>
            )}
          </div>
        )}

        {activeTab === "laboratory" && (
          <div className="clinical-view clinical-view-laboratory">
            <ClinicalRecordState
              isLoading={
                medicalRecordsQuery.isLoading
              }
              isError={
                medicalRecordsQuery.isError
              }
              hasRecords={
                records.length > 0
              }
              emptyMessage="Create a medical record before managing laboratory requests."
            />

            {effectiveMedicalRecordId !==
              undefined && (
              <LaboratoryRequestPanel
                patientId={patient.id}
                doctorId={orderingDoctorId}
                appointmentId={appointmentId}
                medicalRecordId={
                  effectiveMedicalRecordId
                }
              />
            )}
          </div>
        )}

        {activeTab ===
          "prescriptions" && (
          <div className="clinical-view clinical-view-prescriptions">
            <ClinicalRecordState
              isLoading={
                medicalRecordsQuery.isLoading
              }
              isError={
                medicalRecordsQuery.isError
              }
              hasRecords={
                records.length > 0
              }
              emptyMessage="Create a medical record before managing prescriptions."
            />

            {effectiveMedicalRecordId !==
              undefined && (
              <PrescriptionsPanel
                patientId={patient.id}
                doctorId={orderingDoctorId}
                appointmentId={appointmentId}
                medicalRecordId={
                  effectiveMedicalRecordId
                }
              />
            )}
          </div>
        )}

        {activeTab === "billing" && (
          <div className="clinical-view clinical-view-billing">
            <InvoicesPanel
              patientId={patient.id}
              appointmentId={appointmentId}
            />
          </div>
        )}
      </div>
    </section>
  );
}

function TabButton({
  active,
  onClick,
  icon,
  children,
}: {
  active: boolean;
  onClick: () => void;
  icon: ReactNode;
  children: ReactNode;
}) {
  return (
    <button
      className={`clinical-tab ${
        active ? "is-active" : ""
      }`}
      type="button"
      onClick={onClick}
      role="tab"
      aria-selected={active}
    >
      {icon}
      {children}
    </button>
  );
}

function ClinicalRecordState({
  isLoading,
  isError,
  hasRecords,
  emptyMessage,
}: {
  isLoading: boolean;
  isError: boolean;
  hasRecords: boolean;
  emptyMessage: string;
}) {
  if (isLoading) {
    return (
      <div className="state-card clinical-state-card">
        <div className="state-card-icon">
          <HeartPulse size={22} />
        </div>

        <h3>Loading clinical records</h3>

        <p>
          Preparing the patient's clinical
          context...
        </p>
      </div>
    );
  }

  if (isError) {
    return (
      <div
        className="alert"
        role="alert"
      >
        Unable to load the patient medical
        records.
      </div>
    );
  }

  if (!hasRecords) {
    return (
      <div className="clinical-record-required">
        <Stethoscope size={18} />

        <div>
          <strong>
            Medical record required
          </strong>

          <span>
            {emptyMessage}
          </span>
        </div>
      </div>
    );
  }

  return null;
}

function OverviewPanel({
  patient,
  records,
}: {
  patient: Patient;
  records: Array<{
    id: number;
    encounterAt: string;
    clinicalNotes: string;
    patientVisible: boolean;
  }>;
}) {
  return (
    <div className="clinical-overview-grid">
      <article className="clinical-info-card">
        <div className="clinical-info-card-header">
          <div>
            <span>Patient identity</span>
            <h3>Identity</h3>
          </div>
        </div>

        <dl className="details-list">
          <div>
            <dt>Date of birth</dt>
            <dd>{patient.dateOfBirth}</dd>
          </div>

          <div>
            <dt>Sex</dt>
            <dd>{humanize(patient.sex)}</dd>
          </div>

          <div>
            <dt>Blood group</dt>
            <dd>
              {patient.bloodGroup || "—"}
            </dd>
          </div>

          <div>
            <dt>Country</dt>
            <dd>
              {patient.country || "—"}
            </dd>
          </div>
        </dl>
      </article>

      <article className="clinical-info-card clinical-info-card-safety">
        <div className="clinical-info-card-header">
          <div>
            <span>Patient safety</span>
            <h3>Safety</h3>
          </div>

          <ShieldAlert size={17} />
        </div>

        <dl className="details-list">
          <div>
            <dt>Allergies</dt>
            <dd>
              {patient.allergies ||
                "No allergies recorded"}
            </dd>
          </div>

          <div>
            <dt>Emergency contact</dt>
            <dd>
              {patient.emergencyContactName ||
                "—"}
            </dd>
          </div>

          <div>
            <dt>Emergency phone</dt>
            <dd>
              {patient.emergencyContactPhone ||
                "—"}
            </dd>
          </div>
        </dl>
      </article>

      <article className="clinical-info-card">
        <div className="clinical-info-card-header">
          <div>
            <span>Encounter history</span>
            <h3>Clinical activity</h3>
          </div>

          <span className="clinical-count">
            {records.length}
          </span>
        </div>

        <dl className="details-list">
          <div>
            <dt>Medical records</dt>
            <dd>{records.length}</dd>
          </div>

          <div>
            <dt>Latest encounter</dt>
            <dd>
              {records[0]
                ? formatDate(
                    records[0].encounterAt,
                  )
                : "None yet"}
            </dd>
          </div>

          <div>
            <dt>Patient visibility</dt>
            <dd>
              {records[0]
                ? records[0].patientVisible
                  ? "Released"
                  : "Clinical only"
                : "—"}
            </dd>
          </div>
        </dl>
      </article>

      <article className="clinical-info-card">
        <div className="clinical-info-card-header">
          <div>
            <span>Communication</span>
            <h3>Contact</h3>
          </div>
        </div>

        <dl className="details-list">
          <div>
            <dt>Email</dt>
            <dd>
              {patient.email || "—"}
            </dd>
          </div>

          <div>
            <dt>Phone</dt>
            <dd>
              {patient.phoneNumber || "—"}
            </dd>
          </div>

          <div>
            <dt>City</dt>
            <dd>
              {patient.city || "—"}
            </dd>
          </div>
        </dl>
      </article>
    </div>
  );
}