export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  fullName: string
}

export interface JwtResponse {
  accessToken: string
  tokenType: string
  user: any
}
