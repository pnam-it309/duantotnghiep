import apiClient from './api';

export interface BrandDTO {
  id: number;
  name: string;
}

export default {
  getAllBrands() {
    return apiClient.get<BrandDTO[]>('/brands');
  },
  getBrandById(id: number) {
    return apiClient.get<BrandDTO>(`/brands/${id}`);
  },
  createBrand(brand: Omit<BrandDTO, 'id'>) {
    return apiClient.post<BrandDTO>('/brands', brand);
  },
  updateBrand(id: number, brand: BrandDTO) {
    return apiClient.put<BrandDTO>(`/brands/${id}`, brand);
  },
  deleteBrand(id: number) {
    return apiClient.delete(`/brands/${id}`);
  },
};
