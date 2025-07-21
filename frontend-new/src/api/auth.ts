import axios from 'axios';

// API base URL
const API_BASE_URL = 'http://localhost:8081';

// Define interfaces for auth requests/responses
export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  tenantId: number;
  role: string;
}

export interface RegisterResponse {
  id: number;
  username: string;
  email: string;
}

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  tenantId: number;
  tenantName: string;
  roleNames: string[];
  enabled: boolean;
}

// Auth API client
const authApi = {
  // Login endpoint
  login: async (credentials: AuthRequest): Promise<AuthResponse> => {
    const response = await axios.post<AuthResponse>(
      `${API_BASE_URL}/api/auth/login`,
      credentials
    );
    return response.data;
  },

  // Registration endpoint
  register: async (userData: RegisterRequest): Promise<RegisterResponse> => {
    const response = await axios.post<RegisterResponse>(
      `${API_BASE_URL}/api/auth/register`,
      userData
    );
    return response.data;
  },

  // Get current user info
  getCurrentUser: async (token: string): Promise<UserInfo> => {
    const response = await axios.get<UserInfo>(
      `${API_BASE_URL}/api/users/me`,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );
    return response.data;
  }
};

export default authApi;
