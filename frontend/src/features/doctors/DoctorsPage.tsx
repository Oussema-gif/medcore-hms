import { useState } from "react";
import {
  ChevronLeft,
  ChevronRight,
  ExternalLink,
  Plus,
  RefreshCw,
  Search,
  Stethoscope,
} from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";

import { getDoctors } from "./api";

const PAGE_SIZE = 10;

function initials(
  firstName: string,
  lastName: string,
) {
  return `${firstName.slice(0, 1)}${lastName
    .slice(0, 1)}`.toUpperCase();
}

export function DoctorsPage() {
  const navigate = useNavigate();

  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");

  const doctorsQuery = useQuery({
    queryKey: ["doctors", page, PAGE_SIZE],
    queryFn: () =>
      getDoctors(page, PAGE_SIZE),
  });

  const response = doctorsQuery.data;
  const doctors = response?.data ?? [];

  const normalizedSearch =
    search.trim().toLowerCase();

  const filteredDoctors =
    normalizedSearch
      ? doctors.filter((doctor) =>
          [
            doctor.firstName,
            doctor.lastName,
            doctor.specialty ?? "",
            doctor.professionalRegistrationNumber,
            doctor.email ?? "",
            doctor.phoneNumber ?? "",
          ]
            .join(" ")
            .toLowerCase()
            .includes(normalizedSearch),
        )
      : doctors;

  const hasPreviousPage = page > 0;

  const hasNextPage = response
    ? page + 1 < response.totalPages
    : false;

  return (
    <section className="doctor-directory">
      <header className="doctor-command-header">
        <div className="doctor-heading">
          <span className="eyebrow">
            Clinical directory
          </span>

          <h2>Doctors</h2>

          <p>
            Manage the clinical team, professional
            profiles, and consultation settings.
          </p>
        </div>

        <div className="header-actions">
          <button
            className="app-button"
            type="button"
            onClick={() =>
              doctorsQuery.refetch()
            }
            disabled={
              doctorsQuery.isFetching
            }
          >
            <RefreshCw
              size={15}
              className={
                doctorsQuery.isFetching
                  ? "spin"
                  : undefined
              }
            />

            {doctorsQuery.isFetching
              ? "Refreshing..."
              : "Refresh"}
          </button>

          <button
            className="app-button primary"
            type="button"
            onClick={() =>
              navigate("/doctors/new")
            }
          >
            <Plus size={15} />
            Add doctor
          </button>
        </div>
      </header>

      <div className="doctor-toolbar">
        <label className="field doctor-search">
          <Search
            size={16}
            aria-hidden="true"
          />

          <input
            id="doctor-search"
            value={search}
            onChange={(event) =>
              setSearch(
                event.target.value,
              )
            }
            placeholder="Search by name, specialty, registration, email or phone..."
            aria-label="Search doctors"
          />
        </label>

        <div className="doctor-directory-count">
          <span>
            {response?.totalElements ?? 0}
          </span>

          doctors
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

      {doctorsQuery.isLoading && (
        <div className="state-card">
          <div className="state-card-icon">
            <Stethoscope size={25} />
          </div>

          <h3>
            Loading the doctor directory
          </h3>

          <p>
            Retrieving the latest clinical
            profiles...
          </p>
        </div>
      )}

      {doctorsQuery.isError && (
        <div
          className="alert"
          role="alert"
        >
          Unable to load doctors.
          Check that the backend is running
          and try again.
        </div>
      )}

      {!doctorsQuery.isLoading &&
        !doctorsQuery.isError &&
        doctors.length === 0 && (
          <div className="state-card">
            <div className="state-card-icon">
              <Stethoscope size={28} />
            </div>

            <h3>
              No doctors yet
            </h3>

            <p>
              Start the clinical directory
              by creating the first doctor
              profile.
            </p>

            <button
              className="app-button primary"
              type="button"
              onClick={() =>
                navigate("/doctors/new")
              }
            >
              <Plus size={15} />
              Add first doctor
            </button>
          </div>
        )}

      {!doctorsQuery.isLoading &&
        !doctorsQuery.isError &&
        doctors.length > 0 &&
        filteredDoctors.length === 0 && (
          <div className="state-card">
            <div className="state-card-icon">
              <Search size={27} />
            </div>

            <h3>
              No matching doctor
            </h3>

            <p>
              Try another name, specialty,
              registration number, email,
              or phone number.
            </p>
          </div>
        )}

      {filteredDoctors.length > 0 && (
        <div
          className="doctor-list"
          aria-label="Doctor list"
        >
          <div
            className="doctor-row doctor-list-head"
            aria-hidden="true"
          >
            <span>Doctor</span>
            <span>Specialty</span>
            <span>Registration</span>
            <span>Contact</span>
            <span>Duration</span>
            <span>Status</span>
            <span>Action</span>
          </div>

          {filteredDoctors.map(
            (doctor) => (
              <article
                key={doctor.id}
                className="doctor-row"
                tabIndex={0}
                role="button"
                onClick={() =>
                  navigate(
                    `/doctors/${doctor.id}`,
                  )
                }
                onKeyDown={(event) => {
                  if (
                    event.key ===
                      "Enter" ||
                    event.key === " "
                  ) {
                    event.preventDefault();

                    navigate(
                      `/doctors/${doctor.id}`,
                    );
                  }
                }}
              >
                <div className="doctor-row-head">
                  <span className="doctor-avatar">
                    {initials(
                      doctor.firstName,
                      doctor.lastName,
                    )}
                  </span>

                  <div className="doctor-row-name">
                    <strong>
                      Dr.{" "}
                      {doctor.firstName}{" "}
                      {doctor.lastName}
                    </strong>

                    <span>
                      Doctor #{doctor.id}
                    </span>
                  </div>
                </div>

                <div className="doctor-row-value">
                  {doctor.specialty ||
                    "General practice"}
                </div>

                <div className="doctor-row-value doctor-registration">
                  {
                    doctor.professionalRegistrationNumber
                  }
                </div>

                <div className="doctor-row-value doctor-contact">
                  {doctor.email ??
                    doctor.phoneNumber ??
                    "No contact"}
                </div>

                <div className="doctor-duration">
                  <strong>
                    {
                      doctor.consultationDurationMinutes
                    }
                  </strong>
                  <span>min</span>
                </div>

                <div
                  className={`doctor-row-status${
                    doctor.active
                      ? ""
                      : " inactive"
                  }`}
                >
                  <i />

                  {doctor.active
                    ? "Active"
                    : "Inactive"}
                </div>

                <button
                  className="app-button doctor-row-action"
                  type="button"
                  onClick={(event) => {
                    event.stopPropagation();

                    navigate(
                      `/doctors/${doctor.id}`,
                    );
                  }}
                >
                  Open
                  <ExternalLink size={13} />
                </button>
              </article>
            ),
          )}
        </div>
      )}

      {response &&
        response.totalPages > 0 && (
          <div className="pagination-bar doctor-pagination">
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
                disabled={
                  !hasPreviousPage ||
                  doctorsQuery.isFetching
                }
                onClick={() =>
                  setPage(
                    (value) =>
                      value - 1,
                  )
                }
              >
                <ChevronLeft size={15} />
                Previous
              </button>

              <button
                className="app-button"
                type="button"
                disabled={
                  !hasNextPage ||
                  doctorsQuery.isFetching
                }
                onClick={() =>
                  setPage(
                    (value) =>
                      value + 1,
                  )
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