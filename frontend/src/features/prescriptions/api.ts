import { apiClient } from "../../api/client";
import type { ApiResponse, PagedApiResponse } from "../../types/api";

export type PrescriptionStatus = "ACTIVE" | "DISPENSED" | "CANCELLED" | "EXPIRED";

export type PrescriptionItem = {
  id: number;
  medicineId: number;
  dosage: string;
  route: string;
  frequency: string;
  durationDays: number;
  quantityPrescribed: number;
  instructions: string | null;
  createdAt: string;
  updatedAt: string;
};

export type Prescription = {
  id: number;
  patientId: number;
  doctorId: number;
  appointmentId: number | null;
  medicalRecordId: number | null;
  status: PrescriptionStatus;
  prescribedAt: string;
  expiresAt: string | null;
  notes: string | null;
  cancelledAt: string | null;
  cancellationReason: string | null;
  items: PrescriptionItem[];
};

export type CreatePrescriptionInput = {
  patientId: number;
  doctorId: number;
  appointmentId?: number;
  medicalRecordId?: number;
  expiresAt?: string;
  notes?: string;
  items: Array<{
    medicineId: number;
    dosage: string;
    route: string;
    frequency: string;
    durationDays: number;
    quantityPrescribed: number;
    instructions?: string;
  }>;
};

export type CancelPrescriptionInput = {
  cancellationReason: string;
};

export async function getPatientPrescriptions(
  patientId: number,
): Promise<PagedApiResponse<Prescription>> {
  const response = await apiClient.get<PagedApiResponse<Prescription>>(
    `/prescriptions/patient/${patientId}`,
    { params: { page: 0, size: 50, sort: "prescribedAt,desc" } },
  );
  return response.data;
}

export async function getPrescription(
  prescriptionId: number,
): Promise<ApiResponse<Prescription>> {
  const response = await apiClient.get<ApiResponse<Prescription>>(
    `/prescriptions/${prescriptionId}`,
  );
  return response.data;
}

export async function createPrescription(
  input: CreatePrescriptionInput,
): Promise<ApiResponse<Prescription>> {
  const response = await apiClient.post<ApiResponse<Prescription>>(
    "/prescriptions",
    input,
  );
  return response.data;
}

export async function cancelPrescription(
  prescriptionId: number,
  input: CancelPrescriptionInput,
): Promise<ApiResponse<Prescription>> {
  const response = await apiClient.post<ApiResponse<Prescription>>(
    `/prescriptions/${prescriptionId}/cancel`,
    input,
  );
  return response.data;
}

export async function dispensePrescription(
  prescriptionId: number,
): Promise<ApiResponse<Prescription>> {
  const response = await apiClient.post<ApiResponse<Prescription>>(
    `/prescriptions/${prescriptionId}/dispense`,
  );
  return response.data;
}
