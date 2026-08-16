import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import axios from "axios";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { z } from "zod";
import { createAppointment } from "./api";

const appointmentSchema = z
  .object({
    patientId: z.coerce.number().int().positive(),
    doctorId: z.coerce.number().int().positive(),
    departmentId: z.coerce.number().int().positive(),
    scheduledStartAt: z.string().min(
      1,
      "Start date and time are required.",
    ),
    scheduledEndAt: z.string().min(
      1,
      "End date and time are required.",
    ),
    reason: z.string().trim().min(
      1,
      "Reason is required.",
    ).max(512),
    schedulingNotes: z.string().max(1024),
  })
  .refine(
    (values) =>
      new Date(values.scheduledEndAt).getTime() >
      new Date(values.scheduledStartAt).getTime(),
    {
      message: "End time must be after start time.",
      path: ["scheduledEndAt"],
    },
  );

type AppointmentFormInput = z.input<
  typeof appointmentSchema
>;
type AppointmentFormValues = z.output<
  typeof appointmentSchema
>;

function toUtcIsoFromLocal(value: string) {
  const [datePart, timePart] = value.split("T");
  const [year, month, day] = datePart.split("-").map(Number);
  const [hour, minute] = timePart.split(":").map(Number);
  const localDate = new Date(
    year,
    month - 1,
    day,
    hour,
    minute,
    0,
    0,
  );
  return localDate.toISOString();
}

function getLocalDateTimeMinimum() {
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  now.setSeconds(0, 0);
  const minutes = now.getMinutes();
  const roundedMinutes = Math.ceil(minutes / 5) * 5;
  now.setMinutes(roundedMinutes);
  return now.toISOString().slice(0, 16);
}

export function AppointmentForm() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const minDateTime = getLocalDateTimeMinimum();

  const form = useForm<
    AppointmentFormInput,
    unknown,
    AppointmentFormValues
  >({
    resolver: zodResolver(appointmentSchema),
    defaultValues: {
      patientId: "",
      doctorId: "",
      departmentId: "",
      scheduledStartAt: "",
      scheduledEndAt: "",
      reason: "",
      schedulingNotes: "",
    },
  });

  const mutation = useMutation({
    mutationFn: (values: AppointmentFormValues) =>
      createAppointment({
        patientId: values.patientId,
        doctorId: values.doctorId,
        departmentId: values.departmentId,
        scheduledStartAt: toUtcIsoFromLocal(
          values.scheduledStartAt,
        ),
        scheduledEndAt: toUtcIsoFromLocal(
          values.scheduledEndAt,
        ),
        reason: values.reason.trim(),
        schedulingNotes:
          values.schedulingNotes.trim() || undefined,
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["appointments"],
      });

      navigate("/appointments", { replace: true });
    },
  });

  return (
    <section>
      <div className="page-header">
        <div>
          <h2>Schedule appointment</h2>
          <p>Create a consultation for a patient and doctor.</p>
        </div>
      </div>

      <form
        className="card"
        onSubmit={form.handleSubmit((values) =>
          mutation.mutate(values),
        )}
      >
        <div className="card-grid">
          <Field
            label="Patient ID"
            error={form.formState.errors.patientId?.message}
          >
            <input
              type="number"
              min="1"
              {...form.register("patientId")}
            />
          </Field>

          <Field
            label="Doctor ID"
            error={form.formState.errors.doctorId?.message}
          >
            <input
              type="number"
              min="1"
              {...form.register("doctorId")}
            />
          </Field>

          <Field
            label="Department ID"
            error={form.formState.errors.departmentId?.message}
          >
            <input
              type="number"
              min="1"
              {...form.register("departmentId")}
            />
          </Field>

          <Field
            label="Scheduled start"
            error={
              form.formState.errors.scheduledStartAt?.message
            }
          >
            <input
              type="datetime-local"
              min={minDateTime}
              {...form.register("scheduledStartAt")}
            />
          </Field>

          <Field
            label="Scheduled end"
            error={
              form.formState.errors.scheduledEndAt?.message
            }
          >
            <input
              type="datetime-local"
              min={minDateTime}
              {...form.register("scheduledEndAt")}
            />
          </Field>

          <Field
            label="Reason"
            error={form.formState.errors.reason?.message}
          >
            <input {...form.register("reason")} />
          </Field>

          <div className="field field-wide">
            <label htmlFor="schedulingNotes">
              Scheduling notes
            </label>
            <textarea
              id="schedulingNotes"
              {...form.register("schedulingNotes")}
            />
          </div>
        </div>

        {mutation.isError && (
          <div className="alert" role="alert">
            {getApiErrorMessage(mutation.error)}
          </div>
        )}

        <div className="form-actions">
          <button
            className="app-button"
            type="button"
            onClick={() => navigate("/appointments")}
          >
            Cancel
          </button>

          <button
            className="app-button"
            type="submit"
            disabled={mutation.isPending}
          >
            {mutation.isPending
              ? "Scheduling..."
              : "Schedule appointment"}
          </button>
        </div>
      </form>
    </section>
  );
}

function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <div className="field">
      <label>{label}</label>
      {children}
      {error && (
        <span className="helper-text">{error}</span>
      )}
    </div>
  );
}

function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data;
    if (Array.isArray(data?.fieldErrors)) {
      return data.fieldErrors
        .map(
          (item: { field: string; message: string }) =>
            `${item.field}: ${item.message}`,
        )
        .join(" | ");
    }
    if (typeof data?.message === "string") {
      return data.message;
    }
    return `Request failed with status ${
      error.response?.status ?? "unknown"
    }.`;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "Unable to create appointment.";
}
