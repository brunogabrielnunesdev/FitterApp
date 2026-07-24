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

export type ApiProblem = {
  status?: number;
  code?: string;
  detail?: string;
};
