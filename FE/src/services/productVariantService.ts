import apiClient from './api';

export interface ProductVariantDTO {
  id: number;
  productId: number;
  sizeId: number;
  sizeValue?: string;
  colorId: number;
  colorName?: string;
  price: number;
  stockQuantity: number;
  sku: string;
}

export default {
  getVariantsByProduct(productId: number) {
    return apiClient.get<ProductVariantDTO[]>(`/product-variants/product/${productId}`);
  },
  getVariantById(id: number) {
    return apiClient.get<ProductVariantDTO>(`/product-variants/${id}`);
  },
  createVariant(variant: Omit<ProductVariantDTO, 'id' | 'sizeValue' | 'colorName'>) {
    return apiClient.post<ProductVariantDTO>('/product-variants', variant);
  },
  updateVariant(id: number, variant: ProductVariantDTO) {
    return apiClient.put<ProductVariantDTO>(`/product-variants/${id}`, variant);
  },
  deleteVariant(id: number) {
    return apiClient.delete(`/product-variants/${id}`);
  },
};
