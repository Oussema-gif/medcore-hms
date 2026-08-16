import { useMemo, useState } from "react";
import {
  CheckCircle2,
  FlaskConical,
  Plus,
  RefreshCw,
  Search,
  ShieldCheck,
  X,
} from "lucide-react";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import axios from "axios";

import {
  createMedicine,
  deleteMedicine,
  getMedicines,
  type CreateMedicineInput,
} from "./api";

const PAGE_SIZE = 20;

const emptyForm: CreateMedicineInput = {
  medicineCode: "",
  name: "",
  genericName: "",
  dosageForm: "",
  strength: "",
  unit: "",
  reorderLevel: 0,
};

export function MedicinesPage() {
  const queryClient = useQueryClient();

  const [page, setPage] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [search, setSearch] = useState("");

  const [form, setForm] =
    useState<CreateMedicineInput>({
      ...emptyForm,
    });

  const medicinesQuery = useQuery({
    queryKey: ["medicines", page],
    queryFn: () => getMedicines(page, PAGE_SIZE),
  });

  const createMutation = useMutation({
    mutationFn: () =>
      createMedicine({
        medicineCode:
          form.medicineCode.trim(),

        name:
          form.name.trim(),

        genericName:
          form.genericName?.trim() ||
          undefined,

        dosageForm:
          form.dosageForm.trim(),

        strength:
          form.strength?.trim() ||
          undefined,

        unit:
          form.unit.trim(),

        reorderLevel:
          form.reorderLevel ?? 0,
      }),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["medicines"],
      });

      setForm({
        ...emptyForm,
      });

      setSearch("");

      setShowForm(false);
    },
  });

  const deactivateMutation =
    useMutation({
      mutationFn: (
        medicineId: number,
      ) => deleteMedicine(medicineId),

      onSuccess: async () => {
        await queryClient.invalidateQueries({
          queryKey: ["medicines"],
        });
      },
    });

  const response =
    medicinesQuery.data;

  const medicines =
    response?.data ?? [];

  const normalizedSearch =
    search.trim().toLowerCase();

  const filteredMedicines =
    useMemo(() => {
      if (!normalizedSearch) {
        return medicines;
      }

      return medicines.filter(
        (medicine) =>
          [
            medicine.medicineCode,
            medicine.name,
            medicine.genericName ?? "",
            medicine.dosageForm,
            medicine.strength ?? "",
            medicine.unit,
            String(
              medicine.reorderLevel,
            ),
            medicine.active
              ? "active"
              : "inactive",
          ]
            .join(" ")
            .toLowerCase()
            .includes(normalizedSearch),
      );
    }, [medicines, normalizedSearch]);

  const activeCount =
    medicines.filter(
      (medicine) => medicine.active,
    ).length;

  return (
    <section className="medicine-directory">
      <header className="medicine-command-header">
        <div className="medicine-heading">
          <span className="eyebrow">
            Pharmacy catalog
          </span>

          <h2>Medicines</h2>

          <p>
            Manage the medicine catalog used
            across prescriptions, pharmacy
            operations, and inventory.
          </p>
        </div>

        <div className="medicine-header-actions">
          <button
            className="app-button"
            type="button"
            onClick={() =>
              medicinesQuery.refetch()
            }
            disabled={
              medicinesQuery.isFetching
            }
          >
            <RefreshCw
              size={15}
              className={
                medicinesQuery.isFetching
                  ? "spin"
                  : undefined
              }
            />

            Refresh
          </button>

          <button
            className="app-button medicine-add-button"
            type="button"
            onClick={() =>
              setShowForm(
                (current) => !current,
              )
            }
          >
            {showForm ? (
              <X size={16} />
            ) : (
              <Plus size={16} />
            )}

            {showForm
              ? "Close form"
              : "Add medicine"}
          </button>
        </div>
      </header>

      <section className="medicine-toolbar">
        <label
          className="medicine-search"
          htmlFor="medicine-search"
        >
          <Search
            size={16}
            aria-hidden="true"
          />

          <input
            id="medicine-search"
            type="search"
            value={search}
            onChange={(event) =>
              setSearch(
                event.target.value,
              )
            }
            placeholder="Search by name, generic name, code, strength, dosage form or unit"
            autoComplete="off"
          />

          {search && (
            <button
              className="medicine-search-clear"
              type="button"
              aria-label="Clear medicine search"
              onClick={() =>
                setSearch("")
              }
            >
              <X size={14} />
            </button>
          )}
        </label>

        <div className="medicine-toolbar-stat">
          <span>Catalog</span>
          <strong>
            {response?.totalElements ?? 0}
          </strong>
        </div>

        <div className="medicine-toolbar-stat">
          <span>Active</span>
          <strong>
            {activeCount}
          </strong>
        </div>

        {search && (
          <div className="medicine-search-result">
            {filteredMedicines.length} matching
          </div>
        )}
      </section>

      {showForm && (
        <section className="medicine-create-workspace">
          <div className="medicine-create-header">
            <div className="medicine-create-title">
              <div className="medicine-create-icon">
                <FlaskConical size={19} />
              </div>

              <div>
                <span>
                  Pharmacy catalog
                </span>

                <h3>
                  Add medicine
                </h3>

                <p>
                  Create a reusable medicine
                  definition for prescriptions
                  and inventory management.
                </p>
              </div>
            </div>

            <button
              className="medicine-form-close"
              type="button"
              onClick={() =>
                setShowForm(false)
              }
              aria-label="Close add medicine form"
            >
              <X size={16} />
            </button>
          </div>

          <form
            className="medicine-create-form"
            onSubmit={(event) => {
              event.preventDefault();
              createMutation.mutate();
            }}
          >
            <div className="medicine-form-section">
              <div className="medicine-form-section-heading">
                <span>
                  Identity
                </span>

                <h4>
                  Medicine identification
                </h4>

                <p>
                  The code must be unique in
                  the medicine catalog.
                </p>
              </div>

              <div className="medicine-form-grid">
                <label className="field">
                  <span>
                    Medicine code
                  </span>

                  <input
                    value={
                      form.medicineCode
                    }
                    onChange={(event) =>
                      setForm(
                        (current) => ({
                          ...current,
                          medicineCode:
                            event.target.value,
                        }),
                      )
                    }
                    placeholder="MED-0001"
                    required
                    maxLength={64}
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field">
                  <span>Name</span>

                  <input
                    value={form.name}
                    onChange={(event) =>
                      setForm(
                        (current) => ({
                          ...current,
                          name:
                            event.target.value,
                        }),
                      )
                    }
                    placeholder="Paracetamol"
                    required
                    maxLength={256}
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field field-wide">
                  <span>
                    Generic name
                  </span>

                  <input
                    value={
                      form.genericName
                    }
                    onChange={(event) =>
                      setForm(
                        (current) => ({
                          ...current,
                          genericName:
                            event.target.value,
                        }),
                      )
                    }
                    placeholder="Acetaminophen"
                    maxLength={256}
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>
              </div>
            </div>

            <div className="medicine-form-section">
              <div className="medicine-form-section-heading">
                <span>
                  Pharmaceutical profile
                </span>

                <h4>
                  Form and strength
                </h4>

                <p>
                  Define how the medicine is
                  dispensed and represented
                  clinically.
                </p>
              </div>

              <div className="medicine-form-grid">
                <label className="field">
                  <span>
                    Dosage form
                  </span>

                  <input
                    value={
                      form.dosageForm
                    }
                    onChange={(event) =>
                      setForm(
                        (current) => ({
                          ...current,
                          dosageForm:
                            event.target.value,
                        }),
                      )
                    }
                    placeholder="TABLET"
                    required
                    maxLength={64}
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field">
                  <span>
                    Strength
                  </span>

                  <input
                    value={
                      form.strength
                    }
                    onChange={(event) =>
                      setForm(
                        (current) => ({
                          ...current,
                          strength:
                            event.target.value,
                        }),
                      )
                    }
                    placeholder="500 mg"
                    maxLength={128}
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field">
                  <span>
                    Unit
                  </span>

                  <input
                    value={
                      form.unit
                    }
                    onChange={(event) =>
                      setForm(
                        (current) => ({
                          ...current,
                          unit:
                            event.target.value,
                        }),
                      )
                    }
                    placeholder="tablet"
                    required
                    maxLength={32}
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>

                <label className="field">
                  <span>
                    Reorder level
                  </span>

                  <input
                    type="number"
                    min={0}
                    step="any"
                    value={
                      form.reorderLevel ?? 0
                    }
                    onChange={(event) =>
                      setForm(
                        (current) => ({
                          ...current,
                          reorderLevel:
                            Number(
                              event.target.value,
                            ),
                        }),
                      )
                    }
                    disabled={
                      createMutation.isPending
                    }
                  />
                </label>
              </div>
            </div>

            {createMutation.isError && (
              <div
                className="alert medicine-create-error"
                role="alert"
              >
                {getApiErrorMessage(
                  createMutation.error,
                )}
              </div>
            )}

            <footer className="medicine-form-footer">
              <div className="medicine-form-hint">
                <ShieldCheck size={15} />

                <span>
                  Required fields are validated
                  before the medicine is added
                  to the catalog.
                </span>
              </div>

              <div className="medicine-form-actions">
                <button
                  className="app-button"
                  type="button"
                  onClick={() =>
                    setShowForm(false)
                  }
                  disabled={
                    createMutation.isPending
                  }
                >
                  Cancel
                </button>

                <button
                  className="app-button medicine-save-button"
                  type="submit"
                  disabled={
                    createMutation.isPending
                  }
                >
                  <CheckCircle2
                    size={15}
                  />

                  {createMutation.isPending
                    ? "Creating..."
                    : "Create medicine"}
                </button>
              </div>
            </footer>
          </form>
        </section>
      )}

      {medicinesQuery.isLoading && (
        <div className="medicine-state-card">
          <FlaskConical size={28} />
          <strong>
            Loading medicine catalog
          </strong>
          <span>
            Retrieving the latest medicines...
          </span>
        </div>
      )}

      {medicinesQuery.isError && (
        <div
          className="alert"
          role="alert"
        >
          Unable to load medicines.
          Please refresh and try again.
        </div>
      )}

      {!medicinesQuery.isLoading &&
        !medicinesQuery.isError &&
        medicines.length === 0 && (
          <div className="medicine-state-card">
            <div className="medicine-state-icon">
              <FlaskConical size={20} />
            </div>

            <strong>
              No medicines yet
            </strong>

            <span>
              Add the first medicine to start
              building the pharmacy catalog.
            </span>

            <button
              className="app-button"
              type="button"
              onClick={() =>
                setShowForm(true)
              }
            >
              <Plus size={15} />
              Add first medicine
            </button>
          </div>
        )}

      {!medicinesQuery.isLoading &&
        !medicinesQuery.isError &&
        medicines.length > 0 &&
        filteredMedicines.length === 0 && (
          <div className="medicine-state-card">
            <div className="medicine-state-icon">
              <Search size={20} />
            </div>

            <strong>
              No matching medicines
            </strong>

            <span>
              Try a medicine name, generic
              name, code, strength, dosage
              form, or unit.
            </span>
          </div>
        )}

      {filteredMedicines.length > 0 && (
        <div className="medicine-table-shell">
          <div className="medicine-table-header">
            <div>
              <span>
                Medicine catalog
              </span>

              <strong>
                {filteredMedicines.length}{" "}
                visible medicine
                {filteredMedicines.length ===
                1
                  ? ""
                  : "s"}
              </strong>
            </div>
          </div>

          <div className="table-wrap">
            <table className="medicine-table">
              <thead>
                <tr>
                  <th>Medicine</th>
                  <th>Code</th>
                  <th>Generic name</th>
                  <th>Form</th>
                  <th>Strength</th>
                  <th>Unit</th>
                  <th>Reorder</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {filteredMedicines.map(
                  (medicine) => (
                    <tr
                      key={medicine.id}
                      className="medicine-table-row"
                    >
                      <td>
                        <div className="medicine-name-cell">
                          <div className="medicine-mini-icon">
                            <FlaskConical
                              size={14}
                            />
                          </div>

                          <div>
                            <strong>
                              {medicine.name}
                            </strong>

                            <span>
                              Medicine #
                              {medicine.id}
                            </span>
                          </div>
                        </div>
                      </td>

                      <td>
                        <span className="medicine-code">
                          {
                            medicine.medicineCode
                          }
                        </span>
                      </td>

                      <td>
                        {medicine.genericName ||
                          "—"}
                      </td>

                      <td>
                        {medicine.dosageForm ||
                          "—"}
                      </td>

                      <td>
                        {medicine.strength ||
                          "—"}
                      </td>

                      <td>
                        {medicine.unit ||
                          "—"}
                      </td>

                      <td>
                        {medicine.reorderLevel}
                      </td>

                      <td>
                        <span
                          className={`medicine-status ${
                            medicine.active
                              ? "active"
                              : "inactive"
                          }`}
                        >
                          <i />

                          {medicine.active
                            ? "Active"
                            : "Inactive"}
                        </span>
                      </td>

                      <td>
                        {medicine.active && (
                          <button
                            className="app-button app-button-danger medicine-deactivate-button"
                            type="button"
                            disabled={
                              deactivateMutation.isPending
                            }
                            onClick={() => {
                              const confirmed =
                                window.confirm(
                                  `Deactivate ${medicine.name}?`,
                                );

                              if (
                                confirmed
                              ) {
                                deactivateMutation.mutate(
                                  medicine.id,
                                );
                              }
                            }}
                          >
                            Deactivate
                          </button>
                        )}
                      </td>
                    </tr>
                  ),
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {response &&
        response.totalPages > 0 && (
          <div className="pagination-bar">
            <span className="helper-text">
              Page {response.page + 1} of{" "}
              {response.totalPages} ·{" "}
              {response.totalElements} total
            </span>

            <div className="pagination-controls">
              <button
                className="app-button"
                type="button"
                disabled={
                  page === 0 ||
                  medicinesQuery.isFetching
                }
                onClick={() =>
                  setPage(
                    (value) => value - 1,
                  )
                }
              >
                Previous
              </button>

              <button
                className="app-button"
                type="button"
                disabled={
                  page + 1 >=
                    response.totalPages ||
                  medicinesQuery.isFetching
                }
                onClick={() =>
                  setPage(
                    (value) => value + 1,
                  )
                }
              >
                Next
              </button>
            </div>
          </div>
        )}
    </section>
  );
}

function getApiErrorMessage(
  error: unknown,
): string {
  if (axios.isAxiosError(error)) {
    const data =
      error.response?.data as
        | {
            message?: unknown;
          }
        | undefined;

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

  return "Unable to create the medicine. Check the medicine code is unique.";
}