import apiClient from './api';

export interface ColorDTO {
  id: number;
  colorName: string;
  hexCode: string;
}

export default {
  getAllColors() {
    return apiClient.get<ColorDTO[]>('/colors');
  },
  getColorById(id: number) {
    return apiClient.get<ColorDTO>(`/colors/${id}`);
  },
  createColor(color: Omit<ColorDTO, 'id'>) {
    return apiClient.post<ColorDTO>('/colors', color);
  },
  updateColor(id: number, color: ColorDTO) {
    return apiClient.put<ColorDTO>(`/colors/${id}`, color);
  },
  deleteColor(id: number) {
    return apiClient.delete(`/colors/${id}`);
  },
};
