import axios, {
  type AxiosError,
  type InternalAxiosRequestConfig,
} from "axios";
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  updateAccessToken,
} from "../features/auth/session";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

let refreshPromise: Promise<string> | null = null;

async function refreshAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken();

  if (!refreshToken) {
    throw new Error("No refresh token available.");
  }

  const response = await axios.post<{
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
  }>(
    `${API_BASE_URL}/auth/refresh`,
    {
      refreshToken,
    },
    {
      headers: {
        "Content-Type": "application/json",
      },
    },
  );

  updateAccessToken(response.data.accessToken);
  localStorage.setItem(
    "medcore.refreshToken",
    response.data.refreshToken,
  );

  return response.data.accessToken;
}

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken();

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
);

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      originalRequest.url?.includes("/auth/login") ||
      originalRequest.url?.includes("/auth/refresh")
    ) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      refreshPromise ??= refreshAccessToken().finally(() => {
        refreshPromise = null;
      });

      const newAccessToken = await refreshPromise;

      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

      return apiClient(originalRequest);
    } catch (refreshError) {
      clearSession();
      window.location.assign("/login");

      return Promise.reject(refreshError);
    }
  },
);