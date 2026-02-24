<script setup lang="ts">
import { useCartStore } from '../../stores/cartStore';
import { useRouter } from 'vue-router';
import { ref, onMounted, computed } from 'vue';
import orderService from '../../services/orderService';
import userService from '../../services/userService';
import api from '../../services/api';
import CheckoutForm from '../../components/CheckoutForm.vue';
import OrderSummary from '../../components/OrderSummary.vue';

const cartStore = useCartStore();
const router = useRouter();

const shippingAddress = ref('');
const phoneNumber = ref('');
const paymentMethod = ref('COD');
const isSubmitting = ref(false);
const isLoadingProfile = ref(true);

const subtotal = computed(() => cartStore.totalAmount);

const loadProfile = async () => {
    const userId = localStorage.getItem('userId');
    if (!userId) {
        router.push('/login?redirect=/checkout');
        return;
    }

    try {
        const res = await userService.getUserById(Number(userId));
        if (res.data) {
            shippingAddress.value = res.data.address || '';
            phoneNumber.value = res.data.phoneNumber || '';
        }
    } catch (e) {
        console.error("Failed to load profile", e);
    } finally {
        isLoadingProfile.value = false;
    }
};

const placeOrder = async () => {
    if (!shippingAddress.value || !phoneNumber.value) {
        alert("Please provide shipping address and phone number.");
        return;
    }

    const userId = localStorage.getItem('userId');
    if (!userId) return;

    isSubmitting.value = true;
    try {
        const orderPayload: any = {
            user: { id: parseInt(userId) },
            subtotal: subtotal.value,
            discountTotal: cartStore.discountAmount,
            finalTotal: cartStore.totalAmount,
            status: 'PENDING',
            shippingAddress: shippingAddress.value,
            paymentMethod: paymentMethod.value,
            phoneNumber: phoneNumber.value
        };

        if (cartStore.couponId) {
            orderPayload.coupon = { id: cartStore.couponId };
        }

        const itemsPayload = cartStore.items.map(item => ({
            productVariant: { id: item.variantId },
            quantity: item.quantity,
            price: item.price
        }));

        const res = await orderService.createOrder({ order: orderPayload, items: itemsPayload });

        // Handle Payment Redirect
        if (paymentMethod.value === 'BANK_TRANSFER' || paymentMethod.value === 'VNPAY') {
            // Call payment API to separate payment Service if needed or use what we built
            // For now assume logic:
            const paymentRes = await api.post('/payment/create-payment', {
                amount: cartStore.totalAmount,
                orderInfo: `Order #${res.data.id}`
            });
            if (paymentRes.data.status === 'OK') {
                window.location.href = paymentRes.data.url;
                cartStore.clearCart();
                return;
            }
        }

        cartStore.clearCart();
        alert("Order placed successfully!");
        router.push('/orders');
    } catch (e) {
        console.error("Checkout failed", e);
        alert("Checkout failed. Please try again.");
    } finally {
        isSubmitting.value = false;
    }
};

onMounted(() => {
    if (cartStore.items.length === 0) {
        router.push('/cart');
        return;
    }
    loadProfile();
});
</script>

<template>
    <div class="checkout-view container">
        <h1>Checkout</h1>

        <div class="checkout-grid">
            <CheckoutForm v-model:phoneNumber="phoneNumber" v-model:shippingAddress="shippingAddress"
                v-model:paymentMethod="paymentMethod" :isLoadingProfile="isLoadingProfile" />

            <OrderSummary :items="cartStore.items" :subtotal="subtotal" :discount="cartStore.discountAmount"
                :isSubmitting="isSubmitting" @placeOrder="placeOrder" />
        </div>
    </div>
</template>

<style scoped>
.checkout-view {
    padding: 3rem 1rem;
}

.checkout-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 2rem;
    margin-top: 2rem;
}

@media (max-width: 768px) {
    .checkout-grid {
        grid-template-columns: 1fr;
    }
}
</style>
