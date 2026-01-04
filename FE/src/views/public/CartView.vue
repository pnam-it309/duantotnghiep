<script setup lang="ts">
import { useCartStore } from '../../stores/cartStore';
import { useRouter } from 'vue-router';
import { computed } from 'vue';
import orderService from '../../services/orderService'; // You might need a public order service or allow creating orders without auth if backend supports it. Assuming backend requires known user.

const cartStore = useCartStore();
const router = useRouter();

const checkout = async () => {
    // For MVP, since we don't have real Auth, redirect to Login if no User ID in localStorage
    // Simulating Auth: Check localStorage 'userId'
    const userId = localStorage.getItem('userId');
    if (!userId) {
        router.push('/login?redirect=/cart');
        return;
    }

    // Create Order
    try {
        const orderPayload = {
            user: { id: parseInt(userId) },
            subtotal: cartStore.totalAmount,
            discountTotal: 0,
            finalTotal: cartStore.totalAmount,
            status: 'PENDING'
        };

        const itemsPayload = cartStore.items.map(item => ({
            productVariant: { id: item.variantId },
            quantity: item.quantity,
            price: item.price
        }));

        await orderService.createOrder({ order: orderPayload, items: itemsPayload });

        cartStore.clearCart();
        alert("Order placed successfully!");
        router.push('/');
    } catch (e) {
        console.error("Checkout failed", e);
        alert("Checkout failed");
    }
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
                    <span>${{ cartStore.totalAmount.toLocaleString() }}</span>
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
    background: #4f46e5;
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
    color: #4f46e5;
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
    background: #4f46e5;
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
    background: #4338ca;
}

@media (max-width: 768px) {
    .cart-content {
        grid-template-columns: 1fr;
    }
}
</style>
