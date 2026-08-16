import { apiClient } from "../../api/client";
import type { ApiResponse } from "../../types/api";
import type { Patient, PatientPage } from "./types";

export async function getPatients(
  page = 0,
  size = 10,
): Promise<PatientPage> {
  const response = await apiClient.get<PatientPage>("/patients", {
    params: {
      page,
      size,
      sort: "lastName",
    },
  });

  return response.data;
}

export async function getPatient(
  patientId: number,
): Promise<ApiResponse<Patient>> {
  const response = await apiClient.get<ApiResponse<Patient>>(
    `/patients/${patientId}`,
  );

  return response.data;
}

export type CreatePatientInput = {
  medicalRecordNumber: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  sex: "MALE" | "FEMALE" | "OTHER" | "UNKNOWN";
  email?: string;
  phoneNumber?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  addressLine1?: string;
  emergencyContactName?: string;
  emergencyContactPhone?: string;
  bloodGroup?: string;
  allergies?: string;
};

export async function createPatient(
  input: CreatePatientInput,
): Promise<ApiResponse<Patient>> {
  const response = await apiClient.post<ApiResponse<Patient>>(
    "/patients",
    input,
  );
  return response.data;
}

export type UpdatePatientInput = Partial<CreatePatientInput>;

export async function updatePatient(
  patientId: number,
  input: UpdatePatientInput,
): Promise<ApiResponse<Patient>> {
  const response = await apiClient.patch<ApiResponse<Patient>>(
    `/patients/${patientId}`,
    input,
  );
  return response.data;
}

export async function deletePatient(patientId: number): Promise<void> {
  await apiClient.delete(`/patients/${patientId}`);
}
