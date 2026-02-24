import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface ComparisonProduct {
  id: number
  name: string
  price: number
  image: string
  rating: number
  brandName?: string
  categoryName?: string
  // Add other specs
}

export const useComparisonStore = defineStore('comparison', () => {
  const products = ref<ComparisonProduct[]>([])

  // Load from local storage
  const stored = localStorage.getItem('comparison_products')
  if (stored) {
    products.value = JSON.parse(stored)
  }

  const addProduct = (product: ComparisonProduct) => {
    if (products.value.some((p) => p.id === product.id)) {
      alert('Sản phẩm đã có trong danh sách so sánh')
      return
    }

    if (products.value.length >= 4) {
      alert('Chỉ có thể so sánh tối đa 4 sản phẩm')
      return
    }

    products.value.push(product)
    saveToStorage()
  }

  const removeProduct = (id: number) => {
    products.value = products.value.filter((p) => p.id !== id)
    saveToStorage()
  }

  const clear = () => {
    products.value = []
    saveToStorage()
  }

  const saveToStorage = () => {
    localStorage.setItem('comparison_products', JSON.stringify(products.value))
  }

  return {
    products,
    addProduct,
    removeProduct,
    clear,
  }
})
