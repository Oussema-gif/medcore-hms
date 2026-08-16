import { useState } from "react";
import {
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  Plus,
  RefreshCw,
} from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { getAllAppointments } from "./api";

const PAGE_SIZE = 10;

function formatDate(value: string) {
  return new Date(value).toLocaleString();
}

export function AppointmentsPage() {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState("");

  const appointmentsQuery = useQuery({
    queryKey: ["appointments", "all", page],
    queryFn: () => getAllAppointments(page, PAGE_SIZE),
  });

  const response = appointmentsQuery.data;
  const appointments = response?.data ?? [];
  const searchValue = search.trim().toLowerCase();

  const filteredAppointments = appointments.filter((appointment) => {
    const matchesSearch =
      !searchValue ||
      [
        appointment.reason,
        appointment.status,
        String(appointment.id),
        String(appointment.patientId),
        String(appointment.doctorId),
      ]
        .join(" ")
        .toLowerCase()
        .includes(searchValue);

    const matchesStatus = !status || appointment.status === status;
    return matchesSearch && matchesStatus;
  });

  const totalPages = response?.totalPages ?? 0;
  const hasPreviousPage = page > 0;
  const hasNextPage = page + 1 < totalPages;

  return (
    <section>
      <div className="page-header">
        <div>
          <h2>Appointments</h2>
          <p>Schedule and manage patient consultations.</p>
        </div>
        <div className="header-actions">
          <button
            className="app-button"
            type="button"
            onClick={() => appointmentsQuery.refetch()}
            disabled={appointmentsQuery.isFetching}
          >
            <RefreshCw size={16} /> Refresh
          </button>
          <button
            className="app-button"
            type="button"
            onClick={() => navigate("/appointments/new")}
          >
            <Plus size={16} /> Schedule appointment
          </button>
        </div>
      </div>

      <div className="card filter-card">
        <div className="card-grid">
          <label className="field">
            <span>Search current page</span>
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Reason, status, appointment ID, patient ID, doctor ID"
            />
          </label>
          <label className="field">
            <span>Status</span>
            <select
              value={status}
              onChange={(event) => setStatus(event.target.value)}
            >
              <option value="">All statuses</option>
              <option value="SCHEDULED">Scheduled</option>
              <option value="CHECKED_IN">Checked in</option>
              <option value="IN_PROGRESS">In progress</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
              <option value="NO_SHOW">No-show</option>
            </select>
          </label>
        </div>
      </div>

      {appointmentsQuery.isLoading && (
        <div className="card empty-state">Loading appointments...</div>
      )}

      {appointmentsQuery.isError && (
        <div className="alert" role="alert">Unable to load appointments.</div>
      )}

      {!appointmentsQuery.isLoading && !appointmentsQuery.isError && appointments.length === 0 && (
        <div className="card empty-state">
          <CalendarDays size={34} />
          <p>No appointments have been scheduled.</p>
        </div>
      )}

      {!appointmentsQuery.isLoading && !appointmentsQuery.isError && appointments.length > 0 && filteredAppointments.length === 0 && (
        <div className="card empty-state">No appointments match the current filters.</div>
      )}

      {filteredAppointments.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Date and time</th>
                <th>Patient ID</th>
                <th>Doctor ID</th>
                <th>Reason</th>
                <th>Status</th>
                <th>Queue</th>
              </tr>
            </thead>
            <tbody>
              {filteredAppointments.map((appointment) => (
                <tr
                  key={appointment.id}
                  className="clickable-row"
                  onClick={() => navigate(`/appointments/${appointment.id}`)}
                >
                  <td>
                    {formatDate(appointment.scheduledStartAt)}
                    <br />
                    <span className="helper-text">{formatDate(appointment.scheduledEndAt)}</span>
                  </td>
                  <td>{appointment.patientId}</td>
                  <td>{appointment.doctorId}</td>
                  <td>{appointment.reason}</td>
                  <td><span className="app-user-pill">{appointment.status}</span></td>
                  <td>{appointment.queuePosition ?? "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {response && (
        <div className="pagination-bar">
          <span className="helper-text">
            Page {response.page + 1} of {response.totalPages || 1} · {response.totalElements} total
          </span>
          <div className="pagination-controls">
            <button
              className="app-button"
              type="button"
              disabled={!hasPreviousPage || appointmentsQuery.isFetching}
              onClick={() => setPage((current) => current - 1)}
            >
              <ChevronLeft size={16} /> Previous
            </button>
            <button
              className="app-button"
              type="button"
              disabled={!hasNextPage || appointmentsQuery.isFetching}
              onClick={() => setPage((current) => current + 1)}
            >
              Next <ChevronRight size={16} />
            </button>
          </div>
        </div>
      )}
    </section>
  );
}
