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
  Activity,
  HeartPulse,
} from "lucide-react";
import { z } from "zod";
import axios from "axios";

import {
  createVitalSign,
  getVitals,
  type CreateVitalSignInput,
  type VitalSign,
} from "./api";

const optionalNumber = z.preprocess(
  (value) => {
    if (
      value === "" ||
      value === null ||
      value === undefined
    ) {
      return undefined;
    }

    const number = Number(value);

    return Number.isFinite(number)
      ? number
      : value;
  },
  z.number().optional(),
);

const vitalsSchema = z
  .object({
    recordedAt: z.string().min(
      1,
      "Date and time are required.",
    ),

    temperatureCelsius:
      optionalNumber.refine(
        (value) =>
          value === undefined ||
          (value >= 25 && value <= 50),
        "Temperature must be between 25 and 50°C.",
      ),

    systolicBp:
      optionalNumber.refine(
        (value) =>
          value === undefined ||
          (value >= 40 && value <= 300),
        "Systolic BP must be between 40 and 300.",
      ),

    diastolicBp:
      optionalNumber.refine(
        (value) =>
          value === undefined ||
          (value >= 20 && value <= 200),
        "Diastolic BP must be between 20 and 200.",
      ),

    heartRate:
      optionalNumber.refine(
        (value) =>
          value === undefined ||
          (value >= 20 && value <= 300),
        "Heart rate must be between 20 and 300.",
      ),

    respiratoryRate:
      optionalNumber.refine(
        (value) =>
          value === undefined ||
          (value >= 4 && value <= 80),
        "Respiratory rate must be between 4 and 80.",
      ),

    oxygenSaturation:
      optionalNumber.refine(
        (value) =>
          value === undefined ||
          (value >= 0 && value <= 100),
        "Oxygen saturation must be between 0 and 100.",
      ),

    weightKg:
      optionalNumber.refine(
        (value) =>
          value === undefined ||
          value >= 0.01,
        "Weight must be greater than zero.",
      ),

    heightCm:
      optionalNumber.refine(
        (value) =>
          value === undefined ||
          value >= 0.01,
        "Height must be greater than zero.",
      ),
  })
  .refine(
    (values) =>
      (values.systolicBp ===
        undefined &&
        values.diastolicBp ===
          undefined) ||
      (values.systolicBp !==
        undefined &&
        values.diastolicBp !==
          undefined),
    {
      message:
        "Systolic and diastolic BP must be provided together.",
      path: ["systolicBp"],
    },
  )
  .refine(
    (values) =>
      values.systolicBp ===
        undefined ||
      values.diastolicBp ===
        undefined ||
      values.systolicBp >
        values.diastolicBp,
    {
      message:
        "Systolic BP must be greater than diastolic BP.",
      path: ["systolicBp"],
    },
  );

type VitalsFormInput =
  z.input<typeof vitalsSchema>;

type VitalsFormValues =
  z.output<typeof vitalsSchema>;

