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

type AuthenticatedRequestConfig =
  InternalAxiosRequestConfig & {
    _retry?: boolean;
  };

type RefreshResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
};

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

let refreshPromise: Promise<string> | null = null;

function isAuthEndpoint(url?: string): boolean {
  if (!url) {
    return false;
  }

  return (
    url.includes("/auth/login") ||
    url.includes("/auth/refresh") ||
    url.includes("/auth/logout")
  );
}

async function refreshAccessToken(): Promise<string> {
  const refreshToken = getRefreshToken();

  if (!refreshToken) {
    throw new Error("No refresh token available.");
  }

  /*
   * IMPORTANT:
   * Use plain axios here rather than apiClient.
   *
   * The refresh request must not pass through the normal
   * authenticated request / 401 refresh interceptor.
   */
  const response = await axios.post<RefreshResponse>(
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

  const {
    accessToken,
    refreshToken: rotatedRefreshToken,
  } = response.data;

  if (!accessToken || !rotatedRefreshToken) {
    throw new Error(
      "Refresh response did not contain the required tokens.",
    );
  }

  updateAccessToken(accessToken);

  localStorage.setItem(
    "medcore.refreshToken",
    rotatedRefreshToken,
  );

  return accessToken;
}

apiClient.interceptors.request.use(
  (
    config: InternalAxiosRequestConfig,
  ) => {
    const token = getAccessToken();

    /*
     * Do not attach an old access token to auth endpoints.
     */
    if (token && !isAuthEndpoint(config.url)) {
      config.headers.Authorization = `Bearer ${token}`;
    } else if (config.headers.Authorization) {
      delete config.headers.Authorization;
    }

    return config;
  },
);

apiClient.interceptors.response.use(
  (response) => response,

  async (error: AxiosError) => {
    const originalRequest =
      error.config as AuthenticatedRequestConfig | undefined;

    /*
     * Only protected requests should trigger refresh.
     */
    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      isAuthEndpoint(originalRequest.url)
    ) {
      return Promise.reject(error);
    }

    /*
     * Prevent infinite retry loops.
     */
    originalRequest._retry = true;

    try {
      /*
       * Multiple requests can receive 401 at exactly the same time.
       *
       * Only one refresh request is allowed to run.
       * All other requests wait for that same promise.
       */
      refreshPromise ??= refreshAccessToken().finally(() => {
        refreshPromise = null;
      });

      const newAccessToken =
        await refreshPromise;

      originalRequest.headers.Authorization =
        `Bearer ${newAccessToken}`;

      return apiClient(originalRequest);
    } catch (refreshError) {
      /*
       * The refresh token is no longer usable.
       * Remove the complete client session.
       */
      clearSession();

      /*
       * Avoid repeatedly redirecting if the user
       * is already on the login page.
       */
      if (
        window.location.pathname !== "/login"
      ) {
        window.location.replace("/login");
      }

      return Promise.reject(refreshError);
    }
  },
);