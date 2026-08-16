import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, RefreshCw } from "lucide-react";
import { deletePatient, getPatient } from "./api";

function display(value: string | null | undefined) {
  return value?.trim() || "—";
}

export function PatientDetailPage() {
  const navigate = useNavigate();
  const { patientId } = useParams<{ patientId: string }>();

  const queryClient = useQueryClient();
  const deleteMutation = useMutation({
    mutationFn: () => deletePatient(Number(patientId)),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["patients"],
      });
      navigate("/patients", { replace: true });
    },
  });

  const patientQuery = useQuery({
    queryKey: ["patient", patientId],
    queryFn: () => getPatient(Number(patientId)),
    enabled: Boolean(patientId && Number.isFinite(Number(patientId))),
  });

  const patient = patientQuery.data?.data;

  if (patientQuery.isLoading) {
    return (
      <section className="patient-detail-page">
        <div className="card empty-state">
          Loading patient...
        </div>
      </section>
    );
  }

  if (patientQuery.isError || !patient) {
    return (
      <section className="patient-detail-page">
        <div className="page-header">
          <div>
            <h2>Patient not found</h2>
            <p>The patient could not be loaded.</p>
          </div>

          <button
            className="app-button"
            type="button"
            onClick={() => patientQuery.refetch()}
          >
            <RefreshCw size={16} />
            Retry
          </button>
        </div>

        <Link className="app-button" to="/patients">
          <ArrowLeft size={16} />
          Back to patients
        </Link>
      </section>
    );
  }

  return (
    <section className="patient-detail-page">
      <div className="page-header">
        <div>
          <button
            className="back-button"
            type="button"
            onClick={() => navigate("/patients")}
          >
            <ArrowLeft size={16} />
            Patients
          </button>

          <h2>
            {patient.firstName} {patient.lastName}
          </h2>

          <p>
            Medical record number: {patient.medicalRecordNumber}
          </p>
        </div>

        <div style={{ display: "flex", gap: "10px", alignItems: "center" }}>
          <span className="app-user-pill">
            {patient.active ? "Active" : "Inactive"}
          </span>

          <div className="header-actions" style={{ display: "flex", gap: "10px" }}>
            <button
              className="app-button"
              type="button"
              onClick={() =>
                navigate(`/patients/${patient.id}/clinical`)
              }
            >
              Clinical workspace
            </button>

            <button
              className="app-button"
              type="button"
              onClick={() => navigate(`/patients/${patient.id}/edit`)}
            >
              Edit patient
            </button>
            {patient.active && (
              <button
                className="app-button app-button-danger"
                type="button"
                disabled={deleteMutation.isPending}
                onClick={() => {
                  const confirmed = window.confirm(
                    `Deactivate ${patient.firstName} ${patient.lastName}? ` +
                      "They will no longer appear in the active patient list.",
                  );
                  if (confirmed) {
                    deleteMutation.mutate();
                  }
                }}
              >
                {deleteMutation.isPending
                  ? "Deactivating..."
                  : "Deactivate patient"}
              </button>
            )}
          </div>
        </div>
      </div>

      {deleteMutation.isError && (
        <div className="alert" role="alert">
          Unable to deactivate this patient. Please try again.
        </div>
      )}

      <div className="detail-grid patient-detail-grid">
        <article className="card patient-info-card">
          <h3>Personal information</h3>

          <dl className="details-list">
            <div>
              <dt>First name</dt>
              <dd>{patient.firstName}</dd>
            </div>

            <div>
              <dt>Last name</dt>
              <dd>{patient.lastName}</dd>
            </div>

            <div>
              <dt>Date of birth</dt>
              <dd>{patient.dateOfBirth}</dd>
            </div>

            <div>
              <dt>Sex</dt>
              <dd>{patient.sex}</dd>
            </div>

            <div>
              <dt>Blood group</dt>
              <dd>{display(patient.bloodGroup)}</dd>
            </div>
          </dl>
        </article>

        <article className="card patient-info-card">
          <h3>Contact information</h3>

          <dl className="details-list">
            <div>
              <dt>Email</dt>
              <dd>{display(patient.email)}</dd>
            </div>

            <div>
              <dt>Phone</dt>
              <dd>{display(patient.phoneNumber)}</dd>
            </div>

            <div>
              <dt>Address</dt>
              <dd>{display(patient.addressLine1)}</dd>
            </div>

            <div>
              <dt>City</dt>
              <dd>{display(patient.city)}</dd>
            </div>

            <div>
              <dt>Country</dt>
              <dd>{display(patient.country)}</dd>
            </div>
          </dl>
        </article>

        <article className="card patient-info-card">
          <h3>Emergency contact</h3>

          <dl className="details-list">
            <div>
              <dt>Name</dt>
              <dd>{display(patient.emergencyContactName)}</dd>
            </div>

            <div>
              <dt>Phone</dt>
              <dd>{display(patient.emergencyContactPhone)}</dd>
            </div>
          </dl>
        </article>

        <article className="card patient-info-card">
          <h3>Clinical notes</h3>

          <dl className="details-list">
            <div>
              <dt>Allergies</dt>
              <dd>{display(patient.allergies)}</dd>
            </div>
          </dl>
        </article>
      </div>
    </section>
  );
}