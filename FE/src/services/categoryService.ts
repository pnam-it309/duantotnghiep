import apiClient from './api';

export interface CategoryDTO {
  id: number;
  name: string;
}

export default {
  getAllCategories() {
    return apiClient.get<CategoryDTO[]>('/categories');
  },
  getCategoryById(id: number) {
    return apiClient.get<CategoryDTO>(`/categories/${id}`);
  },
  createCategory(category: Omit<CategoryDTO, 'id'>) {
    return apiClient.post<CategoryDTO>('/categories', category);
  },
  updateCategory(id: number, category: CategoryDTO) {
    return apiClient.put<CategoryDTO>(`/categories/${id}`, category);
  },
  deleteCategory(id: number) {
    return apiClient.delete(`/categories/${id}`);
  },
};
