import { apiClient } from "../../api/client";
import type { ApiResponse } from "../../types/api";
import type { 
  Doctor, 
  DoctorPage, 
  CreateDoctorInput, 
  UpdateDoctorInput 
} from "./types";

export async function getDoctors(
  page = 0,
  size = 10,
): Promise<DoctorPage> {
  const response = await apiClient.get<DoctorPage>("/doctors", {
    params: {
      page,
      size,
      sort: "lastName",
    },
  });

  return response.data;
}

export async function getDoctor(
  doctorId: number,
): Promise<ApiResponse<Doctor>> {
  const response = await apiClient.get<ApiResponse<Doctor>>(
    `/doctors/${doctorId}`,
  );

  return response.data;
}

export async function createDoctor(
  input: CreateDoctorInput,
): Promise<ApiResponse<Doctor>> {
  const response = await apiClient.post<ApiResponse<Doctor>>(
    "/doctors",
    input,
  );
  return response.data;
}

export async function updateDoctor(
  doctorId: number,
  input: UpdateDoctorInput,
): Promise<ApiResponse<Doctor>> {
  const response = await apiClient.patch<ApiResponse<Doctor>>(
    `/doctors/${doctorId}`,
    input,
  );
  return response.data;
}

export async function deleteDoctor(
  doctorId: number,
): Promise<void> {
  await apiClient.delete(`/doctors/${doctorId}`);
}
