import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  BadgeCheck,
  ClipboardPlus,
  Stethoscope,
  Tag,
} from "lucide-react";

import {
  createDiagnosis,
  getDiagnoses,
} from "./api";

const diagnosisSchema = z.object({
  codeSystem: z.string().max(64),
  diagnosisCode: z.string().max(64),
  description: z
    .string()
    .trim()
    .min(1, "Description is required.")
    .max(512),
  diagnosisType: z.enum([
    "PRIMARY",
    "SECONDARY",
  ]),
  primary: z.boolean(),
});

type DiagnosisFormValues =
  z.infer<typeof diagnosisSchema>;

type DiagnosisPanelProps = {
  medicalRecordId: number;
};

export function DiagnosisPanel({
  medicalRecordId,
}: DiagnosisPanelProps) {
  const queryClient = useQueryClient();

  const diagnosesQuery = useQuery({
    queryKey: ["diagnoses", medicalRecordId],
    queryFn: () => getDiagnoses(medicalRecordId),
    enabled: medicalRecordId > 0,
  });

  const form = useForm<DiagnosisFormValues>({
    resolver: zodResolver(diagnosisSchema),

    defaultValues: {
      codeSystem: "",
      diagnosisCode: "",
      description: "",
      diagnosisType: "SECONDARY",
      primary: false,
    },
  });

  const createMutation = useMutation({
    mutationFn: (
      values: DiagnosisFormValues,
    ) =>
      createDiagnosis({
        medicalRecordId,
        codeSystem:
          values.codeSystem.trim() || undefined,
        diagnosisCode:
          values.diagnosisCode.trim() || undefined,
        description: values.description.trim(),
        diagnosisType: values.diagnosisType,
        primary: values.primary,
      }),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["diagnoses", medicalRecordId],
      });

      form.reset();
    },
  });

  const diagnoses =
    diagnosesQuery.data?.data ?? [];

  return (
    <section className="diagnosis-workspace">
      <div className="diagnosis-panel-grid">
        <article className="diagnosis-module-card diagnosis-create-card">
          <header className="diagnosis-module-header">
            <div className="diagnosis-module-title">
              <div className="diagnosis-module-icon diagnosis-icon-violet">
                <ClipboardPlus size={18} />
              </div>

              <div>
                <span>Clinical assessment</span>

                <h3>Add diagnosis</h3>

                <p>
                  Record a diagnosis and classify its
                  clinical importance for this medical
                  record.
                </p>
              </div>
            </div>

            <span className="diagnosis-record-badge">
              Record #{medicalRecordId}
            </span>
          </header>

          <form
            className="diagnosis-form"
            onSubmit={form.handleSubmit(
              (values) =>
                createMutation.mutate(values),
            )}
          >
            <div className="diagnosis-form-grid">
              <label className="field">
                <span>Code system</span>

                <div className="diagnosis-input-wrap">
                  <Tag size={14} />

                  <input
                    {...form.register(
                      "codeSystem",
                    )}
                    placeholder="ICD-10"
                    disabled={
                      createMutation.isPending
                    }
                  />
                </div>
              </label>

              <label className="field">
                <span>Diagnosis code</span>

                <div className="diagnosis-input-wrap">
                  <Stethoscope size={14} />

                  <input
                    {...form.register(
                      "diagnosisCode",
                    )}
                    placeholder="R51.9"
                    disabled={
                      createMutation.isPending
                    }
                  />
                </div>
              </label>

              <label className="field field-wide">
                <span>Description</span>

                <input
                  {...form.register(
                    "description",
                  )}
                  placeholder="Clinical diagnosis description"
                  disabled={
                    createMutation.isPending
                  }
                />

                {form.formState.errors.description && (
                  <span className="helper-text diagnosis-error">
                    {
                      form.formState.errors
                        .description.message
                    }
                  </span>
                )}
              </label>

              <label className="field">
                <span>Diagnosis type</span>

                <select
                  {...form.register(
                    "diagnosisType",
                  )}
                  disabled={
                    createMutation.isPending
                  }
                >
                  <option value="PRIMARY">
                    Primary
                  </option>

                  <option value="SECONDARY">
                    Secondary
                  </option>
                </select>
              </label>

              <label className="diagnosis-checkbox-card">
                <input
                  type="checkbox"
                  {...form.register("primary")}
                  disabled={
                    createMutation.isPending
                  }
                />

                <span>
                  <strong>
                    Primary diagnosis
                  </strong>

                  <small>
                    Mark this as the principal
                    diagnosis for the record.
                  </small>
                </span>
              </label>
            </div>

            {createMutation.isError && (
              <div
                className="alert diagnosis-error-alert"
                role="alert"
              >
                Could not create diagnosis. A medical
                record can have only one primary
                diagnosis, and primary diagnoses must
                use type PRIMARY.
              </div>
            )}

            <footer className="diagnosis-form-footer">
              <div className="diagnosis-validation-note">
                <BadgeCheck size={14} />

                <span>
                  Diagnosis information is linked
                  directly to the selected medical
                  record.
                </span>
              </div>

              <button
                className="app-button"
                type="submit"
                disabled={
                  createMutation.isPending
                }
              >
                <ClipboardPlus size={15} />

                {createMutation.isPending
                  ? "Saving..."
                  : "Add diagnosis"}
              </button>
            </footer>
          </form>
        </article>

        <article className="diagnosis-module-card diagnosis-list-card">
          <header className="diagnosis-module-header">
            <div className="diagnosis-module-title">
              <div className="diagnosis-module-icon diagnosis-icon-cyan">
                <Stethoscope size={18} />
              </div>

              <div>
                <span>Clinical record</span>

                <h3>Diagnoses</h3>

                <p>
                  Current diagnoses associated with this
                  medical record.
                </p>
              </div>
            </div>

            <span className="diagnosis-count-badge">
              {diagnoses.length}
              {diagnoses.length === 1
                ? " diagnosis"
                : " diagnoses"}
            </span>
          </header>

          {diagnosesQuery.isLoading && (
            <div className="diagnosis-state">
              <div className="diagnosis-state-icon">
                <Stethoscope size={17} />
              </div>

              <strong>
                Loading diagnoses
              </strong>

              <span>
                Retrieving the clinical assessment...
              </span>
            </div>
          )}

          {diagnosesQuery.isError && (
            <div
              className="alert"
              role="alert"
            >
              Unable to load diagnoses.
            </div>
          )}

          {!diagnosesQuery.isLoading &&
            !diagnosesQuery.isError &&
            diagnoses.length === 0 && (
              <div className="diagnosis-state">
                <div className="diagnosis-state-icon">
                  <ClipboardPlus size={17} />
                </div>

                <strong>
                  No diagnoses recorded
                </strong>

                <span>
                  Add the first diagnosis using the
                  clinical assessment form.
                </span>
              </div>
            )}

          {diagnoses.length > 0 && (
            <div className="diagnosis-list">
              {diagnoses.map((diagnosis) => {
                const isPrimary =
                  diagnosis.primary;

                return (
                  <article
                    className={`diagnosis-card ${
                      isPrimary
                        ? "is-primary"
                        : ""
                    }`}
                    key={diagnosis.id}
                  >
                    <div className="diagnosis-card-leading">
                      <div className="diagnosis-card-icon">
                        <Stethoscope size={16} />
                      </div>

                      <div className="diagnosis-card-main">
                        <div className="diagnosis-card-heading">
                          <strong>
                            {diagnosis.description}
                          </strong>

                          <span
                            className={`diagnosis-status ${
                              isPrimary
                                ? "primary"
                                : "secondary"
                            }`}
                          >
                            {isPrimary
                              ? "Primary"
                              : "Secondary"}
                          </span>
                        </div>

                        <div className="diagnosis-card-meta">
                          {diagnosis.codeSystem &&
                            diagnosis.diagnosisCode && (
                              <span>
                                {diagnosis.codeSystem}{" "}
                                {
                                  diagnosis.diagnosisCode
                                }
                              </span>
                            )}

                          <span>
                            Record #{medicalRecordId}
                          </span>
                        </div>
                      </div>
                    </div>

                    {isPrimary && (
                      <BadgeCheck
                        className="diagnosis-primary-icon"
                        size={17}
                      />
                    )}
                  </article>
                );
              })}
            </div>
          )}
        </article>
      </div>
    </section>
  );
}