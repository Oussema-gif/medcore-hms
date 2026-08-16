import { apiClient } from "../../api/client";
import {
  getRefreshToken,
  setCurrentUser,
  setSession,
  updateAccessToken,
} from "./session";
import type {
  LoginResponse,
  RefreshResponse,
  UserSummary,
} from "./types";

export async function login(
  usernameOrEmail: string,
  password: string,
): Promise<LoginResponse> {
  const response = await apiClient.post<LoginResponse>(
    "/auth/login",
    {
      usernameOrEmail,
      password,
    },
  );

  setSession(
    response.data.tokens.accessToken,
    response.data.tokens.refreshToken,
    response.data.user,
  );

  return response.data;
}

export async function refreshSession(): Promise<RefreshResponse> {
  const refreshToken = getRefreshToken();

  if (!refreshToken) {
    throw new Error("No refresh token available.");
  }

  const response = await apiClient.post<RefreshResponse>(
    "/auth/refresh",
    {
      refreshToken,
    },
  );

  updateAccessToken(response.data.accessToken);

  /*
   * Refresh-token rotation is enabled by the backend,
   * so always persist the replacement refresh token.
   */
  localStorage.setItem(
    "medcore.refreshToken",
    response.data.refreshToken,
  );

  return response.data;
}

export async function getAuthenticatedUser(): Promise<UserSummary> {
  const response = await apiClient.get<UserSummary>("/users/me");

  setCurrentUser(response.data);

  return response.data;
}