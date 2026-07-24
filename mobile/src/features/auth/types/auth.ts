export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponse = {
  tokenType: 'Bearer';
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
};

export type RegisterRequest = {
  fullName: string;
  email: string;
  phoneNumber: string;
  password: string;
};

export type RegisterResponse = {
  userId: string;
};

export type StoredSession = LoginResponse & {
  expiresAt: number;
};

export type ApiProblem = {
  status?: number;
  code?: string;
  detail?: string;
  errors?: Record<string, string>;
};
