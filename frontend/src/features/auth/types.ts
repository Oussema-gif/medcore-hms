export type UserSummary = {
  id: number;
  username: string;
  email: string;
  displayName: string | null;
  roles: string[];
};

export type LoginResponse = {
  tokens: {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
  };
  user: UserSummary;
};

export type RefreshResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
};