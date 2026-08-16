import { zodResolver } from "@hookform/resolvers/zod";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import {
  useForm,
  type UseFormRegisterReturn,
} from "react-hook-form";
import {
  ClipboardPlus,
  FileText,
  ShieldCheck,
} from "lucide-react";
import { z } from "zod";
import axios from "axios";

import {
  createMedicalRecord,
  getMedicalRecordsByPatientId,
  type MedicalRecord,
  type CreateMedicalRecordInput,
} from "./api";

const recordSchema = z.object({
  doctorId: z.coerce
    .number()
    .int()
    .positive("Doctor ID is required."),

  encounterAt: z.string().min(
    1,
    "Encounter date and time are required.",
  ),

  symptoms: z
    .string()
    .max(10000)
    .optional(),

  clinicalNotes: z
    .string()
    .trim()
    .min(
      1,
      "Clinical notes are required.",
    )
    .max(10000),
});

type RecordFormInput =
  z.input<typeof recordSchema>;

type RecordFormValues =
  z.output<typeof recordSchema>;

type MedicalRecordsPanelProps = {
  patientId: number;
  appointmentId?: number;
  doctorId?: number;
  onRecordReady?: (
    medicalRecordId: number,
  ) => void;
};

export function MedicalRecordsPanel({
  patientId,
  appointmentId,
  doctorId,
  onRecordReady,
}: MedicalRecordsPanelProps) {
  const queryClient = useQueryClient();

  const recordsQuery = useQuery({
    queryKey: [
      "medical-records",
      patientId,
    ],
    queryFn: () =>
      getMedicalRecordsByPatientId(
        patientId,
      ),
  });

  const records: MedicalRecord[] =
    recordsQuery.data?.data ?? [];

  const form = useForm<
    RecordFormInput,
    unknown,
    RecordFormValues
  >({
    resolver: zodResolver(
      recordSchema,
    ),
    defaultValues: {
      doctorId: doctorId ?? 0,
      encounterAt: new Date()
        .toISOString()
        .slice(0, 16),
      symptoms: "",
      clinicalNotes: "",
    },
  });

  const createMutation = useMutation({
    mutationFn: (
      values: RecordFormValues,
    ) => {
      const payload: CreateMedicalRecordInput =
        {
          patientId,
          doctorId: values.doctorId,
          appointmentId,
          encounterAt: new Date(
            values.encounterAt,
          ).toISOString(),
          symptoms:
            values.symptoms?.trim() ||
            undefined,
          clinicalNotes:
            values.clinicalNotes.trim(),
        };

      return createMedicalRecord(
        payload,
      );
    },

    onSuccess: async (response) => {
      await queryClient.invalidateQueries({
        queryKey: [
          "medical-records",
          patientId,
        ],
      });

      onRecordReady?.(
        response.data.id,
      );

      form.reset({
        doctorId: doctorId ?? 0,
        encounterAt: new Date()
          .toISOString()
          .slice(0, 16),
        symptoms: "",
        clinicalNotes: "",
      });
    },
  });

  if (recordsQuery.isLoading) {
    return (
      <div className="state-card clinical-state-card">
        <div className="state-card-icon">
          <FileText size={24} />
        </div>

        <h3>
          Loading medical records
        </h3>

        <p>
          Retrieving the patient's clinical
          history...
        </p>
      </div>
    );
  }

  if (recordsQuery.isError) {
    return (
      <div
        className="alert"
        role="alert"
      >
        Unable to load this patient's
        medical records.
      </div>
    );
  }

  return (
    <div className="clinical-panel-grid records-panel-grid">
      <article className="clinical-module-card clinical-module-create">
        <div className="clinical-module-header">
          <div>
            <span>
              Clinical documentation
            </span>

            <h3>
              Create medical record
            </h3>

            <p>
              Start a new encounter record for
              this patient.
            </p>
          </div>

          <div className="clinical-module-icon clinical-icon-cyan">
            <ClipboardPlus size={18} />
          </div>
        </div>

        <form
          className="card-grid"
          onSubmit={form.handleSubmit(
            (values) =>
              createMutation.mutate(
                values,
              ),
          )}
        >
          <div className="field">
            <label htmlFor="doctorId">
              Doctor ID
            </label>

            <input
              id="doctorId"
              type="number"
              min={1}
              {...form.register(
                "doctorId",
              )}
            />

            {form.formState.errors
              .doctorId && (
              <span className="helper-text">
                {
                  form.formState.errors
                    .doctorId.message
                }
              </span>
            )}
          </div>

          <div className="field">
            <label htmlFor="encounterAt">
              Encounter date and time
            </label>

            <input
              id="encounterAt"
              type="datetime-local"
              {...form.register(
                "encounterAt",
              )}
            />

            {form.formState.errors
              .encounterAt && (
              <span className="helper-text">
                {
                  form.formState.errors
                    .encounterAt.message
                }
              </span>
            )}
          </div>

          <RecordField
            label="Symptoms"
            registration={form.register(
              "symptoms",
            )}
            isTextArea
          />

          <div className="field field-wide">
            <label htmlFor="clinicalNotes">
              Clinical notes
            </label>

            <textarea
              id="clinicalNotes"
              {...form.register(
                "clinicalNotes",
              )}
              placeholder="Document the encounter, findings, assessment, and clinical plan."
            />

            {form.formState.errors
              .clinicalNotes && (
              <span className="helper-text">
                {
                  form.formState.errors
                    .clinicalNotes.message
                }
              </span>
            )}
          </div>

          {createMutation.isError && (
            <div
              className="alert field-wide"
              role="alert"
            >
              {getApiErrorMessage(
                createMutation.error,
              )}
            </div>
          )}

          <div className="field-wide">
            <button
              className="app-button primary"
              type="submit"
              disabled={
                createMutation.isPending
              }
            >
              {createMutation.isPending
                ? "Creating..."
                : "Create medical record"}
            </button>
          </div>
        </form>
      </article>

      <article className="clinical-module-card clinical-module-list">
        <div className="clinical-module-header">
          <div>
            <span>
              Patient history
            </span>

            <h3>
              Existing records
            </h3>

            <p>
              Select a record to continue into
              clinical workflows.
            </p>
          </div>

          <span className="clinical-count">
            {records.length}
          </span>
        </div>

        {records.length === 0 ? (
          <div className="state-card clinical-inner-state">
            <div className="state-card-icon">
              <FileText size={22} />
            </div>

            <h3>
              No medical records
            </h3>

            <p>
              This patient does not have a
              clinical encounter record yet.
            </p>
          </div>
        ) : (
          <div className="record-list">
            {records.map((record) => (
              <button
                className="record-list-item"
                type="button"
                key={record.id}
                onClick={() =>
                  onRecordReady?.(
                    record.id,
                  )
                }
              >
                <span className="record-list-main">
                  <strong>
                    Record #{record.id}
                  </strong>

                  <small>
                    {new Date(
                      record.encounterAt,
                    ).toLocaleString()}
                  </small>

                  {record.clinicalNotes && (
                    <small>
                      {record.clinicalNotes}
                    </small>
                  )}
                </span>

                <span className="record-list-meta">
                  <ShieldCheck
                    size={14}
                  />

                  {record.patientVisible
                    ? "Released"
                    : "Internal"}
                </span>
              </button>
            ))}
          </div>
        )}
      </article>
    </div>
  );
}

function RecordField({
  label,
  registration,
  isTextArea = false,
}: {
  label: string;
  registration: UseFormRegisterReturn;
  isTextArea?: boolean;
}) {
  return (
    <div className="field field-wide">
      <label>{label}</label>

      {isTextArea ? (
        <textarea
          {...registration}
          placeholder="Describe the patient's reported symptoms..."
        />
      ) : (
        <input
          type="text"
          {...registration}
        />
      )}
    </div>
  );
}

function getApiErrorMessage(
  error: unknown,
): string {
  if (axios.isAxiosError(error)) {
    const data =
      error.response?.data;

    if (data?.fieldErrors?.length) {
      return data.fieldErrors
        .map(
          (item: {
            field: string;
            message: string;
          }) =>
            `${item.field}: ${item.message}`,
        )
        .join(" | ");
    }

    if (
      typeof data?.message ===
      "string"
    ) {
      return data.message;
    }

    if (typeof data === "string") {
      return data;
    }

    return `Request failed with status ${
      error.response?.status ??
      "unknown"
    }.`;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "Unable to create the medical record.";
}