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
import { ArrowLeft, Save, UserRound } from "lucide-react";
import { z } from "zod";

import { getPatient, updatePatient } from "./api";

const editSchema = z.object({
  medicalRecordNumber: z.string().trim().min(1).max(32),
  firstName: z.string().trim().min(1).max(128),
  lastName: z.string().trim().min(1).max(128),
  dateOfBirth: z.string().min(1),
  sex: z.enum(["MALE", "FEMALE", "OTHER", "UNKNOWN"]),
  email: z.union([
    z.string().email("Enter a valid email"),
    z.literal(""),
  ]),
  phoneNumber: z.string().max(32),
  addressLine1: z.string().max(256),
  city: z.string().max(128),
  state: z.string().max(128),
  postalCode: z.string().max(32),
  country: z.string().max(128),
  emergencyContactName: z.string().max(256),
  emergencyContactPhone: z.string().max(32),
  bloodGroup: z.string().max(8),
  allergies: z.string().max(1024),
});

type EditValues = z.infer<typeof editSchema>;

function optional(value: string) {
  const trimmed = value.trim();
  return trimmed === "" ? undefined : trimmed;
}

export function PatientEditPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { patientId } = useParams<{ patientId: string }>();

  const numericId = Number(patientId);

  const patientQuery = useQuery({
    queryKey: ["patient", patientId],
    queryFn: () => getPatient(numericId),
    enabled: Number.isFinite(numericId),
  });

  const patient = patientQuery.data?.data;

  const form = useForm<EditValues>({
    resolver: zodResolver(editSchema),
    values: patient
      ? {
          medicalRecordNumber:
            patient.medicalRecordNumber,
          firstName: patient.firstName,
          lastName: patient.lastName,
          dateOfBirth: patient.dateOfBirth,
          sex: patient.sex,
          email: patient.email ?? "",
          phoneNumber: patient.phoneNumber ?? "",
          addressLine1: patient.addressLine1 ?? "",
          city: patient.city ?? "",
          state: patient.state ?? "",
          postalCode: patient.postalCode ?? "",
          country: patient.country ?? "",
          emergencyContactName:
            patient.emergencyContactName ?? "",
          emergencyContactPhone:
            patient.emergencyContactPhone ?? "",
          bloodGroup: patient.bloodGroup ?? "",
          allergies: patient.allergies ?? "",
        }
      : undefined,
  });

  const mutation = useMutation({
    mutationFn: (values: EditValues) =>
      updatePatient(numericId, {
        medicalRecordNumber:
          values.medicalRecordNumber.trim(),
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        dateOfBirth: values.dateOfBirth,
        sex: values.sex,
        email: optional(values.email),
        phoneNumber: optional(values.phoneNumber),
        addressLine1: optional(values.addressLine1),
        city: optional(values.city),
        state: optional(values.state),
        postalCode: optional(values.postalCode),
        country: optional(values.country),
        emergencyContactName: optional(
          values.emergencyContactName,
        ),
        emergencyContactPhone: optional(
          values.emergencyContactPhone,
        ),
        bloodGroup: optional(values.bloodGroup),
        allergies: optional(values.allergies),
      }),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["patient", patientId],
      });

      await queryClient.invalidateQueries({
        queryKey: ["patients"],
      });

      navigate(`/patients/${patientId}`);
    },
  });

  if (patientQuery.isLoading) {
    return (
      <section className="patient-form-page">
        <div className="patient-form-card state-card">
          <div className="state-card-icon">
            <UserRound size={24} />
          </div>

          <h3>Loading patient</h3>

          <p>
            Preparing the patient record for editing...
          </p>
        </div>
      </section>
    );
  }

  if (patientQuery.isError || !patient) {
    return (
      <section className="patient-form-page">
        <div className="alert" role="alert">
          Unable to load this patient.
        </div>
      </section>
    );
  }

  return (
    <section className="patient-form-page">
      <header className="patient-form-header">
        <div>
          <Link
            className="back-button"
            to={`/patients/${patient.id}`}
          >
            <ArrowLeft size={16} />
            Patient details
          </Link>

          <span className="eyebrow">
            Patient record
          </span>

          <h2>Edit patient</h2>

          <p>
            Update {patient.firstName}{" "}
            {patient.lastName}&apos;s information while
            preserving the clinical record.
          </p>
        </div>

        <div className="patient-form-header-meta">
          <span className="patient-record-chip">
            {patient.medicalRecordNumber}
          </span>

          <span
            className={`patient-status-chip ${
              patient.active ? "active" : "inactive"
            }`}
          >
            <i />
            {patient.active ? "Active" : "Inactive"}
          </span>
        </div>
      </header>

      <form
        className="patient-form-shell"
        onSubmit={form.handleSubmit((values) =>
          mutation.mutate(values),
        )}
      >
        <FormSection
          eyebrow="Identity"
          title="Patient identity"
          description="Core identity information used throughout the clinical system."
        >
          <Field
            label="Medical record number"
            error={
              form.formState.errors
                .medicalRecordNumber?.message
            }
            input={
              <input
                {...form.register(
                  "medicalRecordNumber",
                )}
              />
            }
          />

          <Field
            label="First name"
            error={
              form.formState.errors.firstName?.message
            }
            input={
              <input
                {...form.register("firstName")}
              />
            }
          />

          <Field
            label="Last name"
            error={
              form.formState.errors.lastName?.message
            }
            input={
              <input
                {...form.register("lastName")}
              />
            }
          />

          <Field
            label="Date of birth"
            error={
              form.formState.errors.dateOfBirth?.message
            }
            input={
              <input
                type="date"
                {...form.register("dateOfBirth")}
              />
            }
          />

          <Field
            label="Sex"
            input={
              <select {...form.register("sex")}>
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
                placeholder="A+, O-, AB+"
                {...form.register("bloodGroup")}
              />
            }
          />
        </FormSection>

        <FormSection
          eyebrow="Contact"
          title="Contact information"
          description="How the hospital can reach and locate the patient."
        >
          <Field
            label="Email"
            error={
              form.formState.errors.email?.message
            }
            input={
              <input
                type="email"
                {...form.register("email")}
              />
            }
          />

          <Field
            label="Phone number"
            input={
              <input
                {...form.register("phoneNumber")}
              />
            }
          />

          <Field
            label="Address"
            wide
            input={
              <input
                {...form.register("addressLine1")}
              />
            }
          />

          <Field
            label="City"
            input={
              <input {...form.register("city")} />
            }
          />

          <Field
            label="State / region"
            input={
              <input {...form.register("state")} />
            }
          />

          <Field
            label="Postal code"
            input={
              <input
                {...form.register("postalCode")}
              />
            }
          />

          <Field
            label="Country"
            input={
              <input
                {...form.register("country")}
              />
            }
          />
        </FormSection>

        <FormSection
          eyebrow="Safety"
          title="Emergency & clinical information"
          description="Important information that may be needed during patient care."
          tone="safety"
        >
          <Field
            label="Emergency contact"
            input={
              <input
                {...form.register(
                  "emergencyContactName",
                )}
              />
            }
          />

          <Field
            label="Emergency phone"
            input={
              <input
                {...form.register(
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
                {...form.register("allergies")}
                placeholder="Record known allergies or enter 'None recorded'."
              />
            }
          />
        </FormSection>

        {mutation.isError && (
          <div className="alert" role="alert">
            Unable to save changes. Check the submitted
            values and try again.
          </div>
        )}

        <footer className="patient-form-actions">
          <Link
            className="app-button"
            to={`/patients/${patient.id}`}
          >
            Cancel
          </Link>

          <button
            className="app-button primary patient-save-button"
            type="submit"
            disabled={mutation.isPending}
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
        wide ? "patient-form-field-wide" : ""
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