import { apiClient } from "../../api/client";
import type { ApiResponse, PagedApiResponse } from "../../types/api";

export type Medicine = {
  id: number;
  medicineCode: string;
  name: string;
  genericName: string | null;
  dosageForm: string;
  strength: string | null;
  unit: string;
  reorderLevel: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type CreateMedicineInput = {
  medicineCode: string;
  name: string;
  genericName?: string;
  dosageForm: string;
  strength?: string;
  unit: string;
  reorderLevel?: number;
};

export type UpdateMedicineInput = Partial<CreateMedicineInput>;

export async function getMedicines(
  page = 0,
  size = 20,
): Promise<PagedApiResponse<Medicine>> {
  const response = await apiClient.get<PagedApiResponse<Medicine>>(
    "/medicines",
    { params: { page, size, sort: "name" } },
  );
  return response.data;
}

export async function getMedicine(
  medicineId: number,
): Promise<ApiResponse<Medicine>> {
  const response = await apiClient.get<ApiResponse<Medicine>>(
    `/medicines/${medicineId}`,
  );
  return response.data;
}

export async function createMedicine(
  input: CreateMedicineInput,
): Promise<ApiResponse<Medicine>> {
  const response = await apiClient.post<ApiResponse<Medicine>>(
    "/medicines",
    input,
  );
  return response.data;
}

export async function updateMedicine(
  medicineId: number,
  input: UpdateMedicineInput,
): Promise<ApiResponse<Medicine>> {
  const response = await apiClient.patch<ApiResponse<Medicine>>(
    `/medicines/${medicineId}`,
    input,
  );
  return response.data;
}

export async function deleteMedicine(medicineId: number): Promise<void> {
  await apiClient.delete(`/medicines/${medicineId}`);
}
