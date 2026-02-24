import apiClient from './api';

export interface SizeDTO {
  id: number;
  sizeValue: string;
}

export default {
  getAllSizes() {
    return apiClient.get<SizeDTO[]>('/sizes/all');
  },
  getSizes(params?: any) {
    return apiClient.get('/sizes', { params });
  },
  getSizeById(id: number) {
    return apiClient.get<SizeDTO>(`/sizes/${id}`);
  },
  createSize(size: Omit<SizeDTO, 'id'>) {
    return apiClient.post<SizeDTO>('/sizes', size);
  },
  updateSize(id: number, size: SizeDTO) {
    return apiClient.put<SizeDTO>(`/sizes/${id}`, size);
  },
  deleteSize(id: number) {
    return apiClient.delete(`/sizes/${id}`);
  },
};
