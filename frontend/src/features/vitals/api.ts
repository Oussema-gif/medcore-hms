import { apiClient } from "../../api/client";
import type {
  ApiResponse,
  PagedApiResponse,
} from "../../types/api";

export type VitalSign = {
  id: number;
  medicalRecordId: number;
  recordedAt: string;
  temperatureCelsius: number | null;
  systolicBp: number | null;
  diastolicBp: number | null;
  heartRate: number | null;
  respiratoryRate: number | null;
  oxygenSaturation: number | null;
  weightKg: number | null;
  heightCm: number | null;
  createdAt: string;
  updatedAt: string;
};

export type CreateVitalSignInput = {
  medicalRecordId: number;
  recordedAt: string;
  temperatureCelsius?: number;
  systolicBp?: number;
  diastolicBp?: number;
  heartRate?: number;
  respiratoryRate?: number;
  oxygenSaturation?: number;
  weightKg?: number;
  heightCm?: number;
};

export async function getVitals(
  medicalRecordId: number,
): Promise<PagedApiResponse<VitalSign>> {
  const response = await apiClient.get<
    PagedApiResponse<VitalSign>
  >(`/vital-signs/medical-record/${medicalRecordId}`, {
    params: {
      page: 0,
      size: 20,
      sort: "recordedAt,desc",
    },
  });

  return response.data;
}

export async function createVitalSign(
  input: CreateVitalSignInput,
): Promise<ApiResponse<VitalSign>> {
  const response = await apiClient.post<ApiResponse<VitalSign>>(
    "/vital-signs",
    input,
  );

  return response.data;
}
