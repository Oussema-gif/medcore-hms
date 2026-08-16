import { apiClient } from "../../api/client";
import type { ApiResponse } from "../../types/api";

export type InventoryBatch = {
  id: number;
  medicineId: number;
  lotNumber: string;
  expiryDate: string;
  receivedAt: string;
  initialQuantity: number;
  quantityOnHand: number;
  unitCost: number;
  supplierReference: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type StockMovement = {
  id: number;
  inventoryBatchId: number;
  movementType:
    | "RECEIPT"
    | "ADJUSTMENTIN"
    | "ADJUSTMENTOUT"
    | "DISPENSE"
    | "REVERSAL"
    | "EXPIREDWRITEOFF";
  quantityDelta: number;
  quantityAfter: number;
  occurredAt: string;
  referenceType: string | null;
  referenceId: number | null;
  prescriptionItemId: number | null;
  reason: string | null;
  performedByUserId: number;
  createdAt: string;
};

export type ReceiveInventoryBatchInput = {
  medicineId: number;
  lotNumber: string;
  expiryDate: string;
  quantity: number;
  unitCost: number;
  supplierReference?: string;
};

export type AdjustInventoryInput = {
  quantityDelta: number;
  reason: string;
};

export async function receiveInventoryBatch(
  input: ReceiveInventoryBatchInput,
): Promise<ApiResponse<InventoryBatch>> {
  const response = await apiClient.post<ApiResponse<InventoryBatch>>(
    "/inventory-batches",
    input,
  );
  return response.data;
}

export async function getAvailableBatchesByMedicine(
  medicineId: number,
): Promise<ApiResponse<InventoryBatch[]>> {
  const response = await apiClient.get<ApiResponse<InventoryBatch[]>>(
    `/inventory-batches/medicine/${medicineId}`,
  );
  return response.data;
}

export async function getBatchMovements(
  batchId: number,
): Promise<ApiResponse<StockMovement[]>> {
  const response = await apiClient.get<ApiResponse<StockMovement[]>>(
    `/inventory-batches/${batchId}/movements`,
  );
  return response.data;
}

export async function adjustInventoryBatch(
  batchId: number,
  input: AdjustInventoryInput,
): Promise<ApiResponse<InventoryBatch>> {
  const response = await apiClient.post<ApiResponse<InventoryBatch>>(
    `/inventory-batches/${batchId}/adjust`,
    input,
  );
  return response.data;
}
