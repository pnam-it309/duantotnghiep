<script setup lang="ts">
import { type CartItem } from '../stores/cartStore';

defineProps<{
    items: CartItem[];
    subtotal: number;
    discount?: number;
    isSubmitting: boolean;
}>();

const emit = defineEmits<{ (e: 'placeOrder'): void; }>();
</script>

<template>
    <div class="summary-section">
        <h2>Order Summary</h2>
        <div class="summary-items">
            <div v-for="item in items" :key="item.variantId" class="summary-item">
                <span>{{ item.productName }} x {{ item.quantity }}</span>
                <span>${{ (item.price * item.quantity).toLocaleString() }}</span>
            </div>
        </div>
        <div class="total-row subtotal">
            <span>Subtotal</span>
            <span>${{ subtotal.toLocaleString() }}</span>
        </div>
        <div v-if="(discount || 0) > 0" class="total-row discount">
            <span>Discount</span>
            <span>-${{ (discount || 0).toLocaleString() }}</span>
        </div>
        <div class="total-row final">
            <span>Total</span>
            <span>${{ (subtotal - (discount || 0)).toLocaleString() }}</span>
        </div>

        <button @click="emit('placeOrder')" :disabled="isSubmitting" class="btn-place-order">
            {{ isSubmitting ? 'Placing Order...' : 'Place Order' }}
        </button>
    </div>
</template>

<style scoped>
.summary-section {
    background: white;
    padding: 2rem;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
}

.summary-items {
    margin-bottom: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #e5e7eb;
}

.summary-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
    color: #6b7280;
    font-size: 0.9rem;
}

.total-row {
    display: flex;
    justify-content: space-between;
    font-size: 1.25rem;
    font-weight: bold;
    margin-bottom: 1.5rem;
}

.btn-place-order {
    width: 100%;
    padding: 1rem;
    background: var(--color-primary);
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 1.1rem;
    font-weight: bold;
    cursor: pointer;
}

.btn-place-order:disabled {
    background: #a5b4fc;
}
</style>
