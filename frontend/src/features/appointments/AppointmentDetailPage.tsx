import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, RefreshCw } from "lucide-react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  cancelAppointment,
  checkInAppointment,
  completeAppointment,
  getAppointment,
  startAppointment,
} from "./api";
import type { AppointmentStatus } from "./types";

function formatDate(value: string | null) {
  return value ? new Date(value).toLocaleString() : "—";
}

export function AppointmentDetailPage() {
  const { appointmentId } = useParams();
  const numericId = Number(appointmentId);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const appointmentQuery = useQuery({
    queryKey: ["appointment", numericId],
    queryFn: () => getAppointment(numericId),
    enabled: Number.isInteger(numericId) && numericId > 0,
  });

  const appointment = appointmentQuery.data?.data;

  const transitionMutation = useMutation({
    mutationFn: async (
      action: "check-in" | "start" | "complete",
    ) => {
      if (action === "check-in") {
        return checkInAppointment(numericId);
      }
      if (action === "start") {
        return startAppointment(numericId);
      }
      return completeAppointment(numericId);
    },
    onSuccess: async (response) => {
      queryClient.setQueryData(
        ["appointment", numericId],
        response,
      );
      await queryClient.invalidateQueries({
        queryKey: ["appointments"],
      });
    },
  });

  const cancelMutation = useMutation({
    mutationFn: () => cancelAppointment(numericId, "Cancelled from appointment details"),
    onSuccess: async (response) => {
      queryClient.setQueryData(
        ["appointment", numericId],
        response,
      );
      await queryClient.invalidateQueries({
        queryKey: ["appointments"],
      });
    },
  });

  if (!Number.isInteger(numericId) || numericId <= 0) {
    return (
      <section>
        <div className="alert" role="alert">Invalid appointment ID.</div>
        <Link className="app-button" to="/appointments">Back to appointments</Link>
      </section>
    );
  }

  if (appointmentQuery.isLoading) {
    return <section><div className="card empty-state">Loading appointment...</div></section>;
  }

  if (appointmentQuery.isError || !appointment) {
    return (
      <section>
        <div className="page-header">
          <div>
            <h2>Appointment not found</h2>
            <p>Unable to load this appointment.</p>
          </div>
          <button className="app-button" type="button" onClick={() => appointmentQuery.refetch()}>
            <RefreshCw size={16} /> Retry
          </button>
        </div>
        <Link className="app-button" to="/appointments">
          <ArrowLeft size={16} /> Back to appointments
        </Link>
      </section>
    );
  }

  const busy = transitionMutation.isPending || cancelMutation.isPending;
  const status = appointment.status as AppointmentStatus;

  return (
    <section>
      <div className="page-header">
        <div>
          <Link className="back-button" to="/appointments">
            <ArrowLeft size={16} /> Appointments
          </Link>
          <h2>Appointment #{appointment.id}</h2>
          <p>{appointment.reason}</p>
        </div>
        <span className="app-user-pill">{status}</span>
      </div>

      {(transitionMutation.isError || cancelMutation.isError) && (
        <div className="alert" role="alert">
          This appointment transition could not be completed. Check the current status and try again.
        </div>
      )}

      <div className="detail-grid">
        <article className="card">
          <h3>Schedule</h3>
          <dl className="details-list">
            <DetailRow label="Starts" value={formatDate(appointment.scheduledStartAt)} />
            <DetailRow label="Ends" value={formatDate(appointment.scheduledEndAt)} />
            <DetailRow label="Reason" value={appointment.reason} />
            <DetailRow label="Scheduling notes" value={appointment.schedulingNotes ?? "—"} />
          </dl>
        </article>

        <article className="card">
          <h3>Participants</h3>
          <dl className="details-list">
            <DetailRow label="Patient ID" value={String(appointment.patientId)} />
            <DetailRow label="Doctor ID" value={String(appointment.doctorId)} />
            <DetailRow label="Department ID" value={String(appointment.departmentId)} />
            <DetailRow label="Queue position" value={String(appointment.queuePosition ?? "—")} />
          </dl>
        </article>

        <article className="card">
          <h3>Actions</h3>
          <div className="header-actions">
            {status === "SCHEDULED" && (
              <button
                className="app-button"
                type="button"
                disabled={busy}
                onClick={() => transitionMutation.mutate("check-in")}
              >
                Check in
              </button>
            )}

            {status === "CHECKED_IN" && (
              <button
                className="app-button"
                type="button"
                disabled={busy}
                onClick={() => transitionMutation.mutate("start")}
              >
                Start consultation
              </button>
            )}

            {status === "IN_PROGRESS" && (
              <button
                className="app-button"
                type="button"
                disabled={busy}
                onClick={() => transitionMutation.mutate("complete")}
              >
                Complete
              </button>
            )}

            {(status === "IN_PROGRESS" || status === "COMPLETED") && (
              <button
                className="app-button"
                type="button"
                onClick={() =>
                  navigate(
                    `/patients/${appointment.patientId}/clinical` +
                    `?appointmentId=${appointment.id}` +
                    `&doctorId=${appointment.doctorId}`,
                  )
                }
              >
                Open medical record
              </button>
            )}

            {(status === "SCHEDULED" || status === "CHECKED_IN") && (
              <button
                className="app-button app-button-danger"
                type="button"
                disabled={busy}
                onClick={() => {
                  if (window.confirm("Cancel this appointment?")) {
                    cancelMutation.mutate();
                  }
                }}
              >
                Cancel appointment
              </button>
            )}
          </div>
        </article>
      </div>
    </section>
  );
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}
