import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export interface CartItem {
    variantId: number;
    productId: number;
    productName: string;
    variantName: string; // e.g. "Color - Size"
    sku: string;
    price: number;
    quantity: number;
    image?: string;
}

export const useCartStore = defineStore('cart', () => {
    const items = ref<CartItem[]>([]);

    // Load from local storage on init
    if (localStorage.getItem('cart')) {
        try {
            items.value = JSON.parse(localStorage.getItem('cart') || '[]');
        } catch (e) {
            console.error("Failed to load cart", e);
        }
    }

    const saveCart = () => {
        localStorage.setItem('cart', JSON.stringify(items.value));
    };

    const addItem = (item: CartItem) => {
        const existing = items.value.find(i => i.variantId === item.variantId);
        if (existing) {
            existing.quantity += item.quantity;
        } else {
            items.value.push(item);
        }
        saveCart();
    };

    const removeItem = (variantId: number) => {
        items.value = items.value.filter(i => i.variantId !== variantId);
        saveCart();
    };

    const updateQuantity = (variantId: number, qty: number) => {
        const item = items.value.find(i => i.variantId === variantId);
        if (item) {
            item.quantity = qty;
            if (item.quantity <= 0) removeItem(variantId);
            else saveCart();
        }
    };

    const clearCart = () => {
        items.value = [];
        saveCart();
    };

    const itemCount = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0));
    const totalAmount = computed(() => items.value.reduce((sum, i) => sum + i.price * i.quantity, 0));

    return {
        items,
        addItem,
        removeItem,
        updateQuantity,
        clearCart,
        itemCount,
        totalAmount
    };
});
