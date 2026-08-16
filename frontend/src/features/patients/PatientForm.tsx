import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Save,
  UserPlus,
} from "lucide-react";
import { z } from "zod";

import {
  createPatient,
  type CreatePatientInput,
} from "./api";

const patientSchema = z.object({
  medicalRecordNumber: z
    .string()
    .trim()
    .min(1, "MRN is required")
    .max(32),

  firstName: z
    .string()
    .trim()
    .min(1, "First name is required")
    .max(128),

  lastName: z
    .string()
    .trim()
    .min(1, "Last name is required")
    .max(128),

  dateOfBirth: z
    .string()
    .min(1, "Date of birth is required"),

  sex: z.enum([
    "MALE",
    "FEMALE",
    "OTHER",
    "UNKNOWN",
  ]),

  email: z.union([
    z
      .string()
      .email("Enter a valid email"),
    z.literal(""),
  ]),

  phoneNumber: z.string().max(32),
  city: z.string().max(128),
  state: z.string().max(128),
  postalCode: z.string().max(32),
  country: z.string().max(128),
  addressLine1: z.string().max(256),

  emergencyContactName:
    z.string().max(256),

  emergencyContactPhone:
    z.string().max(32),

  bloodGroup: z.string().max(8),

  allergies:
    z.string().max(1024),
});

type PatientFormValues =
  z.infer<typeof patientSchema>;

const defaultValues: PatientFormValues = {
  medicalRecordNumber: "",
  firstName: "",
  lastName: "",
  dateOfBirth: "",
  sex: "UNKNOWN",
  email: "",
  phoneNumber: "",
  city: "",
  state: "",
  postalCode: "",
  country: "",
  addressLine1: "",
  emergencyContactName: "",
  emergencyContactPhone: "",
  bloodGroup: "",
  allergies: "",
};

function toOptional(value: string) {
  const trimmed = value.trim();

  return trimmed === ""
    ? undefined
    : trimmed;
}

