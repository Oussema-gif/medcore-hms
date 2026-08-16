import type { ReactNode } from "react";

import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";

import {
  ArrowLeft,
  BriefcaseMedical,
  CalendarClock,
  Clock3,
  Edit3,
  Mail,
  Phone,
  RefreshCw,
  ShieldCheck,
  Stethoscope,
} from "lucide-react";

import {
  Link,
  useNavigate,
  useParams,
} from "react-router-dom";

import {
  deleteDoctor,
  getDoctor,
} from "./api";

import {
  AvailabilityPanel,
} from "./availability/AvailabilityPanel";

function display(
  value: string | null | undefined,
) {
  return value?.trim() || "—";
}

function getInitials(
  firstName: string,
  lastName: string,
) {
  return `${firstName.slice(
    0,
    1,
  )}${lastName.slice(0, 1)}`.toUpperCase();
}

export function DoctorDetailPage() {
  const navigate = useNavigate();
  const queryClient =
    useQueryClient();

  const {
    doctorId,
  } = useParams<{
    doctorId: string;
  }>();

  const numericId =
    Number(doctorId);

  const doctorQuery = useQuery({
    queryKey: [
      "doctor",
      numericId,
    ],

    queryFn: () =>
      getDoctor(numericId),

    enabled:
      Number.isInteger(
        numericId,
      ) &&
      numericId > 0,
  });

  const doctor =
    doctorQuery.data?.data;

  const deleteMutation =
    useMutation({
      mutationFn: () =>
        deleteDoctor(numericId),

      onSuccess:
        async () => {
          await queryClient.invalidateQueries(
            {
              queryKey: [
                "doctors",
              ],
            },
          );

          navigate(
            "/doctors",
            {
              replace: true,
            },
          );
        },
    });

  if (doctorQuery.isLoading) {
    return (
      <section className="doctor-detail-page">
        <div className="doctor-loading-card">
          <div className="doctor-loading-orb">
            <Stethoscope size={25} />
          </div>

          <div>
            <strong>
              Loading physician profile
            </strong>

            <span>
              Preparing clinical information
              and scheduling context...
            </span>
          </div>
        </div>
      </section>
    );
  }

  if (
    doctorQuery.isError ||
    !doctor
  ) {
    return (
      <section className="doctor-detail-page">
        <div className="doctor-error-card">
          <div className="doctor-error-icon">
            <Stethoscope size={22} />
          </div>

          <div className="doctor-error-copy">
            <span>
              Clinical directory
            </span>

            <h2>
              Doctor not found
            </h2>

            <p>
              Unable to load this physician
              profile.
            </p>
          </div>

          <div className="doctor-error-actions">
            <button
              className="app-button"
              type="button"
              onClick={() =>
                doctorQuery.refetch()
              }
            >
              <RefreshCw size={15} />
              Retry
            </button>

            <Link
              className="app-button"
              to="/doctors"
            >
              <ArrowLeft size={15} />
              Doctors
            </Link>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="doctor-detail-page">
      {/* =====================================================
          TOP NAVIGATION
          ===================================================== */}

      <div className="doctor-detail-topbar">
        <Link
          className="doctor-detail-back"
          to="/doctors"
        >
          <span className="doctor-detail-back-icon">
            <ArrowLeft size={15} />
          </span>

          <span>
            Doctor directory
          </span>
        </Link>

        <div className="doctor-detail-topbar-meta">
          <span>
            Physician #{doctor.id}
          </span>

          <span className="doctor-detail-topbar-dot" />

          <span>
            Clinical profile
          </span>
        </div>
      </div>

      {/* =====================================================
          HERO
          ===================================================== */}

      <section className="doctor-profile-hero-v2">
        <div className="doctor-profile-glow doctor-profile-glow-one" />
        <div className="doctor-profile-glow doctor-profile-glow-two" />

        <div className="doctor-profile-main">
          <div className="doctor-profile-avatar-v2">
            <span>
              {getInitials(
                doctor.firstName,
                doctor.lastName,
              )}
            </span>

            <div className="doctor-avatar-status">
              <span />
            </div>
          </div>

          <div className="doctor-profile-main-copy">
            <div className="doctor-profile-eyebrow">
              <span>
                Medical professional
              </span>

              <span className="doctor-profile-id">
                ID {doctor.id}
              </span>
            </div>

            <h1>
              Dr. {doctor.firstName}{" "}
              {doctor.lastName}
            </h1>

            <p className="doctor-profile-specialty">
              {display(
                doctor.specialty,
              )}
            </p>

            <div className="doctor-profile-tags">
              <span className="doctor-profile-tag doctor-profile-tag-accent">
                <Stethoscope size={12} />
                Physician
              </span>

              <span className="doctor-profile-tag">
                Registration{" "}
                {
                  doctor.professionalRegistrationNumber
                }
              </span>

              <span className="doctor-profile-tag">
                Department #{doctor.departmentId}
              </span>
            </div>
          </div>
        </div>

        <div className="doctor-profile-hero-side">
          <span
            className={`doctor-availability-status ${
              doctor.active
                ? "is-active"
                : "is-inactive"
            }`}
          >
            <i />

            {doctor.active
              ? "Active physician"
              : "Inactive physician"}
          </span>

          <div className="doctor-profile-actions">
            {doctor.active && (
              <button
                className="app-button primary"
                type="button"
                onClick={() =>
                  navigate(
                    `/doctors/${doctor.id}/edit`,
                  )
                }
              >
                <Edit3 size={15} />
                Edit profile
              </button>
            )}

            {doctor.active && (
              <button
                className="app-button app-button-danger"
                type="button"
                disabled={
                  deleteMutation.isPending
                }
                onClick={() => {
                  const confirmed =
                    window.confirm(
                      `Deactivate Dr. ${doctor.firstName} ${doctor.lastName}?`,
                    );

                  if (confirmed) {
                    deleteMutation.mutate();
                  }
                }}
              >
                {deleteMutation.isPending
                  ? "Deactivating..."
                  : "Deactivate"}
              </button>
            )}
          </div>
        </div>
      </section>

      {deleteMutation.isError && (
        <div
          className="alert"
          role="alert"
        >
          Unable to deactivate this
          physician. Please try again.
        </div>
      )}

      {/* =====================================================
          QUICK STATS
          ===================================================== */}

      <section className="doctor-stat-grid">
        <DoctorStat
          icon={
            <Stethoscope size={17} />
          }
          label="Specialty"
          value={
            display(
              doctor.specialty,
            )
          }
          tone="cyan"
        />

        <DoctorStat
          icon={
            <Clock3 size={17} />
          }
          label="Consultation"
          value={`${doctor.consultationDurationMinutes} min`}
          tone="blue"
        />

        <DoctorStat
          icon={
            <BriefcaseMedical size={17} />
          }
          label="Department"
          value={`#${doctor.departmentId}`}
          tone="violet"
        />

        <DoctorStat
          icon={
            <ShieldCheck size={17} />
          }
          label="Profile status"
          value={
            doctor.active
              ? "Operational"
              : "Inactive"
          }
          tone="green"
        />
      </section>

      {/* =====================================================
          INFORMATION MODULES
          ===================================================== */}

      <section className="doctor-information-grid-v2">
        <DoctorInfoCard
          eyebrow="Professional context"
          title="Clinical identity"
          icon={
            <BriefcaseMedical
              size={17}
            />
          }
        >
          <InfoRow
            label="Registration number"
            value={
              doctor.professionalRegistrationNumber
            }
          />

          <InfoRow
            label="Specialty"
            value={display(
              doctor.specialty,
            )}
          />

          <InfoRow
            label="Department"
            value={`Department #${doctor.departmentId}`}
          />

          <InfoRow
            label="Consultation duration"
            value={`${doctor.consultationDurationMinutes} minutes`}
          />
        </DoctorInfoCard>

        <DoctorInfoCard
          eyebrow="Communication"
          title="Contact information"
          icon={
            <Mail size={17} />
          }
          tone="blue"
        >
          <InfoRow
            icon={
              <Mail size={13} />
            }
            label="Email"
            value={display(
              doctor.email,
            )}
          />

          <InfoRow
            icon={
              <Phone size={13} />
            }
            label="Phone"
            value={display(
              doctor.phoneNumber,
            )}
          />

          <InfoRow
            label="User account"
            value={`Account #${doctor.userAccountId}`}
          />
        </DoctorInfoCard>
      </section>

      {/* =====================================================
          AVAILABILITY
          ===================================================== */}

      <section className="doctor-scheduling-card">
        <header className="doctor-scheduling-header">
          <div className="doctor-scheduling-heading">
            <div className="doctor-scheduling-icon">
              <CalendarClock size={19} />
            </div>

            <div>
              <span className="doctor-scheduling-eyebrow">
                Scheduling workspace
              </span>

              <h2>Doctor availability</h2>

              <p>
                Configure working hours and availability
                windows for this physician.
              </p>
            </div>
          </div>

          <div className="doctor-scheduling-status">
            <span className="doctor-scheduling-status-dot" />

            <div>
              <strong>Scheduling active</strong>
              <small>Availability controls online</small>
            </div>
          </div>
        </header>

        <div className="doctor-scheduling-panel-body">
          <AvailabilityPanel doctorId={doctor.id} />
        </div>
      </section>
    </section>
  );
}

function DoctorStat({
  icon,
  label,
  value,
  tone,
}: {
  icon: ReactNode;
  label: string;
  value: string;
  tone:
    | "cyan"
    | "blue"
    | "violet"
    | "green";
}) {
  return (
    <article
      className={`doctor-stat-card doctor-stat-${tone}`}
    >
      <div className="doctor-stat-icon">
        {icon}
      </div>

      <div className="doctor-stat-copy">
        <span>
          {label}
        </span>

        <strong>
          {value}
        </strong>
      </div>
    </article>
  );
}

function DoctorInfoCard({
  eyebrow,
  title,
  icon,
  tone = "cyan",
  children,
}: {
  eyebrow: string;
  title: string;
  icon: ReactNode;
  tone?: "cyan" | "blue";
  children: ReactNode;
}) {
  return (
    <article
      className={`doctor-info-card-v2 doctor-info-card-${tone}`}
    >
      <header className="doctor-info-v2-header">
        <div>
          <span>
            {eyebrow}
          </span>

          <h3>
            {title}
          </h3>
        </div>

        <div className="doctor-info-v2-icon">
          {icon}
        </div>
      </header>

      <div className="doctor-info-v2-body">
        {children}
      </div>
    </article>
  );
}

function InfoRow({
  label,
  value,
  icon,
}: {
  label: string;
  value: string;
  icon?: ReactNode;
}) {
  return (
    <div className="doctor-info-v2-row">
      <dt>
        {icon}
        {label}
      </dt>

      <dd>
        {value}
      </dd>
    </div>
  );
}