import type { ReactNode } from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import {
  Link,
  useNavigate,
  useParams,
} from "react-router-dom";
import {
  ArrowLeft,
  Save,
  Stethoscope,
} from "lucide-react";
import { z } from "zod";

import {
  getDoctor,
  updateDoctor,
} from "./api";

const doctorSchema = z.object({
  userAccountId:
    z.coerce
      .number()
      .int()
      .positive(
        "User account ID must be positive.",
      ),

  departmentId:
    z.coerce
      .number()
      .int()
      .positive(
        "Department ID must be positive.",
      ),

  professionalRegistrationNumber:
    z
      .string()
      .trim()
      .min(
        1,
        "Registration number is required.",
      )
      .max(64),

  firstName: z
    .string()
    .trim()
    .min(
      1,
      "First name is required.",
    )
    .max(128),

  lastName: z
    .string()
    .trim()
    .min(
      1,
      "Last name is required.",
    )
    .max(128),

  specialty:
    z.string().max(256),

  phoneNumber:
    z.string().max(32),

  email: z.union([
    z
      .string()
      .email(
        "Enter a valid email.",
      ),
    z.literal(""),
  ]),

  consultationDurationMinutes:
    z.coerce
      .number()
      .int()
      .positive(
        "Duration must be positive.",
      ),
});

type DoctorFormInput =
  z.input<typeof doctorSchema>;

type DoctorFormValues =
  z.output<typeof doctorSchema>;

