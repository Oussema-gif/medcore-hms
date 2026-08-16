import { useState } from "react";
import {
  ChevronLeft,
  ChevronRight,
  ExternalLink,
  Plus,
  RefreshCw,
  Search,
  UserRound,
} from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";

import { getPatients } from "./api";

const PAGE_SIZE = 10;

function initials(
  firstName: string,
  lastName: string,
) {
  return `${firstName.slice(0, 1)}${lastName
    .slice(0, 1)}`.toUpperCase();
}

function formatSex(value: string) {
  return value
    .toLowerCase()
    .replace(/_/g, " ")
    .replace(/\b\w/g, (letter) =>
      letter.toUpperCase(),
    );
}

export function PatientsPage() {
  const navigate = useNavigate();

  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");

  const patientsQuery = useQuery({
    queryKey: [
      "patients",
      page,
      PAGE_SIZE,
    ],
    queryFn: () =>
      getPatients(page, PAGE_SIZE),
  });

  const response = patientsQuery.data;
  const patients = response?.data ?? [];

  const normalizedSearch =
    search.trim().toLowerCase();

  const filteredPatients = normalizedSearch
    ? patients.filter((patient) =>
        [
          patient.firstName,
          patient.lastName,
          patient.medicalRecordNumber,
          patient.email ?? "",
          patient.phoneNumber ?? "",
        ]
          .join(" ")
          .toLowerCase()
          .includes(normalizedSearch),
      )
    : patients;

  const hasPreviousPage = page > 0;

  const hasNextPage = response
    ? page + 1 < response.totalPages
    : false;

  return (
    <section className="patient-directory">
      <header className="patient-command-header">
        <div className="patient-heading">
          <span className="eyebrow">
            Clinical directory
          </span>

          <h2>Patients</h2>

          <p>
            A live care registry for identity,
            clinical context, and the next action.
          </p>
        </div>

        <div className="header-actions">
          <button
            className="app-button"
            type="button"
            onClick={() =>
              patientsQuery.refetch()
            }
            disabled={
              patientsQuery.isFetching
            }
          >
            <RefreshCw
              size={15}
              className={
                patientsQuery.isFetching
                  ? "spin"
                  : undefined
              }
            />

            {patientsQuery.isFetching
              ? "Refreshing..."
              : "Refresh"}
          </button>

          <button
            className="app-button primary"
            type="button"
            onClick={() =>
              navigate("/patients/new")
            }
          >
            <Plus size={15} />
            Register patient
          </button>
        </div>
      </header>

      <div className="patient-toolbar">
        <label className="field patient-search">
          <Search
            size={16}
            aria-hidden="true"
          />

          <input
            value={search}
            onChange={(event) =>
              setSearch(event.target.value)
            }
            placeholder="Find by name, MRN, email or phone"
            aria-label="Search patients"
          />
        </label>

        <div className="patient-directory-count">
          <span>
            {response?.totalElements ?? 0}
          </span>

          patients
        </div>

        <button
          className="app-button"
          type="button"
          onClick={() =>
            setSearch("")
          }
          disabled={!search}
        >
          Clear
        </button>
      </div>

      {patientsQuery.isLoading && (
        <div className="state-card">
          <div className="state-card-icon">
            <UserRound size={25} />
          </div>

          <h3>Loading the patient registry</h3>

          <p>
            Retrieving the latest patient
            records...
          </p>
        </div>
      )}

      {patientsQuery.isError && (
        <div
          className="alert"
          role="alert"
        >
          Unable to load patients. Check that
          the backend is running and try again.
        </div>
      )}

      {!patientsQuery.isLoading &&
        !patientsQuery.isError &&
        patients.length === 0 && (
          <div className="state-card">
            <div className="state-card-icon">
              <UserRound size={28} />
            </div>

            <h3>No patients yet</h3>

            <p>
              Start the registry by creating
              the first patient record.
            </p>

            <button
              className="app-button primary"
              type="button"
              onClick={() =>
                navigate("/patients/new")
              }
            >
              <Plus size={15} />
              Register first patient
            </button>
          </div>
        )}

      {!patientsQuery.isLoading &&
        !patientsQuery.isError &&
        patients.length > 0 &&
        filteredPatients.length === 0 && (
          <div className="state-card">
            <div className="state-card-icon">
              <Search size={27} />
            </div>

            <h3>No matching patient</h3>

            <p>
              Try another name, medical record
              number, email, or phone number.
            </p>
          </div>
        )}

      {filteredPatients.length > 0 && (
        <div
          className="patient-list"
          aria-label="Patient list"
        >
          <div
            className="patient-row patient-list-head"
            aria-hidden="true"
          >
            <span>Patient</span>
            <span>MRN</span>
            <span>Birth</span>
            <span>Sex</span>
            <span>Contact</span>
            <span>Status</span>
            <span>Action</span>
          </div>

          {filteredPatients.map(
            (patient) => (
              <article
                key={patient.id}
                className="patient-row"
                tabIndex={0}
                role="button"
                onClick={() =>
                  navigate(
                    `/patients/${patient.id}`,
                  )
                }
                onKeyDown={(event) => {
                  if (
                    event.key === "Enter" ||
                    event.key === " "
                  ) {
                    event.preventDefault();

                    navigate(
                      `/patients/${patient.id}`,
                    );
                  }
                }}
              >
                <div className="patient-row-head">
                  <span className="patient-avatar">
                    {initials(
                      patient.firstName,
                      patient.lastName,
                    )}
                  </span>

                  <div className="patient-row-name">
                    <strong>
                      {patient.firstName}{" "}
                      {patient.lastName}
                    </strong>

                    <span>
                      Patient #{patient.id}
                    </span>
                  </div>
                </div>

                <div className="patient-row-value">
                  {patient.medicalRecordNumber}
                </div>

                <div className="patient-row-value">
                  {patient.dateOfBirth}
                </div>

                <div className="patient-row-value">
                  {formatSex(patient.sex)}
                </div>

                <div className="patient-row-value patient-contact">
                  {patient.email ??
                    patient.phoneNumber ??
                    "No contact"}
                </div>

                <div
                  className={`patient-row-status${
                    patient.active
                      ? ""
                      : " inactive"
                  }`}
                >
                  <i />

                  {patient.active
                    ? "Active"
                    : "Inactive"}
                </div>

                <button
                  className="app-button patient-row-action"
                  type="button"
                  onClick={(event) => {
                    event.stopPropagation();

                    navigate(
                      `/patients/${patient.id}/clinical`,
                    );
                  }}
                >
                  Clinical

                  <ExternalLink size={13} />
                </button>
              </article>
            ),
          )}
        </div>
      )}

      {response &&
        response.totalPages > 0 && (
          <div className="pagination-bar patient-pagination">
            <span className="helper-text">
              Page {response.page + 1} of{" "}
              {response.totalPages}
              {" · "}
              {response.totalElements} total
            </span>

            <div className="pagination-controls">
              <button
                className="app-button"
                type="button"
                onClick={() =>
                  setPage(
                    (current) =>
                      current - 1,
                  )
                }
                disabled={
                  !hasPreviousPage ||
                  patientsQuery.isFetching
                }
              >
                <ChevronLeft size={15} />
                Previous
              </button>

              <button
                className="app-button"
                type="button"
                onClick={() =>
                  setPage(
                    (current) =>
                      current + 1,
                  )
                }
                disabled={
                  !hasNextPage ||
                  patientsQuery.isFetching
                }
              >
                Next
                <ChevronRight size={15} />
              </button>
            </div>
          </div>
        )}
    </section>
  );
}