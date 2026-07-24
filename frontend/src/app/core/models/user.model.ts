export type Role = 'USER' | 'ADMIN';
export type UserStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

/** Full profile of the authenticated user. */
export interface User {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  photo: string | null;
  role: Role;
  status: UserStatus;
}

/** Minimal public view of another user (search results, conversation partner). */
export interface UserSummary {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  photo: string | null;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface RegisterRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}
