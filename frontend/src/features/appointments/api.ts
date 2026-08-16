import { apiClient } from "../../api/client";
import type { ApiResponse } from "../../types/api";
import type {
  Appointment,
  AppointmentPage,
  CreateAppointmentInput,
} from "./types";

export async function getPatientAppointments(
  patientId: number,
  page = 0,
  size = 10,
): Promise<AppointmentPage> {
  const response = await apiClient.get<AppointmentPage>(
    `/appointments/patient/${patientId}`,
    {
      params: {
        page,
        size,
        sort: "scheduledStartAt,asc",
      },
    },
  );

  return response.data;
}

export async function getDoctorAppointments(
  doctorId: number,
  page = 0,
  size = 20,
): Promise<AppointmentPage> {
  const response = await apiClient.get<AppointmentPage>(
    `/appointments/doctor/${doctorId}`,
    {
      params: {
        page,
        size,
        sort: "scheduledStartAt,asc",
      },
    },
  );

  return response.data;
}

export async function getAllAppointments(
  page = 0,
  size = 20,
): Promise<AppointmentPage> {
  const response = await apiClient.get<AppointmentPage>("/appointments", {
    params: {
      page,
      size,
      sort: "scheduledStartAt,asc",
    },
  });
  return response.data;
}

export async function getAppointment(
  appointmentId: number,
): Promise<ApiResponse<Appointment>> {
  const response = await apiClient.get<ApiResponse<Appointment>>(
    `/appointments/${appointmentId}`,
  );
  return response.data;
}

export async function createAppointment(
  input: CreateAppointmentInput,
): Promise<ApiResponse<Appointment>> {
  const response = await apiClient.post<ApiResponse<Appointment>>(
    "/appointments",
    input,
  );

  return response.data;
}

export async function checkInAppointment(
  appointmentId: number,
): Promise<ApiResponse<Appointment>> {
  const response = await apiClient.patch<ApiResponse<Appointment>>(
    `/appointments/${appointmentId}/check-in`,
  );

  return response.data;
}

export async function startAppointment(
  appointmentId: number,
): Promise<ApiResponse<Appointment>> {
  const response = await apiClient.patch<ApiResponse<Appointment>>(
    `/appointments/${appointmentId}/start`,
  );

  return response.data;
}

export async function completeAppointment(
  appointmentId: number,
): Promise<ApiResponse<Appointment>> {
  const response = await apiClient.patch<ApiResponse<Appointment>>(
    `/appointments/${appointmentId}/complete`,
  );

  return response.data;
}

export async function cancelAppointment(
  appointmentId: number,
  cancellationReason: string,
): Promise<ApiResponse<Appointment>> {
  const response = await apiClient.patch<ApiResponse<Appointment>>(
    `/appointments/${appointmentId}/cancel`,
    { cancellationReason },
  );

  return response.data;
}
