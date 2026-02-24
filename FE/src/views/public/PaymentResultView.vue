<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const isSuccess = ref(false);
const message = ref('Processing payment...');

onMounted(() => {
    const responseCode = route.query.vnp_ResponseCode;
    if (responseCode === '00') {
        isSuccess.value = true;
        message.value = 'Payment Successful! Thank you for your purchase.';
    } else {
        isSuccess.value = false;
        message.value = 'Payment Failed. Please try again or check your balance.';
    }
});
</script>

<template>
    <div class="container payment-result">
        <div class="result-card" :class="{ success: isSuccess, error: !isSuccess }">
            <h1 v-if="isSuccess">✅ Successful</h1>
            <h1 v-else>❌ Failed</h1>
            <p>{{ message }}</p>
            <div class="actions">
                <router-link to="/orders" class="btn">View Orders</router-link>
                <router-link to="/" class="btn btn-secondary">Continue Shopping</router-link>
            </div>
        </div>
    </div>
</template>

<style scoped>
.payment-result {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 60vh;
}

.result-card {
    background: white;
    padding: 3rem;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    max-width: 500px;
    width: 100%;
}

.result-card.success h1 {
    color: #10b981;
}

.result-card.error h1 {
    color: #ef4444;
}

.actions {
    margin-top: 2rem;
    display: flex;
    gap: 1rem;
    justify-content: center;
}

.btn {
    padding: 0.75rem 1.5rem;
    border-radius: 6px;
    text-decoration: none;
    font-weight: bold;
    background: var(--color-primary);
    color: white;
}

.btn-secondary {
    background: #e5e7eb;
    color: #374151;
}
</style>
