import apiClient from './api'

export interface ReviewDTO {
  id?: number
  content: string
  rating: number // 1-5
  createdAt?: string
  productId: number
  userId: number
  username?: string
  imageUrls?: string[]
  isVerifiedPurchase?: boolean
}

const reviewService = {
  getReviewsByProduct(productId: number) {
    return apiClient.get<ReviewDTO[]>(`/public/reviews/product/${productId}`)
  },

  createReview(review: ReviewDTO) {
    return apiClient.post<ReviewDTO>('/reviews', review)
  },
}

export default reviewService
