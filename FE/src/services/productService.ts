import apiClient from './api'

export interface ProductImageDTO {
  id?: number
  imageUrl: string
  isMain: boolean
}

export interface ProductVariantDTO {
  id?: number
  sku: string
  price: number
  quantity: number
  sizeId?: number
  colorId?: number
  sizeName?: string
  colorName?: string
  productId?: number
}

export interface ProductDTO {
  id: number
  name: string
  slug: string
  description: string
  categoryId: number
  categoryName?: string
  brandId: number
  brandName?: string
  active: boolean
  images?: ProductImageDTO[]
  variants?: ProductVariantDTO[]
}

export interface Page<T> {
  content: T[]
  totalPages: number
  totalElements: number
  number: number
  size: number
}

export default {
  getAllProducts(page = 0, size = 10, sortBy = 'id', direction = 'desc') {
    return apiClient.get<Page<ProductDTO>>('/products', { params: { page, size, sortBy, direction } })
  },
  getProductById(id: number) {
    return apiClient.get<ProductDTO>(`/products/${id}`)
  },
  createProduct(
    product: Omit<ProductDTO, 'id' | 'slug' | 'categoryName' | 'brandName' | 'images' | 'variants'>,
  ) {
    return apiClient.post<ProductDTO>('/products', product)
  },
  updateProduct(id: number, product: ProductDTO) {
    return apiClient.put<ProductDTO>(`/products/${id}`, product)
  },
  deleteProduct(id: number) {
    return apiClient.delete(`/products/${id}`)
  },
  searchProducts(params: {
    keyword?: string
    categoryId?: number
    brandId?: number
    minPrice?: number
    maxPrice?: number
    page?: number
    size?: number
    sortBy?: string
    direction?: string
  }) {
    return apiClient.get<Page<ProductDTO>>('/products/search', { params })
  },
}
