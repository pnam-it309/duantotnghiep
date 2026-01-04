import apiClient from './api';

export interface DiscountDTO {
  id: number;
  productId: number;
  productName?: string;
  discountPercent: number;
  startDate: string; // ISO DateTime
  endDate: string; // ISO DateTime
  active: boolean;
}

export default {
  getAllDiscounts() {
    return apiClient.get<DiscountDTO[]>('/discounts');
  },
  getDiscountsByProduct(productId: number) {
    return apiClient.get<DiscountDTO[]>(`/discounts/product/${productId}`);
  },
  getDiscountById(id: number) {
    return apiClient.get<DiscountDTO>(`/discounts/${id}`);
  },
  createDiscount(discount: Omit<DiscountDTO, 'id' | 'productName'>) {
    return apiClient.post<DiscountDTO>('/discounts', discount);
  },
  updateDiscount(id: number, discount: DiscountDTO) {
    return apiClient.put<DiscountDTO>(`/discounts/${id}`, discount);
  },
  deleteDiscount(id: number) {
    return apiClient.delete(`/discounts/${id}`);
  },
};