export function VitalsPanel({
  medicalRecordId,
}: {
  medicalRecordId: number;
}) {
  const queryClient =
    useQueryClient();

  const vitalsQuery = useQuery({
    queryKey: [
      "vitals",
      medicalRecordId,
    ],
    queryFn: () =>
      getVitals(medicalRecordId),
    enabled:
      Boolean(medicalRecordId),
  });

  const form = useForm<
    VitalsFormInput,
    unknown,
    VitalsFormValues
  >({
    resolver:
      zodResolver(vitalsSchema),
    defaultValues: {
      recordedAt: new Date()
        .toISOString()
        .slice(0, 16),
    },
  });

  const mutation = useMutation({
    mutationFn: (
      values: VitalsFormValues,
    ) => {
      const input: CreateVitalSignInput =
        {
          medicalRecordId,
          recordedAt: new Date(
            values.recordedAt,
          ).toISOString(),
          temperatureCelsius:
            values.temperatureCelsius,
          systolicBp:
            values.systolicBp,
          diastolicBp:
            values.diastolicBp,
          heartRate:
            values.heartRate,
          respiratoryRate:
            values.respiratoryRate,
          oxygenSaturation:
            values.oxygenSaturation,
          weightKg:
            values.weightKg,
          heightCm:
            values.heightCm,
        };

      return createVitalSign(input);
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries(
        {
          queryKey: [
            "vitals",
            medicalRecordId,
          ],
        },
      );

      form.reset({
        recordedAt: new Date()
          .toISOString()
          .slice(0, 16),
      });
    },
  });

  const vitals: VitalSign[] =
    vitalsQuery.data?.data ?? [];

  const errors =
    form.formState.errors;

  return (
    <div className="clinical-panel-grid vitals-panel-grid">
      <article className="clinical-module-card clinical-vitals-form">
        <div className="clinical-module-header">
          <div>
            <span>
              Clinical measurements
            </span>

            <h3>
              Record vital signs
            </h3>

            <p>
              Capture the patient's current
              physiological observations.
            </p>
          </div>

          <div className="clinical-module-icon clinical-icon-cyan">
            <HeartPulse size={18} />
          </div>
        </div>

        <form
          className="card-grid"
          onSubmit={form.handleSubmit(
            (values) =>
              mutation.mutate(values),
          )}
        >
          <div className="field field-wide">
            <label htmlFor="recordedAt">
              Recorded at
            </label>

            <input
              id="recordedAt"
              type="datetime-local"
              {...form.register(
                "recordedAt",
              )}
            />

            {errors.recordedAt?.message && (
              <span className="helper-text">
                {
                  errors.recordedAt
                    .message
                }
              </span>
            )}
          </div>

          <VitalField
            label="Temperature °C"
            registration={form.register(
              "temperatureCelsius",
            )}
            error={
              errors.temperatureCelsius
                ?.message
            }
          />

          <VitalField
            label="Systolic BP"
            registration={form.register(
              "systolicBp",
            )}
            error={
              errors.systolicBp
                ?.message
            }
          />

          <VitalField
            label="Diastolic BP"
            registration={form.register(
              "diastolicBp",
            )}
            error={
              errors.diastolicBp
                ?.message
            }
          />

          <VitalField
            label="Heart rate"
            registration={form.register(
              "heartRate",
            )}
            error={
              errors.heartRate
                ?.message
            }
          />

          <VitalField
            label="Respiratory rate"
            registration={form.register(
              "respiratoryRate",
            )}
            error={
              errors.respiratoryRate
                ?.message
            }
          />

          <VitalField
            label="Oxygen saturation %"
            registration={form.register(
              "oxygenSaturation",
            )}
            error={
              errors.oxygenSaturation
                ?.message
            }
          />

          <VitalField
            label="Weight kg"
            registration={form.register(
              "weightKg",
            )}
            error={
              errors.weightKg?.message
            }
          />

          <VitalField
            label="Height cm"
            registration={form.register(
              "heightCm",
            )}
            error={
              errors.heightCm?.message
            }
          />

          {mutation.isError && (
            <div
              className="alert field-wide"
              role="alert"
            >
              {getApiErrorMessage(
                mutation.error,
              )}
            </div>
          )}

          <div className="field-wide">
            <button
              className="app-button primary"
              type="submit"
              disabled={
                mutation.isPending
              }
            >
              <Activity size={15} />

              {mutation.isPending
                ? "Recording..."
                : "Record vitals"}
            </button>
          </div>
        </form>
      </article>

      <article className="clinical-module-card clinical-vitals-history">
        <div className="clinical-module-header">
          <div>
            <span>
              Patient history
            </span>

            <h3>
              Recent readings
            </h3>

            <p>
              Latest measurements recorded
              for this medical record.
            </p>
          </div>

          <span className="clinical-count">
            {vitals.length}
          </span>
        </div>

        {vitalsQuery.isLoading && (
          <div className="state-card clinical-inner-state">
            <div className="state-card-icon">
              <HeartPulse size={22} />
            </div>

            <h3>Loading readings</h3>

            <p>
              Retrieving vital-sign history...
            </p>
          </div>
        )}

        {vitalsQuery.isError && (
          <div
            className="alert"
            role="alert"
          >
            Unable to load vital signs.
          </div>
        )}

        {!vitalsQuery.isLoading &&
          !vitalsQuery.isError &&
          vitals.length === 0 && (
            <div className="state-card clinical-inner-state">
              <div className="state-card-icon">
                <HeartPulse size={22} />
              </div>

              <h3>
                No vital signs recorded
              </h3>

              <p>
                Record the first measurement
                for this medical record.
              </p>
            </div>
          )}

        {vitals.length > 0 && (
          <div className="table-wrap">
            <table className="clinical-data-table clinical-vitals-table">
              <thead>
                <tr>
                  <th>Recorded</th>
                  <th>Temp.</th>
                  <th>Blood pressure</th>
                  <th>Heart rate</th>
                  <th>Respiratory</th>
                  <th>SpO₂</th>
                </tr>
              </thead>

              <tbody>
                {vitals.map(
                  (vital) => (
                    <tr
                      key={vital.id}
                    >
                      <td>
                        {new Date(
                          vital.recordedAt,
                        ).toLocaleString()}
                      </td>

                      <td>
                        {vital.temperatureCelsius ??
                          "—"}
                      </td>

                      <td>
                        {vital.systolicBp !==
                          null &&
                        vital.systolicBp !==
                          undefined &&
                        vital.diastolicBp !==
                          null &&
                        vital.diastolicBp !==
                          undefined
                          ? `${vital.systolicBp}/${vital.diastolicBp}`
                          : "—"}
                      </td>

                      <td>
                        {vital.heartRate ??
                          "—"}
                      </td>

                      <td>
                        {vital.respiratoryRate ??
                          "—"}
                      </td>

                      <td>
                        {vital.oxygenSaturation !==
                          null &&
                        vital.oxygenSaturation !==
                          undefined
                          ? `${vital.oxygenSaturation}%`
                          : "—"}
                      </td>
                    </tr>
                  ),
                )}
              </tbody>
            </table>
          </div>
        )}
      </article>
    </div>
  );
}

function VitalField({
  label,
  registration,
  error,
}: {
  label: string;
  registration: UseFormRegisterReturn;
  error?: string;
}) {
  return (
    <div className="field">
      <label>{label}</label>

      <input
        type="number"
        step="0.1"
        {...registration}
      />

      {error && (
        <span className="helper-text">
          {error}
        </span>
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

  return "Unable to record vital signs.";
}