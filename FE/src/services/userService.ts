import apiClient from './api';

export interface UserDTO {
  id: number;
  username: string;
  email: string;
  rewardPoints: number;
  membershipTier: string;
  password?: string; // Optional for creation/update
}

export default {
  getAllUsers() {
    return apiClient.get<UserDTO[]>('/users');
  },
  getUserById(id: number) {
    return apiClient.get<UserDTO>(`/users/${id}`);
  },
  getUserByUsername(username: string) {
    return apiClient.get<UserDTO>(`/users/username/${username}`);
  },
  createUser(user: Omit<UserDTO, 'id'>) {
    return apiClient.post<UserDTO>('/users', user);
  },
  updateUser(id: number, user: UserDTO) {
    return apiClient.put<UserDTO>(`/users/${id}`, user);
  },
  deleteUser(id: number) {
    return apiClient.delete(`/users/${id}`);
  },
};