export function DoctorEditPage() {
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

  const form = useForm<
    DoctorFormInput,
    unknown,
    DoctorFormValues
  >({
    resolver:
      zodResolver(
        doctorSchema,
      ),

    values: doctor
      ? {
          userAccountId:
            doctor.userAccountId,

          departmentId:
            doctor.departmentId,

          professionalRegistrationNumber:
            doctor.professionalRegistrationNumber,

          firstName:
            doctor.firstName,

          lastName:
            doctor.lastName,

          specialty:
            doctor.specialty ?? "",

          phoneNumber:
            doctor.phoneNumber ?? "",

          email:
            doctor.email ?? "",

          consultationDurationMinutes:
            doctor.consultationDurationMinutes,
        }
      : undefined,
  });

  const mutation =
    useMutation({
      mutationFn:
        (
          values: DoctorFormValues,
        ) =>
          updateDoctor(
            numericId,
            {
              userAccountId:
                values.userAccountId,

              departmentId:
                values.departmentId,

              professionalRegistrationNumber:
                values.professionalRegistrationNumber.trim(),

              firstName:
                values.firstName.trim(),

              lastName:
                values.lastName.trim(),

              specialty:
                values.specialty.trim(),

              phoneNumber:
                values.phoneNumber.trim(),

              email:
                values.email.trim(),

              consultationDurationMinutes:
                values.consultationDurationMinutes,
            },
          ),

      onSuccess:
        async () => {
          await Promise.all([
            queryClient.invalidateQueries(
              {
                queryKey: [
                  "doctor",
                  numericId,
                ],
              },
            ),

            queryClient.invalidateQueries(
              {
                queryKey: [
                  "doctors",
                ],
              },
            ),
          ]);

          navigate(
            `/doctors/${numericId}`,
            {
              replace: true,
            },
          );
        },
    });

  if (doctorQuery.isLoading) {
    return (
      <section className="doctor-form-page">
        <div className="state-card">
          <div className="state-card-icon">
            <Stethoscope size={25} />
          </div>

          <h3>
            Loading doctor
          </h3>

          <p>
            Preparing the professional
            profile...
          </p>
        </div>
      </section>
    );
  }

  if (
    doctorQuery.isError ||
    !doctor
  ) {
    return (
      <section className="doctor-form-page">
        <div
          className="alert"
          role="alert"
        >
          Unable to load this doctor.
        </div>

        <Link
          className="app-button"
          to="/doctors"
        >
          <ArrowLeft size={16} />
          Back to doctors
        </Link>
      </section>
    );
  }

  return (
    <section className="doctor-form-page">
      <header className="doctor-form-header">
        <div>
          <Link
            className="back-button"
            to={`/doctors/${doctor.id}`}
          >
            <ArrowLeft size={15} />
            Doctor details
          </Link>

          <span className="eyebrow">
            Physician profile
          </span>

          <h2>
            Edit doctor
          </h2>

          <p>
            Update Dr.{" "}
            {doctor.firstName}{" "}
            {doctor.lastName}&apos;s
            professional profile.
          </p>
        </div>

        <div className="doctor-form-avatar">
          <Stethoscope size={21} />
        </div>
      </header>

      <form
        className="doctor-form-shell"
        onSubmit={form.handleSubmit(
          (values) =>
            mutation.mutate(
              values,
            ),
        )}
      >
        <FormSection
          eyebrow="Account"
          title="System identity"
          description="Link this professional profile to the hospital's internal account and department."
        >
          <FormField
            label="User account ID"
            error={
              form.formState
                .errors
                .userAccountId
                ?.message
            }
          >
            <input
              type="number"
              min="1"
              {...form.register(
                "userAccountId",
              )}
            />
          </FormField>

          <FormField
            label="Department ID"
            error={
              form.formState
                .errors
                .departmentId
                ?.message
            }
          >
            <input
              type="number"
              min="1"
              {...form.register(
                "departmentId",
              )}
            />
          </FormField>
        </FormSection>

        <FormSection
          eyebrow="Professional"
          title="Clinical identity"
          description="Professional information shown across doctor, appointment, and clinical workflows."
        >
          <FormField
            label="Professional registration number"
            error={
              form.formState
                .errors
                .professionalRegistrationNumber
                ?.message
            }
          >
            <input
              {...form.register(
                "professionalRegistrationNumber",
              )}
            />
          </FormField>

          <FormField
            label="Specialty"
            error={
              form.formState
                .errors
                .specialty
                ?.message
            }
          >
            <input
              {...form.register(
                "specialty",
              )}
              placeholder="Cardiology"
            />
          </FormField>

          <FormField
            label="First name"
            error={
              form.formState
                .errors.firstName
                ?.message
            }
          >
            <input
              {...form.register(
                "firstName",
              )}
            />
          </FormField>

          <FormField
            label="Last name"
            error={
              form.formState
                .errors.lastName
                ?.message
            }
          >
            <input
              {...form.register(
                "lastName",
              )}
            />
          </FormField>

          <FormField
            label="Consultation duration"
            error={
              form.formState
                .errors
                .consultationDurationMinutes
                ?.message
            }
          >
            <div className="doctor-duration-input">
              <input
                type="number"
                min="1"
                {...form.register(
                  "consultationDurationMinutes",
                )}
              />

              <span>
                minutes
              </span>
            </div>
          </FormField>
        </FormSection>

        <FormSection
          eyebrow="Contact"
          title="Contact information"
          description="Communication details used by hospital staff and clinical workflows."
        >
          <FormField
            label="Phone number"
            error={
              form.formState
                .errors
                .phoneNumber
                ?.message
            }
          >
            <input
              {...form.register(
                "phoneNumber",
              )}
            />
          </FormField>

          <FormField
            label="Email"
            error={
              form.formState.errors.email
                ?.message
            }
          >
            <input
              type="email"
              {...form.register(
                "email",
              )}
            />
          </FormField>
        </FormSection>

        {mutation.isError && (
          <div
            className="alert"
            role="alert"
          >
            Unable to save changes.
            Check the submitted values,
            account ID, department ID, or
            registration number.
          </div>
        )}

        <footer className="doctor-form-actions">
          <Link
            className="app-button"
            to={`/doctors/${doctor.id}`}
          >
            Cancel
          </Link>

          <button
            className="app-button primary"
            type="submit"
            disabled={
              mutation.isPending
            }
          >
            <Save size={15} />

            {mutation.isPending
              ? "Saving changes..."
              : "Save changes"}
          </button>
        </footer>
      </form>
    </section>
  );
}

function FormSection({
  eyebrow,
  title,
  description,
  children,
}: {
  eyebrow: string;
  title: string;
  description: string;
  children: ReactNode;
}) {
  return (
    <section className="doctor-form-section">
      <header className="doctor-form-section-header">
        <span>{eyebrow}</span>

        <h3>{title}</h3>

        <p>{description}</p>
      </header>

      <div className="doctor-form-grid">
        {children}
      </div>
    </section>
  );
}

function FormField({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: ReactNode;
}) {
  return (
    <div className="field">
      <label>{label}</label>

      {children}

      {error && (
        <span className="helper-text">
          {error}
        </span>
      )}
    </div>
  );
}