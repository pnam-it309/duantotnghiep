<script setup lang="ts">
import { useCartStore } from '../../stores/cartStore';
import { useRouter } from 'vue-router';
import { ref } from 'vue';
import api from '../../services/api';

const cartStore = useCartStore();
const router = useRouter();

const couponCodeInput = ref('');
const isValidating = ref(false);
const couponMessage = ref('');
const isError = ref(false);

const handleApplyCoupon = async () => {
    if (!couponCodeInput.value) return;

    isValidating.value = true;
    couponMessage.value = '';
    isError.value = false;

    try {
        const res = await api.get('/coupons/validate', {
            params: {
                code: couponCodeInput.value,
                orderValue: cartStore.subtotal
            }
        });

        if (res.data.valid) {
            cartStore.applyCoupon(couponCodeInput.value, res.data.coupon.id, res.data.discountAmount);
            couponMessage.value = `Coupon applied! Saved $${res.data.discountAmount}`;
            couponCodeInput.value = '';
        }
    } catch (e: any) {
        isError.value = true;
        couponMessage.value = e.response?.data?.message || 'Invalid coupon';
    } finally {
        isValidating.value = false;
    }
};

const checkout = async () => {
    const userId = localStorage.getItem('userId');
    if (!userId) {
        router.push('/login?redirect=/cart');
        return;
    }
    router.push('/checkout');
};

</script>

<template>
    <div class="cart-view container">
        <h1>Shopping Cart</h1>

        <div v-if="cartStore.items.length === 0" class="empty-cart">
            <p>Your cart is empty.</p>
            <router-link to="/products" class="btn-primary">Browse Products</router-link>
        </div>

        <div v-else class="cart-content">
            <div class="cart-items">
                <div v-for="item in cartStore.items" :key="item.variantId" class="cart-item">
                    <div class="item-info">
                        <h3>{{ item.productName }}</h3>
                        <p class="variant">{{ item.variantName }} ({{ item.sku }})</p>
                    </div>
                    <div class="item-price">${{ item.price.toLocaleString() }}</div>
                    <div class="item-qty">
                        <button @click="cartStore.updateQuantity(item.variantId, item.quantity - 1)">-</button>
                        <span>{{ item.quantity }}</span>
                        <button @click="cartStore.updateQuantity(item.variantId, item.quantity + 1)">+</button>
                    </div>
                    <div class="item-total">${{ (item.price * item.quantity).toLocaleString() }}</div>
                    <button class="btn-remove" @click="cartStore.removeItem(item.variantId)">×</button>
                </div>
            </div>

            <div class="cart-summary">
                <h2>Summary</h2>
                <div class="summary-row">
                    <span>Subtotal</span>
                    <span>${{ cartStore.subtotal.toLocaleString() }}</span>
                </div>

                <div class="coupon-section">
                    <div v-if="cartStore.couponCode" class="applied-coupon">
                        <span>Coupon: {{ cartStore.couponCode }}</span>
                        <button @click="cartStore.removeCoupon" class="btn-remove-coupon">Remove</button>
                    </div>
                    <div v-else class="coupon-input">
                        <input v-model="couponCodeInput" placeholder="Enter code" />
                        <button @click="handleApplyCoupon" :disabled="isValidating">
                            {{ isValidating ? '...' : 'Apply' }}
                        </button>
                    </div>
                    <div v-if="couponMessage" class="coupon-message" :class="{ error: isError }">
                        {{ couponMessage }}
                    </div>
                </div>

                <div v-if="cartStore.discountAmount > 0" class="summary-row discount">
                    <span>Discount</span>
                    <span>-${{ cartStore.discountAmount.toLocaleString() }}</span>
                </div>

                <div class="summary-row total">
                    <span>Total</span>
                    <span>${{ cartStore.totalAmount.toLocaleString() }}</span>
                </div>
                <button class="btn-checkout" @click="checkout">Proceed to Checkout</button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.cart-view {
    padding: 3rem 1rem;
}

.empty-cart {
    text-align: center;
    padding: 4rem;
    color: #6b7280;
}

.btn-primary {
    display: inline-block;
    background: var(--color-primary);
    color: white;
    padding: 0.8rem 1.5rem;
    border-radius: 6px;
    text-decoration: none;
    margin-top: 1rem;
}

.cart-content {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 2rem;
    margin-top: 2rem;
}

.cart-item {
    display: flex;
    align-items: center;
    background: white;
    padding: 1rem;
    border-radius: 8px;
    margin-bottom: 1rem;
    border: 1px solid #e5e7eb;
}

.item-info {
    flex: 2;
}

.item-info h3 {
    margin: 0;
    font-size: 1.1rem;
}

.variant {
    color: #6b7280;
    font-size: 0.9rem;
    margin: 0;
}

.item-price {
    flex: 1;
    text-align: right;
    padding-right: 1rem;
    color: #374151;
}

.item-qty {
    display: flex;
    align-items: center;
    margin: 0 1rem;
    border: 1px solid #d1d5db;
    border-radius: 4px;
}

.item-qty button {
    width: 25px;
    border: none;
    background: none;
    cursor: pointer;
}

.item-qty span {
    padding: 0 0.5rem;
    font-weight: bold;
}

.item-total {
    flex: 1;
    text-align: right;
    font-weight: bold;
    color: var(--color-primary);
}

.btn-remove {
    background: none;
    border: none;
    color: #ef4444;
    font-size: 1.5rem;
    cursor: pointer;
    margin-left: 1rem;
}

.cart-summary {
    background: white;
    padding: 1.5rem;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    height: fit-content;
}

.summary-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1rem;
    color: #374151;
}

.summary-row.total {
    font-size: 1.25rem;
    font-weight: 800;
    color: #111827;
    border-top: 1px solid #e5e7eb;
    padding-top: 1rem;
}

.btn-checkout {
    width: 100%;
    background: var(--color-primary);
    color: white;
    border: none;
    padding: 1rem;
    border-radius: 6px;
    font-size: 1.1rem;
    font-weight: bold;
    cursor: pointer;
    margin-top: 1rem;
}

.btn-checkout:hover {
    background: var(--color-primary-hover);
}

@media (max-width: 768px) {
    .cart-content {
        grid-template-columns: 1fr;
    }
}
</style>
