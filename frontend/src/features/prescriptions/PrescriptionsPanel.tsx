import { useState } from "react";
import {
  Pill,
  Plus,
  Trash2,
} from "lucide-react";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import axios from "axios";

import {
  cancelPrescription,
  createPrescription,
  dispensePrescription,
  getPatientPrescriptions,
  type CreatePrescriptionInput,
} from "./api";

import { getMedicines } from "../medicines/api";

type PrescriptionsPanelProps = {
  patientId: number;
  doctorId?: number;
  appointmentId?: number;
  medicalRecordId?: number;
};

type ItemDraft = {
  medicineId: string;
  dosage: string;
  route: string;
  frequency: string;
  durationDays: string;
  quantityPrescribed: string;
  instructions: string;
};

const emptyItem: ItemDraft = {
  medicineId: "",
  dosage: "",
  route: "",
  frequency: "",
  durationDays: "",
  quantityPrescribed: "",
  instructions: "",
};

export function PrescriptionsPanel({
  patientId,
  doctorId,
  appointmentId,
  medicalRecordId,
}: PrescriptionsPanelProps) {
  const queryClient =
    useQueryClient();

  const [items, setItems] =
    useState<ItemDraft[]>([
      { ...emptyItem },
    ]);

  const [notes, setNotes] =
    useState("");

  const [expiresAt, setExpiresAt] =
    useState("");

  const [selectedId, setSelectedId] =
    useState<number | null>(
      null,
    );

  const prescriptionsQuery =
    useQuery({
      queryKey: [
        "prescriptions",
        patientId,
      ],

      queryFn: () =>
        getPatientPrescriptions(
          patientId,
        ),

      enabled: patientId > 0,
    });

  const medicinesQuery =
    useQuery({
      queryKey: [
        "medicines",
        "all",
      ],

      queryFn: () =>
        getMedicines(0, 100),
    });

  const prescriptions =
    prescriptionsQuery.data
      ?.data ?? [];

  const medicines =
    medicinesQuery.data
      ?.data ?? [];

  const selected =
    prescriptions.find(
      (prescription) =>
        prescription.id ===
        selectedId,
    );

  const createMutation =
    useMutation({
      mutationFn: () => {
        if (!doctorId) {
          throw new Error(
            "A doctor is required before creating a prescription.",
          );
        }

        const parsedItems = items
          .filter(
            (item) =>
              item.medicineId &&
              item.dosage.trim() &&
              item.route.trim() &&
              item.frequency.trim(),
          )
          .map((item) => ({
            medicineId:
              Number(
                item.medicineId,
              ),

            dosage:
              item.dosage.trim(),

            route:
              item.route.trim(),

            frequency:
              item.frequency.trim(),

            durationDays:
              Number(
                item.durationDays,
              ),

            quantityPrescribed:
              Number(
                item.quantityPrescribed,
              ),

            instructions:
              item.instructions
                .trim() ||
              undefined,
          }));

        if (
          parsedItems.length === 0 ||
          parsedItems.some(
            (item) =>
              !Number.isInteger(
                item.medicineId,
              ) ||
              item.medicineId <= 0 ||
              !Number.isInteger(
                item.durationDays,
              ) ||
              item.durationDays <= 0 ||
              !Number.isFinite(
                item.quantityPrescribed,
              ) ||
              item.quantityPrescribed <=
                0,
          )
        ) {
          throw new Error(
            "Add at least one complete item with a valid medicine, duration, and quantity.",
          );
        }

        const payload: CreatePrescriptionInput =
          {
            patientId,
            doctorId,
            appointmentId,
            medicalRecordId,

            expiresAt: expiresAt
              ? new Date(
                  expiresAt,
                ).toISOString()
              : undefined,

            notes:
              notes.trim() ||
              undefined,

            items: parsedItems,
          };

        return createPrescription(
          payload,
        );
      },

      onSuccess: async (
        response,
      ) => {
        await queryClient.invalidateQueries(
          {
            queryKey: [
              "prescriptions",
              patientId,
            ],
          },
        );

        setItems([
          { ...emptyItem },
        ]);

        setNotes("");
        setExpiresAt("");

        if (
          response.data?.id
        ) {
          setSelectedId(
            response.data.id,
          );
        }
      },
    });

  const cancelMutation =
    useMutation({
      mutationFn: ({
        id,
        reason,
      }: {
        id: number;
        reason: string;
      }) =>
        cancelPrescription(
          id,
          {
            cancellationReason:
              reason,
          },
        ),

      onSuccess:
        async () => {
          await queryClient.invalidateQueries(
            {
              queryKey: [
                "prescriptions",
                patientId,
              ],
            },
          );
        },
    });

  const dispenseMutation =
    useMutation({
      mutationFn: (id: number) =>
        dispensePrescription(
          id,
        ),

      onSuccess:
        async () => {
          await queryClient.invalidateQueries(
            {
              queryKey: [
                "prescriptions",
                patientId,
              ],
            },
          );
        },
    });

  function updateItem(
    index: number,
    field: keyof ItemDraft,
    value: string,
  ) {
    setItems((current) =>
      current.map(
        (item, itemIndex) =>
          itemIndex === index
            ? {
                ...item,
                [field]: value,
              }
            : item,
      ),
    );
  }

  function removeItem(
    index: number,
  ) {
    setItems((current) =>
      current.length === 1
        ? current
        : current.filter(
            (_, itemIndex) =>
              itemIndex !==
              index,
          ),
    );
  }

  function addItem() {
    setItems((current) => [
      ...current,
      { ...emptyItem },
    ]);
  }

  function medicineLabel(
    medicineId: number,
  ) {
    const medicine =
      medicines.find(
        (entry) =>
          entry.id ===
          medicineId,
      );

    return medicine
      ? `${medicine.name} (${medicine.medicineCode})`
      : `Medicine #${medicineId}`;
  }

  return (
    <div className="clinical-panel-grid prescriptions-panel-grid">
      <article className="clinical-module-card clinical-prescription-create">
        <div className="clinical-module-header">
          <div>
            <span>
              Medication therapy
            </span>

            <h3>
              Create prescription
            </h3>

            <p>
              Build a prescription with
              dosage, route, frequency,
              duration, and instructions.
            </p>
          </div>

          <div className="clinical-module-icon clinical-icon-blue">
            <Pill size={18} />
          </div>
        </div>

        {!doctorId && (
          <div
            className="alert"
            role="alert"
          >
            A doctor is required before
            creating a prescription.
          </div>
        )}

        {medicinesQuery.isError && (
          <div
            className="alert"
            role="alert"
          >
            Unable to load medicines
            for the prescription form.
          </div>
        )}

        <form
          className="card-grid"
          onSubmit={(event) => {
            event.preventDefault();
            createMutation.mutate();
          }}
        >
          {items.map(
            (item, index) => (
              <div
                className="clinical-item-block field-wide"
                key={`prescription-item-${index}`}
              >
                <label className="field">
                  <span>
                    Medicine
                  </span>

                  <select
                    value={
                      item.medicineId
                    }
                    onChange={(
                      event,
                    ) =>
                      updateItem(
                        index,
                        "medicineId",
                        event.target
                          .value,
                      )
                    }
                    disabled={
                      !doctorId ||
                      createMutation.isPending
                    }
                    required
                  >
                    <option value="">
                      Select a medicine
                    </option>

                    {medicines.map(
                      (medicine) => (
                        <option
                          key={
                            medicine.id
                          }
                          value={
                            medicine.id
                          }
                        >
                          {medicine.name}{" "}
                          (
                          {
                            medicine.medicineCode
                          }
                          )
                        </option>
                      ),
                    )}
                  </select>
                </label>

                <label className="field">
                  <span>
                    Dosage
                  </span>

                  <input
                    value={
                      item.dosage
                    }
                    onChange={(
                      event,
                    ) =>
                      updateItem(
                        index,
                        "dosage",
                        event.target
                          .value,
                      )
                    }
                    placeholder="500 mg"
                    disabled={
                      !doctorId ||
                      createMutation.isPending
                    }
                    required
                  />
                </label>

                <label className="field">
                  <span>
                    Route
                  </span>

                  <input
                    value={
                      item.route
                    }
                    onChange={(
                      event,
                    ) =>
                      updateItem(
                        index,
                        "route",
                        event.target
                          .value,
                      )
                    }
                    placeholder="Oral"
                    disabled={
                      !doctorId ||
                      createMutation.isPending
                    }
                    required
                  />
                </label>

                <label className="field">
                  <span>
                    Frequency
                  </span>

                  <input
                    value={
                      item.frequency
                    }
                    onChange={(
                      event,
                    ) =>
                      updateItem(
                        index,
                        "frequency",
                        event.target
                          .value,
                      )
                    }
                    placeholder="Twice daily"
                    disabled={
                      !doctorId ||
                      createMutation.isPending
                    }
                    required
                  />
                </label>

                <label className="field">
                  <span>
                    Duration (days)
                  </span>

                  <input
                    type="number"
                    min={1}
                    step={1}
                    value={
                      item.durationDays
                    }
                    onChange={(
                      event,
                    ) =>
                      updateItem(
                        index,
                        "durationDays",
                        event.target
                          .value,
                      )
                    }
                    disabled={
                      !doctorId ||
                      createMutation.isPending
                    }
                    required
                  />
                </label>

                <label className="field">
                  <span>
                    Quantity
                  </span>

                  <input
                    type="number"
                    min={0.0001}
                    step="any"
                    value={
                      item.quantityPrescribed
                    }
                    onChange={(
                      event,
                    ) =>
                      updateItem(
                        index,
                        "quantityPrescribed",
                        event.target
                          .value,
                      )
                    }
                    disabled={
                      !doctorId ||
                      createMutation.isPending
                    }
                    required
                  />
                </label>

                <label className="field field-wide">
                  <span>
                    Instructions
                  </span>

                  <input
                    value={
                      item.instructions
                    }
                    onChange={(
                      event,
                    ) =>
                      updateItem(
                        index,
                        "instructions",
                        event.target
                          .value,
                      )
                    }
                    placeholder="Take with food"
                    disabled={
                      !doctorId ||
                      createMutation.isPending
                    }
                  />
                </label>

                <div className="clinical-item-actions">
                  <button
                    className="app-button app-button-danger"
                    type="button"
                    onClick={() =>
                      removeItem(
                        index,
                      )
                    }
                    disabled={
                      items.length ===
                        1 ||
                      createMutation.isPending
                    }
                  >
                    <Trash2
                      size={14}
                    />

                    Remove item
                  </button>
                </div>
              </div>
            ),
          )}

          <div className="field-wide clinical-form-actions">
            <button
              className="app-button"
              type="button"
              onClick={addItem}
              disabled={
                !doctorId ||
                createMutation.isPending
              }
            >
              <Plus size={15} />
              Add item
            </button>
          </div>

          <label className="field">
            <span>
              Expires at
            </span>

            <input
              type="datetime-local"
              value={expiresAt}
              onChange={(event) =>
                setExpiresAt(
                  event.target.value,
                )
              }
              disabled={
                !doctorId ||
                createMutation.isPending
              }
            />
          </label>

          <label className="field field-wide">
            <span>
              Notes
            </span>

            <textarea
              value={notes}
              onChange={(event) =>
                setNotes(
                  event.target.value,
                )
              }
              maxLength={2048}
              disabled={
                !doctorId ||
                createMutation.isPending
              }
              placeholder="Additional instructions or clinical notes..."
            />
          </label>

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
                !doctorId ||
                createMutation.isPending
              }
            >
              {createMutation.isPending
                ? "Creating..."
                : "Create prescription"}
            </button>
          </div>
        </form>
      </article>

      <article className="clinical-module-card clinical-prescription-history">
        <div className="clinical-module-header">
          <div>
            <span>
              Medication history
            </span>

            <h3>
              Prescriptions
            </h3>

            <p>
              Review, dispense, or cancel
              active prescriptions.
            </p>
          </div>

          <span className="clinical-count">
            {prescriptions.length}
          </span>
        </div>

        {prescriptionsQuery.isLoading && (
          <div className="state-card clinical-inner-state">
            <div className="state-card-icon">
              <Pill size={22} />
            </div>

            <h3>
              Loading prescriptions
            </h3>

            <p>
              Retrieving medication
              history...
            </p>
          </div>
        )}

        {prescriptionsQuery.isError && (
          <div
            className="alert"
            role="alert"
          >
            Unable to load
            prescriptions.
          </div>
        )}

        {!prescriptionsQuery.isLoading &&
          !prescriptionsQuery.isError &&
          prescriptions.length ===
            0 && (
            <div className="state-card clinical-inner-state">
              <div className="state-card-icon">
                <Pill size={22} />
              </div>

              <h3>
                No prescriptions
              </h3>

              <p>
                No medications have been
                prescribed for this patient.
              </p>
            </div>
          )}

        {prescriptions.length >
          0 && (
          <div className="record-list">
            {prescriptions.map(
              (prescription) => {
                const isSelected =
                  selected?.id ===
                  prescription.id;

                return (
                  <button
                    key={
                      prescription.id
                    }
                    className={`record-list-item ${
                      isSelected
                        ? "is-selected"
                        : ""
                    }`}
                    type="button"
                    onClick={() =>
                      setSelectedId(
                        prescription.id,
                      )
                    }
                    aria-pressed={
                      isSelected
                    }
                  >
                    <span className="record-list-main">
                      <strong>
                        Prescription #
                        {
                          prescription.id
                        }
                      </strong>

                      <small>
                        {
                          prescription.status
                        }
                      </small>

                      <small>
                        {
                          prescription
                            .items.length
                        }{" "}
                        item(s)
                      </small>
                    </span>

                    <span className="record-list-meta">
                      {
                        prescription.status
                      }
                    </span>
                  </button>
                );
              },
            )}
          </div>
        )}

        {prescriptions.length >
          0 &&
          !selected && (
            <div className="state-card clinical-inner-state">
              <div className="state-card-icon">
                <Pill size={22} />
              </div>

              <h3>
                Select a prescription
              </h3>

              <p>
                Choose a prescription to
                inspect its items and
                available actions.
              </p>
            </div>
          )}

        {selected && (
          <div className="clinical-selected-detail">
            <div className="clinical-detail-summary">
              <div>
                <span>
                  Status
                </span>

                <strong>
                  <span className="app-user-pill">
                    {selected.status}
                  </span>
                </strong>
              </div>

              {selected.notes && (
                <div>
                  <span>
                    Notes
                  </span>

                  <strong>
                    {selected.notes}
                  </strong>
                </div>
              )}

              {selected.cancellationReason && (
                <div>
                  <span>
                    Cancellation reason
                  </span>

                  <strong>
                    {
                      selected.cancellationReason
                    }
                  </strong>
                </div>
              )}
            </div>

            <div className="record-list">
              {selected.items.map(
                (item) => (
                  <div
                    className="record-list-item clinical-static-item"
                    key={item.id}
                  >
                    <span className="record-list-main">
                      <strong>
                        {medicineLabel(
                          item.medicineId,
                        )}
                      </strong>

                      <small>
                        {item.dosage}
                        {" · "}
                        {item.route}
                        {" · "}
                        {item.frequency}
                        {" · "}
                        {item.durationDays}d
                      </small>

                      <small>
                        Quantity:{" "}
                        {
                          item.quantityPrescribed
                        }
                      </small>

                      {item.instructions && (
                        <small>
                          {
                            item.instructions
                          }
                        </small>
                      )}
                    </span>
                  </div>
                ),
              )}
            </div>

            {cancelMutation.isError && (
              <div
                className="alert"
                role="alert"
              >
                {getApiErrorMessage(
                  cancelMutation.error,
                )}
              </div>
            )}

            {dispenseMutation.isError && (
              <div
                className="alert"
                role="alert"
              >
                {getApiErrorMessage(
                  dispenseMutation.error,
                )}
              </div>
            )}

            {selected.status ===
              "ACTIVE" && (
              <div className="clinical-form-actions clinical-detail-actions">
                <button
                  className="app-button"
                  type="button"
                  disabled={
                    dispenseMutation.isPending
                  }
                  onClick={() =>
                    dispenseMutation.mutate(
                      selected.id,
                    )
                  }
                >
                  {dispenseMutation.isPending
                    ? "Dispensing..."
                    : "Dispense"}
                </button>

                <button
                  className="app-button app-button-danger"
                  type="button"
                  disabled={
                    cancelMutation.isPending
                  }
                  onClick={() => {
                    const reason =
                      window.prompt(
                        "Cancellation reason:",
                      );

                    if (
                      reason?.trim()
                    ) {
                      cancelMutation.mutate(
                        {
                          id: selected.id,
                          reason:
                            reason.trim(),
                        },
                      );
                    }
                  }}
                >
                  {cancelMutation.isPending
                    ? "Cancelling..."
                    : "Cancel prescription"}
                </button>
              </div>
            )}
          </div>
        )}
      </article>
    </div>
  );
}

function getApiErrorMessage(
  error: unknown,
): string {
  if (axios.isAxiosError(error)) {
    const data =
      error.response?.data as {
        message?: unknown;
      } | undefined;

    if (
      typeof data?.message ===
      "string"
    ) {
      return data.message;
    }

    return `Request failed with status ${
      error.response?.status ??
      "unknown"
    }.`;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "An unexpected error occurred.";
}