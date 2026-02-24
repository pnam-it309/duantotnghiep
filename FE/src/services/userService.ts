import apiClient from './api'

import type { Page } from './productService'

export interface UserDTO {
  id: number
  username: string
  email: string
  rewardPoints: number
  membershipTier: string
  password?: string
  fullName?: string
  phoneNumber?: string
  address?: string
  role?: string
}

export default {
  getAllUsers(page = 0, size = 10, sortBy = 'id', direction = 'desc') {
    return apiClient.get<Page<UserDTO>>('/users', { params: { page, size, sortBy, direction } })
  },
  searchUsers(params: {
    keyword?: string
    role?: string
    page?: number
    size?: number
    sortBy?: string
    direction?: string
  }) {
    return apiClient.get<Page<UserDTO>>('/users/search', { params })
  },
  getUserById(id: number) {
    return apiClient.get<UserDTO>(`/users/${id}`)
  },
  getUserByUsername(username: string) {
    return apiClient.get<UserDTO>(`/users/username/${username}`)
  },
  createUser(user: Omit<UserDTO, 'id'>) {
    return apiClient.post<UserDTO>('/users', user)
  },
  updateUser(id: number, user: UserDTO) {
    return apiClient.put<UserDTO>(`/users/${id}`, user)
  },
  updateProfile(id: number, user: Partial<UserDTO>) {
    return apiClient.put<UserDTO>(`/users/${id}`, user)
  },
  deleteUser(id: number) {
    return apiClient.delete(`/users/${id}`)
  },
  sendSMS(id: number, message: string) {
    return apiClient.post(`/users/${id}/sms`, { message })
  },
}