export function PatientForm() {
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<PatientFormValues>({
    resolver: zodResolver(patientSchema),
    defaultValues,
  });

  const mutation = useMutation({
    mutationFn: (
      values: PatientFormValues,
    ) => {
      const input: CreatePatientInput = {
        medicalRecordNumber:
          values.medicalRecordNumber.trim(),

        firstName:
          values.firstName.trim(),

        lastName:
          values.lastName.trim(),

        dateOfBirth:
          values.dateOfBirth,

        sex:
          values.sex,

        email:
          toOptional(values.email),

        phoneNumber:
          toOptional(values.phoneNumber),

        city:
          toOptional(values.city),

        state:
          toOptional(values.state),

        postalCode:
          toOptional(values.postalCode),

        country:
          toOptional(values.country),

        addressLine1:
          toOptional(values.addressLine1),

        emergencyContactName:
          toOptional(
            values.emergencyContactName,
          ),

        emergencyContactPhone:
          toOptional(
            values.emergencyContactPhone,
          ),

        bloodGroup:
          toOptional(values.bloodGroup),

        allergies:
          toOptional(values.allergies),
      };

      return createPatient(input);
    },

    onSuccess: () => {
      navigate("/patients", {
        replace: true,
      });
    },
  });

  function onSubmit(
    values: PatientFormValues,
  ) {
    mutation.mutate(values);
  }

  return (
    <section className="patient-form-page">
      <header className="patient-form-header">
        <div>
          <button
            className="back-button patient-form-back"
            type="button"
            onClick={() =>
              navigate("/patients")
            }
          >
            <ArrowLeft size={16} />
            Patient directory
          </button>

          <span className="eyebrow">
            Clinical directory
          </span>

          <h2>Register patient</h2>

          <p>
            Create a complete patient profile
            ready for clinical workflows.
          </p>
        </div>

        <div className="patient-form-header-icon">
          <UserPlus size={22} />
        </div>
      </header>

      <form
        className="patient-form-shell"
        onSubmit={handleSubmit(onSubmit)}
      >
        <FormSection
          eyebrow="Identity"
          title="Patient identity"
          description="Core identity information for the hospital registry."
        >
          <Field
            label="Medical record number"
            error={
              errors.medicalRecordNumber?.message
            }
            input={
              <input
                id="medicalRecordNumber"
                {...register(
                  "medicalRecordNumber",
                )}
              />
            }
          />

          <Field
            label="First name"
            error={
              errors.firstName?.message
            }
            input={
              <input
                id="firstName"
                {...register("firstName")}
              />
            }
          />

          <Field
            label="Last name"
            error={
              errors.lastName?.message
            }
            input={
              <input
                id="lastName"
                {...register("lastName")}
              />
            }
          />

          <Field
            label="Date of birth"
            error={
              errors.dateOfBirth?.message
            }
            input={
              <input
                id="dateOfBirth"
                type="date"
                {...register("dateOfBirth")}
              />
            }
          />

          <Field
            label="Sex"
            input={
              <select
                id="sex"
                {...register("sex")}
              >
                <option value="UNKNOWN">
                  Unknown
                </option>
                <option value="FEMALE">
                  Female
                </option>
                <option value="MALE">
                  Male
                </option>
                <option value="OTHER">
                  Other
                </option>
              </select>
            }
          />

          <Field
            label="Blood group"
            input={
              <input
                id="bloodGroup"
                placeholder="A+, O-, AB+"
                {...register("bloodGroup")}
              />
            }
          />
        </FormSection>

        <FormSection
          eyebrow="Contact"
          title="Contact information"
          description="Address and communication details for the patient."
        >
          <Field
            label="Email"
            error={
              errors.email?.message
            }
            input={
              <input
                id="email"
                type="email"
                {...register("email")}
              />
            }
          />

          <Field
            label="Phone number"
            input={
              <input
                id="phoneNumber"
                {...register("phoneNumber")}
              />
            }
          />

          <Field
            label="Address"
            wide
            input={
              <input
                id="addressLine1"
                {...register(
                  "addressLine1",
                )}
              />
            }
          />

          <Field
            label="City"
            input={
              <input
                id="city"
                {...register("city")}
              />
            }
          />

          <Field
            label="State / region"
            input={
              <input
                id="state"
                {...register("state")}
              />
            }
          />

          <Field
            label="Postal code"
            input={
              <input
                id="postalCode"
                {...register(
                  "postalCode",
                )}
              />
            }
          />

          <Field
            label="Country"
            input={
              <input
                id="country"
                {...register("country")}
              />
            }
          />
        </FormSection>

        <FormSection
          eyebrow="Safety"
          title="Emergency & clinical information"
          description="Information that may be critical during patient care."
          tone="safety"
        >
          <Field
            label="Emergency contact"
            input={
              <input
                id="emergencyContactName"
                {...register(
                  "emergencyContactName",
                )}
              />
            }
          />

          <Field
            label="Emergency phone"
            input={
              <input
                id="emergencyContactPhone"
                {...register(
                  "emergencyContactPhone",
                )}
              />
            }
          />

          <Field
            label="Allergies"
            wide
            input={
              <textarea
                id="allergies"
                {...register("allergies")}
                placeholder="Record known allergies or enter 'None recorded'."
              />
            }
          />
        </FormSection>

        {mutation.isError && (
          <div
            className="alert"
            role="alert"
          >
            Unable to create the patient.
            Check for duplicate data or invalid
            fields and try again.
          </div>
        )}

        <footer className="patient-form-actions">
          <button
            className="app-button"
            type="button"
            onClick={() =>
              navigate("/patients")
            }
          >
            Cancel
          </button>

          <button
            className="app-button primary patient-save-button"
            type="submit"
            disabled={mutation.isPending}
          >
            <Save size={15} />

            {mutation.isPending
              ? "Creating patient..."
              : "Create patient"}
          </button>
        </footer>
      </form>
    </section>
  );
}

type FormSectionProps = {
  eyebrow: string;
  title: string;
  description: string;
  tone?: "default" | "safety";
  children: React.ReactNode;
};

function FormSection({
  eyebrow,
  title,
  description,
  tone = "default",
  children,
}: FormSectionProps) {
  return (
    <section
      className={`patient-form-section ${
        tone === "safety"
          ? "patient-form-section-safety"
          : ""
      }`}
    >
      <header className="patient-form-section-header">
        <div>
          <span>{eyebrow}</span>
          <h3>{title}</h3>
          <p>{description}</p>
        </div>
      </header>

      <div className="patient-form-grid">
        {children}
      </div>
    </section>
  );
}

type FieldProps = {
  label: string;
  input: React.ReactNode;
  error?: string;
  wide?: boolean;
};

function Field({
  label,
  input,
  error,
  wide = false,
}: FieldProps) {
  return (
    <div
      className={`field patient-form-field ${
        wide
          ? "patient-form-field-wide"
          : ""
      }`}
    >
      <label>{label}</label>

      {input}

      {error && (
        <span className="helper-text">
          {error}
        </span>
      )}
    </div>
  );
}