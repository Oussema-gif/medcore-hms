import { apiClient } from "../../api/client";
import type { ApiResponse, PagedApiResponse } from "../../types/api";

export type DiagnosisType = "PRIMARY" | "SECONDARY";

export type Diagnosis = {
  id: number;
  medicalRecordId: number;
  codeSystem: string | null;
  diagnosisCode: string | null;
  description: string;
  diagnosisType: DiagnosisType;
  primary: boolean;
  createdAt: string;
  updatedAt: string;
};

export type CreateDiagnosisInput = {
  medicalRecordId: number;
  codeSystem?: string;
  diagnosisCode?: string;
  description: string;
  diagnosisType: DiagnosisType;
  primary: boolean;
};

export async function getDiagnoses(
  medicalRecordId: number,
): Promise<PagedApiResponse<Diagnosis>> {
  const response = await apiClient.get<PagedApiResponse<Diagnosis>>(
    `/diagnoses/medical-record/${medicalRecordId}`,
    {
      params: {
        page: 0,
        size: 50,
        sort: "createdAt,desc",
      },
    },
  );

  return response.data;
}

export async function createDiagnosis(
  input: CreateDiagnosisInput,
): Promise<ApiResponse<Diagnosis>> {
  const response = await apiClient.post<ApiResponse<Diagnosis>>(
    "/diagnoses",
    input,
  );

  return response.data;
}
