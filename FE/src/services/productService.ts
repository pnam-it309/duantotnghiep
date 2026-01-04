import apiClient from './api';

export interface ProductImageDTO {
  id?: number;
  imageUrl: string;
  isMain: boolean;
}

export interface ProductVariantDTO {
  id?: number;
  sku: string;
  price: number;
  quantity: number;
  sizeId?: number;
  colorId?: number;
  sizeName?: string;
  colorName?: string;
  productId?: number;
}

export interface ProductDTO {
  id: number;
  name: string;
  slug: string;
  description: string;
  categoryId: number;
  categoryName?: string;
  brandId: number;
  brandName?: string;
  active: boolean;
  images?: ProductImageDTO[];
  variants?: ProductVariantDTO[];
}

export default {
  getAllProducts() {
    return apiClient.get<ProductDTO[]>('/products');
  },
  getProductById(id: number) {
    return apiClient.get<ProductDTO>(`/products/${id}`);
  },
  createProduct(product: Omit<ProductDTO, 'id' | 'slug' | 'categoryName' | 'brandName' | 'images' | 'variants'>) {
    return apiClient.post<ProductDTO>('/products', product);
  },
  updateProduct(id: number, product: ProductDTO) {
    return apiClient.put<ProductDTO>(`/products/${id}`, product);
  },
  deleteProduct(id: number) {
    return apiClient.delete(`/products/${id}`);
  },
};
