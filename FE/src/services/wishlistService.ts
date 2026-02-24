import api from './api'

export interface WishlistItem {
  id: number
  userId: number
  productId: number
  productName: string
  productPrice: number
  addedAt: string
}

class WishlistService {
  async add(productId: number) {
    const response = await api.post('/wishlist', { productId })
    return response.data
  }

  async getAll() {
    const response = await api.get('/wishlist')
    return response.data
  }

  async remove(productId: number) {
    await api.delete(`/wishlist/${productId}`)
  }

  async checkInWishlist(productId: number): Promise<boolean> {
    const response = await api.get(`/wishlist/check/${productId}`)
    return response.data.inWishlist
  }

  async getCount(): Promise<number> {
    const response = await api.get('/wishlist/count')
    return response.data.count
  }
}

export default new WishlistService()
