import { apiClient } from "../../api/client";
import type { ApiResponse, PagedApiResponse } from "../../types/api";

export type LaboratoryPriority = "ROUTINE" | "URGENT" | "STAT";

export type LaboratoryRequestStatus =
  | "REQUESTED"
  | "SAMPLECOLLECTED"
  | "INPROGRESS"
  | "RESULTREADY"
  | "RELEASED"
  | "CANCELLED";

export type LaboratoryTestItem = {
  id: number;
  testCode: string;
  testName: string;
  sortOrder: number;
};

export type LaboratoryRequest = {
  id: number;
  patientId: number;
  orderingDoctorId: number;
  appointmentId: number | null;
  medicalRecordId: number | null;
  priority: LaboratoryPriority;
  status: LaboratoryRequestStatus;
  clinicalNotes: string | null;
  requestedAt: string;
  releasedAt: string | null;
  cancelledAt: string | null;
  cancellationReason: string | null;
  items: LaboratoryTestItem[];
};

export type CreateLaboratoryRequestInput = {
  patientId: number;
  orderingDoctorId: number;
  appointmentId?: number;
  medicalRecordId?: number;
  priority?: LaboratoryPriority;
  clinicalNotes?: string;
  items: Array<{
    testCode: string;
    testName: string;
    sortOrder?: number;
  }>;
};

export type LaboratoryResult = {
  id: number;
  laboratoryRequestItemId: number;
  resultValue: number | null;
  resultText: string | null;
  unit: string | null;
  referenceRange: string | null;
  interpretation: string | null;
  recordedByUserId: number;
  recordedAt: string;
  releasedAt: string | null;
  amendedFromResultId: number | null;
  createdAt: string;
  updatedAt: string;
};

export type CreateLaboratoryResultInput = {
  laboratoryRequestItemId: number;
  resultValue?: number;
  resultText?: string;
  unit?: string;
  referenceRange?: string;
  interpretation?: string;
  amendedFromResultId?: number;
};

export type CancelLaboratoryRequestInput = {
  cancellationReason: string;
};

export async function getPatientLaboratoryRequests(
  patientId: number,
): Promise<PagedApiResponse<LaboratoryRequest>> {
  const response = await apiClient.get<PagedApiResponse<LaboratoryRequest>>(
    `/laboratory-requests/patient/${patientId}`,
    {
      params: {
        page: 0,
        size: 50,
        sort: "requestedAt,desc",
      },
    },
  );
  return response.data;
}

export async function getLaboratoryRequest(
  requestId: number,
): Promise<ApiResponse<LaboratoryRequest>> {
  const response = await apiClient.get<ApiResponse<LaboratoryRequest>>(
    `/laboratory-requests/${requestId}`,
  );
  return response.data;
}

export async function createLaboratoryRequest(
  input: CreateLaboratoryRequestInput,
): Promise<ApiResponse<LaboratoryRequest>> {
  const response = await apiClient.post<ApiResponse<LaboratoryRequest>>(
    "/laboratory-requests",
    input,
  );
  return response.data;
}

export async function cancelLaboratoryRequest(
  requestId: number,
  input: CancelLaboratoryRequestInput,
): Promise<ApiResponse<LaboratoryRequest>> {
  const response = await apiClient.post<ApiResponse<LaboratoryRequest>>(
    `/laboratory-requests/${requestId}/cancel`,
    input,
  );
  return response.data;
}

export async function getLaboratoryResultsByRequestItem(
  requestItemId: number,
): Promise<ApiResponse<LaboratoryResult[]>> {
  const response = await apiClient.get<ApiResponse<LaboratoryResult[]>>(
    `/laboratory-results/request-item/${requestItemId}`,
  );
  return response.data;
}

export async function getLaboratoryResult(
  resultId: number,
): Promise<ApiResponse<LaboratoryResult>> {
  const response = await apiClient.get<ApiResponse<LaboratoryResult>>(
    `/laboratory-results/${resultId}`,
  );
  return response.data;
}

export async function createLaboratoryResult(
  input: CreateLaboratoryResultInput,
): Promise<ApiResponse<LaboratoryResult>> {
  const response = await apiClient.post<ApiResponse<LaboratoryResult>>(
    "/laboratory-results",
    input,
  );
  return response.data;
}

export async function releaseLaboratoryResult(
  resultId: number,
): Promise<ApiResponse<LaboratoryResult>> {
  const response = await apiClient.post<ApiResponse<LaboratoryResult>>(
    `/laboratory-results/${resultId}/release`,
  );
  return response.data;
}
