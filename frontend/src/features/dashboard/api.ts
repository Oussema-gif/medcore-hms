import { apiClient } from "../../api/client";
import type { ApiResponse } from "../../types/api";

export type DashboardSummary = {
  activePatients: number;
  todayAppointments: number;
  pendingLaboratoryWork: number;
  systemStatus: string;
};

export async function getDashboardSummary(): Promise<DashboardSummary> {
  const response = await apiClient.get<ApiResponse<DashboardSummary>>(
    "/dashboard/summary",
  );

  return response.data.data;
}