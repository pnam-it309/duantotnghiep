import api from './api'
import type { LoginRequest, RegisterRequest, JwtResponse } from '@/types/auth' // We'll need to define these types

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
  email: string
  fullName: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  user: any // UserDTO
}

class AuthService {
  async login(payload: LoginPayload): Promise<AuthResponse> {
    const response = await api.post('/auth/login', payload)
    return response.data
  }

  async register(payload: RegisterPayload): Promise<string> {
    const response = await api.post('/auth/register', payload)
    return response.data
  }

  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
}

export default new AuthService()
