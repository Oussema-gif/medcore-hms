import type { UserSummary } from "./types";

const REFRESH_TOKEN_KEY = "medcore.refreshToken";
const USER_KEY = "medcore.user";

let accessToken: string | null = null;
let currentUser: UserSummary | null = null;

export function setSession(
  token: string,
  refreshToken: string,
  user: UserSummary,
): void {
  accessToken = token;
  currentUser = user;

  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function updateAccessToken(token: string): void {
  accessToken = token;
}

export function getAccessToken(): string | null {
  return accessToken;
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function getStoredUser(): UserSummary | null {
  const raw = localStorage.getItem(USER_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as UserSummary;
  } catch {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}

export function setCurrentUser(user: UserSummary): void {
  currentUser = user;
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function getCurrentUser(): UserSummary | null {
  return currentUser ?? getStoredUser();
}

export function clearSession(): void {
  accessToken = null;
  currentUser = null;

  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}