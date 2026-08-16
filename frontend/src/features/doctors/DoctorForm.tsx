import type { ReactNode } from "react";

import { zodResolver } from "@hookform/resolvers/zod";
import {
  useMutation,
  useQueryClient,
} from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Save,
  Stethoscope,
} from "lucide-react";
import { z } from "zod";

import { createDoctor } from "./api";

const doctorSchema = z.object({
  userAccountId:
    z.coerce
      .number()
      .int()
      .positive(
        "A valid user account ID is required.",
      ),

  departmentId:
    z.coerce
      .number()
      .int()
      .positive(
        "A valid department ID is required.",
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

  firstName:
    z.string()
      .trim()
      .min(
        1,
        "First name is required.",
      )
      .max(128),

  lastName:
    z.string()
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
        "Duration must be greater than zero.",
      ),
});

type DoctorFormInput =
  z.input<typeof doctorSchema>;

type DoctorFormValues =
  z.output<typeof doctorSchema>;

export function DoctorForm() {
  const navigate =
    useNavigate();

  const queryClient =
    useQueryClient();

  const form = useForm<
    DoctorFormInput,
    unknown,
    DoctorFormValues
  >({
    resolver:
      zodResolver(
        doctorSchema,
      ),

    defaultValues: {
      userAccountId: "",
      departmentId: "",
      professionalRegistrationNumber:
        "",
      firstName: "",
      lastName: "",
      specialty: "",
      phoneNumber: "",
      email: "",
      consultationDurationMinutes: 30,
    },
  });

  const mutation =
    useMutation({
      mutationFn:
        (
          values: DoctorFormValues,
        ) =>
          createDoctor({
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
              values.specialty.trim() ||
              undefined,

            phoneNumber:
              values.phoneNumber.trim() ||
              undefined,

            email:
              values.email.trim() ||
              undefined,

            consultationDurationMinutes:
              values.consultationDurationMinutes,
          }),

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

  return (
    <section className="doctor-form-page">
      <header className="doctor-form-header">
        <div>
          <button
            className="back-button"
            type="button"
            onClick={() =>
              navigate("/doctors")
            }
          >
            <ArrowLeft size={15} />
            Doctors
          </button>

          <span className="eyebrow">
            Clinical directory
          </span>

          <h2>
            Add doctor
          </h2>

          <p>
            Create a professional profile
            for clinical operations,
            scheduling, and patient care.
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
          description="Connect the new physician to an existing hospital account and department."
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
          description="Define the physician's identity as it will appear across MedCore."
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
              placeholder="DOC-2026-001"
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
              placeholder="First name"
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
              placeholder="Last name"
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
          description="Communication details available to authorized hospital staff."
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
              placeholder="+216..."
            />
          </FormField>

          <FormField
            label="Email"
            error={
              form.formState
                .errors.email
                ?.message
            }
          >
            <input
              type="email"
              {...form.register(
                "email",
              )}
              placeholder="doctor@medcore.local"
            />
          </FormField>
        </FormSection>

        {mutation.isError && (
          <div
            className="alert"
            role="alert"
          >
            Unable to create doctor.
            Confirm that the account and
            department IDs exist and that
            the registration number is
            unique.
          </div>
        )}

        <footer className="doctor-form-actions">
          <button
            className="app-button"
            type="button"
            onClick={() =>
              navigate("/doctors")
            }
          >
            Cancel
          </button>

          <button
            className="app-button primary"
            type="submit"
            disabled={
              mutation.isPending
            }
          >
            <Save size={15} />

            {mutation.isPending
              ? "Creating..."
              : "Create doctor"}
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