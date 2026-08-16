import { apiClient } from "../../../api/client";
import type { ApiResponse } from "../../../types/api";
import type {
  AvailabilityPage,
  CreateAvailabilityInput,
  DoctorAvailability,
  UpdateAvailabilityInput,
} from "./types";

export async function getDoctorAvailability(
  doctorId: number,
): Promise<AvailabilityPage> {
  const response = await apiClient.get<AvailabilityPage>(
    `/doctor-availability/doctor/${doctorId}`,
    {
      params: {
        page: 0,
        size: 50,
        sort: "dayOfWeek",
      },
    },
  );

  return response.data;
}

export async function createDoctorAvailability(
  input: CreateAvailabilityInput,
): Promise<ApiResponse<DoctorAvailability>> {
  const response = await apiClient.post<
    ApiResponse<DoctorAvailability>
  >("/doctor-availability", input);

  return response.data;
}

export async function updateDoctorAvailability(
  availabilityId: number,
  input: UpdateAvailabilityInput,
): Promise<ApiResponse<DoctorAvailability>> {
  const response = await apiClient.patch<
    ApiResponse<DoctorAvailability>
  >(`/doctor-availability/${availabilityId}`, input);

  return response.data;
}

export async function deleteDoctorAvailability(
  availabilityId: number,
): Promise<void> {
  await apiClient.delete(
    `/doctor-availability/${availabilityId}`,
  );
}
