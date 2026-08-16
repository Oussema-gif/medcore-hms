import { apiClient } from "../../api/client";
import type {
  ApiResponse,
  PagedApiResponse,
} from "../../types/api";

export type MedicalRecord = {
  id: number;
  patientId: number;
  doctorId: number;
  appointmentId: number | null;
  encounterAt: string;
  symptoms: string | null;
  clinicalNotes: string;
  patientVisible: boolean;
  releasedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type CreateMedicalRecordInput = {
  patientId: number;
  doctorId: number;
  appointmentId?: number;
  encounterAt: string;
  symptoms?: string;
  clinicalNotes: string;
};

export async function getMedicalRecordsByPatientId(
  patientId: number,
): Promise<PagedApiResponse<MedicalRecord>> {
  const response = await apiClient.get<
    PagedApiResponse<MedicalRecord>
  >(`/medical-records/patient/${patientId}`, {
    params: {
      page: 0,
      size: 20,
      sort: "encounterAt,desc",
    },
  });

  return response.data;
}

export async function createMedicalRecord(
  input: CreateMedicalRecordInput,
): Promise<ApiResponse<MedicalRecord>> {
  const response = await apiClient.post<ApiResponse<MedicalRecord>>(
    "/medical-records",
    input,
  );

  return response.data;
}
