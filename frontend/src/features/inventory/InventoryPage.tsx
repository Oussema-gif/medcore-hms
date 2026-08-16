import { useMemo, useState } from "react";
import {
  Boxes,
  CheckCircle2,
  Clock3,
  FlaskConical,
  PackagePlus,
  Pill,
  RefreshCw,
  Search,
  ShieldCheck,
  TrendingDown,
  Truck,
  X,
} from "lucide-react";
import {
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import axios from "axios";

import {
  adjustInventoryBatch,
  getAvailableBatchesByMedicine,
  getBatchMovements,
  receiveInventoryBatch,
} from "./api";

import { getMedicines } from "../medicines/api";

export function InventoryPage() {
  const queryClient = useQueryClient();

  const [medicineId, setMedicineId] = useState<string>("");
  const [selectedBatchId, setSelectedBatchId] = useState<number | null>(null);

  const [medicineSearch, setMedicineSearch] = useState("");

  const [lotNumber, setLotNumber] = useState("");
  const [expiryDate, setExpiryDate] = useState("");
  const [quantity, setQuantity] = useState("");
  const [unitCost, setUnitCost] = useState("");
  const [supplierReference, setSupplierReference] = useState("");

  const [adjustDelta, setAdjustDelta] = useState("");
  const [adjustReason, setAdjustReason] = useState("");

  const medicinesQuery = useQuery({
    queryKey: ["medicines", "all"],
    queryFn: () => getMedicines(0, 100),
  });

  const batchesQuery = useQuery({
    queryKey: ["inventory-batches", medicineId],
    queryFn: () => getAvailableBatchesByMedicine(Number(medicineId)),
    enabled: Boolean(medicineId),
  });

  const movementsQuery = useQuery({
    queryKey: ["stock-movements", selectedBatchId],
    queryFn: () => getBatchMovements(selectedBatchId as number),
    enabled: selectedBatchId !== null,
  });

  const receiveMutation = useMutation({
    mutationFn: () =>
      receiveInventoryBatch({
        medicineId: Number(medicineId),
        lotNumber: lotNumber.trim(),
        expiryDate,
        quantity: Number(quantity),
        unitCost: Number(unitCost),
        supplierReference: supplierReference.trim() || undefined,
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["inventory-batches", medicineId],
      });

      setLotNumber("");
      setExpiryDate("");
      setQuantity("");
      setUnitCost("");
      setSupplierReference("");
    },
  });

  const adjustMutation = useMutation({
    mutationFn: () => {
      if (!selectedBatchId) {
        throw new Error("Select a batch first.");
      }

      return adjustInventoryBatch(selectedBatchId, {
        quantityDelta: Number(adjustDelta),
        reason: adjustReason.trim(),
      });
    },

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["inventory-batches", medicineId],
      });

      await queryClient.invalidateQueries({
        queryKey: ["stock-movements", selectedBatchId],
      });

      setAdjustDelta("");
      setAdjustReason("");
    },
  });

  const medicines = useMemo(
    () => medicinesQuery.data?.data ?? [],
    [medicinesQuery.data?.data],
  );

  const batches = batchesQuery.data?.data ?? [];
  const movements = movementsQuery.data?.data ?? [];

  const normalizedMedicineSearch =
    medicineSearch.trim().toLowerCase();

  const filteredMedicines = useMemo(() => {
    if (!normalizedMedicineSearch) {
      return medicines;
    }

    return medicines.filter((medicine) =>
      [
        medicine.name,
        medicine.medicineCode,
        medicine.genericName ?? "",
        medicine.dosageForm,
        medicine.strength ?? "",
        medicine.unit,
      ]
        .join(" ")
        .toLowerCase()
        .includes(normalizedMedicineSearch),
    );
  }, [
    medicines,
    normalizedMedicineSearch,
  ]);

  const selectedMedicine = medicines.find(
    (medicine) => String(medicine.id) === medicineId,
  );

  const selectedBatch = batches.find(
    (batch) => batch.id === selectedBatchId,
  );

  function handleMedicineChange(value: string) {
    setMedicineId(value);
    setSelectedBatchId(null);
    setAdjustDelta("");
    setAdjustReason("");
  }

  function formatExpiry(date: string) {
    if (!date) {
      return "—";
    }

    return new Date(`${date}T00:00:00`).toLocaleDateString();
  }

  return (
    <section className="inventory-page">
      <div className="page-header inventory-page-header">
        <div>
          <span className="eyebrow">Pharmacy operations</span>

          <h2>Inventory</h2>

          <p>
            Receive stock, monitor batches, track expiry dates,
            and maintain accurate quantities.
          </p>
        </div>

        <button
          className="app-button"
          type="button"
          onClick={() => batchesQuery.refetch()}
          disabled={!medicineId || batchesQuery.isFetching}
        >
          <RefreshCw
            size={15}
            className={
              batchesQuery.isFetching ? "inventory-spin" : undefined
            }
          />

          {batchesQuery.isFetching
            ? "Refreshing..."
            : "Refresh"}
        </button>
      </div>

      {/* =========================================================
          SELECTED MEDICINE HEADER
          ========================================================= */}

      <section className="inventory-selected-card">
        <div className="inventory-selected-icon">
          <Pill size={19} />
        </div>

        <div className="inventory-selected-content">
          <span className="inventory-section-label">
            Inventory context
          </span>

          <h3>
            {selectedMedicine?.name ?? "Select a medicine"}
          </h3>

          <p>
            {selectedMedicine
              ? `${selectedMedicine.medicineCode}${selectedMedicine.genericName
                ? ` · ${selectedMedicine.genericName}`
                : ""
              }`
              : "Choose a medicine to manage its stock and batches."}
          </p>
        </div>

        {selectedMedicine && (
          <div className="inventory-selected-meta">
            <span>
              <strong>{batches.length}</strong>
              {batches.length === 1 ? " batch" : " batches"}
            </span>

            <span>
              Unit
              <strong>{selectedMedicine.unit}</strong>
            </span>
          </div>
        )}

        {selectedMedicine && (
          <div className="inventory-selected-medicine">
            <div className="inventory-selected-medicine-icon">
              <FlaskConical size={17} />
            </div>

            <div className="inventory-selected-medicine-copy">
              <span>Selected medicine</span>

              <strong>
                {selectedMedicine.name}
              </strong>

              <small>
                {selectedMedicine.medicineCode}
                {" · "}
                {selectedMedicine.dosageForm}
                {selectedMedicine.strength
                  ? ` · ${selectedMedicine.strength}`
                  : ""}
                {selectedMedicine.unit
                  ? ` · ${selectedMedicine.unit}`
                  : ""}
              </small>
            </div>

            <button
              type="button"
              className="inventory-selected-medicine-clear"
              onClick={() => {
                setMedicineId("");
                setSelectedBatchId(null);
                setMedicineSearch("");
              }}
              aria-label="Change selected medicine"
            >
              <X size={15} />
            </button>
          </div>
        )}
      </section>

      {/* =========================================================
          MEDICINE PICKER
          Full-width panel, only shown until a medicine is chosen.
          ========================================================= */}

      {!selectedMedicine && (
        <section className="inventory-medicine-picker">
          <header className="inventory-medicine-picker-header">
            <div>
              <span>Medicine lookup</span>
              <h3>Select a medicine</h3>
              <p>
                Search by name, code, or generic name to manage
                its stock and batches.
              </p>
            </div>

            <div className="inventory-medicine-picker-icon">
              <FlaskConical size={17} />
            </div>
          </header>

          <div className="inventory-medicine-search">
            <Search size={15} />

            <input
              type="text"
              value={medicineSearch}
              onChange={(event) =>
                setMedicineSearch(event.target.value)
              }
              placeholder="Search medicines by name, code, or generic name..."
            />

            {medicineSearch && (
              <button
                type="button"
                className="inventory-search-clear"
                onClick={() => setMedicineSearch("")}
                aria-label="Clear search"
              >
                <X size={14} />
              </button>
            )}
          </div>

          {filteredMedicines.length === 0 ? (
            <div className="inventory-medicine-empty">
              No medicines match your search.
            </div>
          ) : (
            <div className="inventory-medicine-results">
              {filteredMedicines.map((medicine) => (
                <button
                  key={medicine.id}
                  type="button"
                  className="inventory-medicine-result"
                  onClick={() => {
                    handleMedicineChange(String(medicine.id));
                    setMedicineSearch("");
                  }}
                >
                  <div className="inventory-medicine-result-icon">
                    <FlaskConical size={15} />
                  </div>

                  <div className="inventory-medicine-result-copy">
                    <strong>
                      {medicine.name}
                    </strong>

                    <span>
                      {medicine.genericName ||
                        "Generic name not specified"}
                    </span>

                    <small>
                      {medicine.medicineCode}
                      {" · "}
                      {medicine.dosageForm}
                      {medicine.strength
                        ? ` · ${medicine.strength}`
                        : ""}
                      {medicine.unit
                        ? ` · ${medicine.unit}`
                        : ""}
                    </small>
                  </div>

                  <CheckCircle2 size={15} />
                </button>
              ))}
            </div>
          )}
        </section>
      )}

      {!medicineId && (
        <div className="inventory-empty-context">
          <div className="inventory-empty-icon">
            <Boxes size={20} />
          </div>

          <strong>Select a medicine to continue</strong>

          <span>
            Inventory batches, receiving, and stock adjustments
            will appear here once a medicine is selected.
          </span>
        </div>
      )}

      {medicineId && (
        <>
          {/* =====================================================
              MAIN INVENTORY WORKSPACE
              ===================================================== */}

          <div className="inventory-workspace-grid">
            {/* RECEIVE */}
            <article className="inventory-module-card inventory-receive-card">
              <header className="inventory-module-header">
                <div className="inventory-module-title">
                  <div className="inventory-module-icon inventory-icon-cyan">
                    <PackagePlus size={18} />
                  </div>

                  <div>
                    <span>Stock intake</span>
                    <h3>Receive inventory batch</h3>
                    <p>
                      Add a new lot of stock to the selected
                      medicine.
                    </p>
                  </div>
                </div>

                <span className="inventory-module-status">
                  <CheckCircle2 size={12} />
                  Ready
                </span>
              </header>

              <form
                className="inventory-form-grid"
                onSubmit={(event) => {
                  event.preventDefault();
                  receiveMutation.mutate();
                }}
              >
                <label className="field">
                  <span>Lot number</span>

                  <input
                    value={lotNumber}
                    onChange={(event) =>
                      setLotNumber(event.target.value)
                    }
                    required
                    maxLength={128}
                    placeholder="LOT-2026-001"
                    disabled={receiveMutation.isPending}
                  />
                </label>

                <label className="field">
                  <span>Expiry date</span>

                  <div className="inventory-input-with-icon">
                    <Clock3 size={14} />

                    <input
                      type="date"
                      value={expiryDate}
                      onChange={(event) =>
                        setExpiryDate(event.target.value)
                      }
                      required
                      disabled={receiveMutation.isPending}
                    />
                  </div>
                </label>

                <label className="field">
                  <span>Quantity</span>

                  <input
                    type="number"
                    min={0.0001}
                    step="any"
                    value={quantity}
                    onChange={(event) =>
                      setQuantity(event.target.value)
                    }
                    required
                    placeholder="100"
                    disabled={receiveMutation.isPending}
                  />
                </label>

                <label className="field">
                  <span>Unit cost</span>

                  <input
                    type="number"
                    min={0}
                    step="any"
                    value={unitCost}
                    onChange={(event) =>
                      setUnitCost(event.target.value)
                    }
                    required
                    placeholder="12.50"
                    disabled={receiveMutation.isPending}
                  />
                </label>

                <label className="field field-wide">
                  <span>Supplier reference</span>

                  <div className="inventory-input-with-icon">
                    <Truck size={14} />

                    <input
                      value={supplierReference}
                      onChange={(event) =>
                        setSupplierReference(event.target.value)
                      }
                      maxLength={256}
                      placeholder="PO-2026-0042"
                      disabled={receiveMutation.isPending}
                    />
                  </div>
                </label>

                {receiveMutation.isError && (
                  <div
                    className="alert field-wide"
                    role="alert"
                  >
                    {getApiErrorMessage(
                      receiveMutation.error,
                    )}
                  </div>
                )}

                <div className="inventory-form-footer field-wide">
                  <div className="inventory-form-note">
                    <ShieldCheck size={13} />

                    <span>
                      Stock quantity and batch data are validated
                      before submission.
                    </span>
                  </div>

                  <button
                    className="app-button"
                    type="submit"
                    disabled={receiveMutation.isPending}
                  >
                    <PackagePlus size={15} />

                    {receiveMutation.isPending
                      ? "Receiving..."
                      : "Receive batch"}
                  </button>
                </div>
              </form>
            </article>

            {/* AVAILABLE BATCHES */}
            <article className="inventory-module-card inventory-batches-card">
              <header className="inventory-module-header">
                <div className="inventory-module-title">
                  <div className="inventory-module-icon inventory-icon-blue">
                    <Boxes size={18} />
                  </div>

                  <div>
                    <span>Stock overview</span>
                    <h3>Available batches</h3>
                    <p>
                      Active stock lots currently available for
                      this medicine.
                    </p>
                  </div>
                </div>

                <span className="inventory-batch-count">
                  {batches.length}
                  {batches.length === 1
                    ? " batch"
                    : " batches"}
                </span>
              </header>

              {batchesQuery.isLoading && (
                <div className="inventory-state">
                  <div className="inventory-state-icon">
                    <Boxes size={17} />
                  </div>

                  <strong>Loading batches</strong>

                  <span>
                    Retrieving current stock information...
                  </span>
                </div>
              )}

              {batchesQuery.isError && (
                <div className="alert" role="alert">
                  Unable to load inventory batches.
                </div>
              )}

              {!batchesQuery.isLoading &&
                !batchesQuery.isError &&
                batches.length === 0 && (
                  <div className="inventory-state">
                    <div className="inventory-state-icon">
                      <Boxes size={17} />
                    </div>

                    <strong>No available stock</strong>

                    <span>
                      No active inventory batches are currently
                      available for this medicine.
                    </span>
                  </div>
                )}

              {batches.length > 0 && (
                <div className="inventory-batch-grid">
                  {batches.map((batch) => {
                    const selected =
                      selectedBatchId === batch.id;

                    return (
                      <button
                        key={batch.id}
                        type="button"
                        className={`inventory-batch-card ${selected ? "is-selected" : ""
                          }`}
                        onClick={() =>
                          setSelectedBatchId(batch.id)
                        }
                        aria-pressed={selected}
                      >
                        <div className="inventory-batch-top">
                          <div className="inventory-batch-icon">
                            <Boxes size={15} />
                          </div>

                          <div className="inventory-batch-heading">
                            <span>LOT</span>

                            <strong>
                              {batch.lotNumber}
                            </strong>
                          </div>

                          {selected && (
                            <CheckCircle2
                              className="inventory-selected-check"
                              size={16}
                            />
                          )}
                        </div>

                        <div className="inventory-batch-values">
                          <div>
                            <span>On hand</span>

                            <strong>
                              {batch.quantityOnHand}
                            </strong>
                          </div>

                          <div>
                            <span>Unit cost</span>

                            <strong>
                              {batch.unitCost}
                            </strong>
                          </div>
                        </div>

                        <div className="inventory-batch-footer">
                          <span className="inventory-expiry">
                            <Clock3 size={11} />
                            {formatExpiry(batch.expiryDate)}
                          </span>

                          <span className="inventory-active-badge">
                            Active
                          </span>
                        </div>
                      </button>
                    );
                  })}
                </div>
              )}
            </article>
          </div>

          {/* =====================================================
              SELECTED BATCH
              ===================================================== */}

          {selectedBatch && (
            <section className="inventory-selected-batch-card">
              <header className="inventory-selected-batch-header">
                <div>
                  <span className="inventory-section-label">
                    Selected batch
                  </span>

                  <h3>
                    Lot {selectedBatch.lotNumber}
                  </h3>

                  <p>
                    Manage quantity for the currently selected
                    inventory batch.
                  </p>
                </div>

                <div className="inventory-selected-batch-badge">
                  <CheckCircle2 size={13} />
                  Active batch
                </div>
              </header>

              <div className="inventory-selected-batch-summary">
                <div>
                  <span>On hand</span>
                  <strong>
                    {selectedBatch.quantityOnHand}
                  </strong>
                </div>

                <div>
                  <span>Initial quantity</span>
                  <strong>
                    {selectedBatch.initialQuantity}
                  </strong>
                </div>

                <div>
                  <span>Unit cost</span>
                  <strong>
                    {selectedBatch.unitCost}
                  </strong>
                </div>

                <div>
                  <span>Expiry</span>
                  <strong>
                    {formatExpiry(
                      selectedBatch.expiryDate,
                    )}
                  </strong>
                </div>
              </div>

              <div className="inventory-adjustment-section">
                <div className="inventory-adjustment-heading">
                  <div className="inventory-adjustment-icon">
                    <TrendingDown size={16} />
                  </div>

                  <div>
                    <span>Inventory adjustment</span>
                    <h4>Adjust stock quantity</h4>
                  </div>
                </div>

                <form
                  className="inventory-adjustment-form"
                  onSubmit={(event) => {
                    event.preventDefault();
                    adjustMutation.mutate();
                  }}
                >
                  <label className="field">
                    <span>Quantity delta</span>

                    <input
                      type="number"
                      step="any"
                      value={adjustDelta}
                      onChange={(event) =>
                        setAdjustDelta(event.target.value)
                      }
                      placeholder="-5 or 10"
                      required
                      disabled={adjustMutation.isPending}
                    />
                  </label>

                  <label className="field inventory-adjustment-reason">
                    <span>Reason</span>

                    <input
                      value={adjustReason}
                      onChange={(event) =>
                        setAdjustReason(event.target.value)
                      }
                      placeholder="Stock correction"
                      required
                      maxLength={512}
                      disabled={adjustMutation.isPending}
                    />
                  </label>

                  <button
                    className="app-button"
                    type="submit"
                    disabled={adjustMutation.isPending}
                  >
                    {adjustMutation.isPending
                      ? "Adjusting..."
                      : "Adjust stock"}
                  </button>
                </form>

                {adjustMutation.isError && (
                  <div
                    className="alert inventory-adjustment-alert"
                    role="alert"
                  >
                    {getApiErrorMessage(
                      adjustMutation.error,
                    )}
                  </div>
                )}
              </div>

              <div className="inventory-movements-section">
                <div className="inventory-movements-heading">
                  <div>
                    <span>Audit trail</span>
                    <h4>Stock movements</h4>
                  </div>

                  <span className="inventory-movement-count">
                    {movements.length}
                  </span>
                </div>

                {movementsQuery.isLoading && (
                  <div className="inventory-state compact">
                    Loading stock movements...
                  </div>
                )}

                {!movementsQuery.isLoading &&
                  movements.length === 0 && (
                    <div className="inventory-state compact">
                      <Clock3 size={17} />
                      <span>
                        No movements recorded for this batch.
                      </span>
                    </div>
                  )}

                {movements.length > 0 && (
                  <div className="table-wrap inventory-movements-table">
                    <table>
                      <thead>
                        <tr>
                          <th>Type</th>
                          <th>Delta</th>
                          <th>After</th>
                          <th>Occurred</th>
                          <th>Reason</th>
                        </tr>
                      </thead>

                      <tbody>
                        {movements.map((movement) => (
                          <tr key={movement.id}>
                            <td>{movement.movementType}</td>

                            <td>
                              {movement.quantityDelta}
                            </td>

                            <td>
                              {movement.quantityAfter}
                            </td>

                            <td>
                              {new Date(
                                movement.occurredAt,
                              ).toLocaleString()}
                            </td>

                            <td>
                              {movement.reason ?? "—"}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </section>
          )}
        </>
      )}
    </section>
  );
}

function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as
      | { message?: unknown }
      | undefined;

    if (typeof data?.message === "string") {
      return data.message;
    }

    return `Request failed with status ${error.response?.status ?? "unknown"
      }.`;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "An unexpected error occurred.";
}