import { apiClient } from "../../api/client";
import type { ApiResponse, PagedApiResponse } from "../../types/api";

export type Notification = {
  id: number;
  notificationType: string;
  title: string;
  body: string;
  resourceType: string | null;
  resourceId: number | null;
  readAt: string | null;
  createdAt: string;
};

export async function getNotifications(
  page = 0,
  size = 20,
): Promise<PagedApiResponse<Notification>> {
  const response = await apiClient.get<PagedApiResponse<Notification>>(
    "/notifications",
    {
      params: { page, size },
    },
  );

  return response.data;
}

export async function markNotificationAsRead(
  notificationId: number,
): Promise<ApiResponse<Notification>> {
  const response = await apiClient.patch<ApiResponse<Notification>>(
    `/notifications/${notificationId}/read`,
  );

  return response.data;
}
