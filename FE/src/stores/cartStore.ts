import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface CartItem {
  variantId: number
  productId: number
  productName: string
  variantName: string // e.g. "Color - Size"
  sku: string
  price: number
  quantity: number
  image?: string
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const couponCode = ref<string | null>(null)
  const couponId = ref<number | null>(null)
  const discountAmount = ref<number>(0)

  const saveCart = () => {
    const state = {
      items: items.value,
      couponCode: couponCode.value,
      couponId: couponId.value,
      discountAmount: discountAmount.value,
    }
    localStorage.setItem('cart', JSON.stringify(state))
  }

  // Load from local storage on init (Compatible with old format array)
  if (localStorage.getItem('cart')) {
    try {
      const data = JSON.parse(localStorage.getItem('cart') || '[]')
      if (Array.isArray(data)) {
        items.value = data
      } else {
        items.value = data.items || []
        couponCode.value = data.couponCode || null
        couponId.value = data.couponId || null
        discountAmount.value = data.discountAmount || 0
      }
    } catch (e) {
      console.error('Failed to load cart', e)
    }
  }

  const addItem = (item: CartItem) => {
    const existing = items.value.find((i) => i.variantId === item.variantId)
    if (existing) {
      existing.quantity += item.quantity
    } else {
      items.value.push(item)
    }
    // Reset coupon on cart change to ensure validity?
    // For now keep it, backend will re-validate.
    saveCart()
  }

  const removeItem = (variantId: number) => {
    items.value = items.value.filter((i) => i.variantId !== variantId)
    saveCart()
  }

  const updateQuantity = (variantId: number, qty: number) => {
    const item = items.value.find((i) => i.variantId === variantId)
    if (item) {
      item.quantity = qty
      if (item.quantity <= 0) removeItem(variantId)
      else saveCart()
    }
  }

  const clearCart = () => {
    items.value = []
    couponCode.value = null
    couponId.value = null
    discountAmount.value = 0
    saveCart()
  }

  const applyCoupon = (code: string, id: number, discount: number) => {
    couponCode.value = code
    couponId.value = id
    discountAmount.value = discount
    saveCart()
  }

  const removeCoupon = () => {
    couponCode.value = null
    couponId.value = null
    discountAmount.value = 0
    saveCart()
  }

  const itemCount = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0))
  const subtotal = computed(() => items.value.reduce((sum, i) => sum + i.price * i.quantity, 0))
  const totalAmount = computed(() => Math.max(0, subtotal.value - discountAmount.value))

  return {
    items,
    couponCode,
    couponId,
    discountAmount,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    applyCoupon,
    removeCoupon,
    itemCount,
    subtotal,
    totalAmount,
  }
})
