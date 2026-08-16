import { useMemo, useState } from "react";
import { Search, X } from "lucide-react";
import { useQuery } from "@tanstack/react-query";

import { InvoicesPanel } from "./InvoicesPanel";
import { getPatients } from "../patients/api";
import type { Patient } from "../patients/types";

export function InvoicesPage() {
  const [search, setSearch] = useState("");
  const [selectedPatient, setSelectedPatient] =
    useState<Patient | null>(null);

  const patientsQuery = useQuery({
    queryKey: ["invoice-patient-lookup"],
    queryFn: () => getPatients(0, 100),
  });

  const patients = useMemo(
    () => patientsQuery.data?.data ?? [],
    [patientsQuery.data?.data],
  );

  const filteredPatients = useMemo(() => {
    const normalized = search.trim().toLowerCase();

    if (!normalized) {
      return [];
    }

    return patients.filter((patient) =>
      [
        patient.firstName,
        patient.lastName,
        patient.medicalRecordNumber,
        patient.email ?? "",
        patient.phoneNumber ?? "",
      ]
        .join(" ")
        .toLowerCase()
        .includes(normalized),
    );
  }, [patients, search]);

  function selectPatient(patient: Patient) {
    setSelectedPatient(patient);
    setSearch("");
  }

  function clearPatient() {
    setSelectedPatient(null);
    setSearch("");
  }

  return (
    <section>
      <div className="page-header">
        <div>
          <h2>Invoices and payments</h2>
          <p>
            Review patient invoices, issue charges, and record
            payments.
          </p>
        </div>
      </div>

      <div className="lookup-card">
        <div className="lookup-heading">
          <span>Patient</span>
          <small>
            Select the patient whose billing history you want to
            review.
          </small>
        </div>

        {selectedPatient ? (
          <div className="lookup-selected">
            <div className="lookup-avatar">
              {selectedPatient.firstName
                .slice(0, 1)
                .toUpperCase()}
            </div>

            <div className="lookup-selected-copy">
              <strong>
                {selectedPatient.firstName}{" "}
                {selectedPatient.lastName}
              </strong>

              <span>
                {selectedPatient.medicalRecordNumber}
                {selectedPatient.email
                  ? ` · ${selectedPatient.email}`
                  : ""}
              </span>
            </div>

            <button
              className="lookup-clear"
              type="button"
              onClick={clearPatient}
              aria-label="Clear selected patient"
            >
              <X size={16} />
            </button>
          </div>
        ) : (
          <div className="lookup-search">
            <Search size={17} />

            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search patient by name, MRN, email, or phone..."
              autoComplete="off"
            />
          </div>
        )}

        {!selectedPatient &&
          search.trim() &&
          filteredPatients.length > 0 && (
            <div className="lookup-results">
              {filteredPatients.map((patient) => (
                <button
                  type="button"
                  key={patient.id}
                  className="lookup-result"
                  onClick={() => selectPatient(patient)}
                >
                  <div className="lookup-avatar">
                    {patient.firstName
                      .slice(0, 1)
                      .toUpperCase()}
                  </div>

                  <div className="lookup-result-copy">
                    <strong>
                      {patient.firstName}{" "}
                      {patient.lastName}
                    </strong>

                    <span>
                      {patient.medicalRecordNumber}
                      {patient.email
                        ? ` · ${patient.email}`
                        : ""}
                    </span>
                  </div>
                </button>
              ))}
            </div>
          )}

        {!selectedPatient &&
          search.trim() &&
          !patientsQuery.isLoading &&
          filteredPatients.length === 0 && (
            <div className="lookup-empty">
              No patient matches "{search}".
            </div>
          )}
      </div>

      {!selectedPatient && (
        <div className="card empty-state">
          Select a patient to view invoices and payments.
        </div>
      )}

      {selectedPatient && (
        <InvoicesPanel patientId={selectedPatient.id} />
      )}
    </section>
  );
}