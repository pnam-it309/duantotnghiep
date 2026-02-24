<script setup lang="ts">
import { ref, onMounted } from 'vue';
import orderService, { type OrderDTO } from '../../services/orderService';
import { useRouter } from 'vue-router';

const router = useRouter();
const orders = ref<OrderDTO[]>([]);
const isLoading = ref(true);

const loadOrders = async () => {
    const userId = localStorage.getItem('userId');
    if (!userId) {
        router.push('/login');
        return;
    }

    isLoading.value = true;
    try {
        const res = await orderService.getOrdersByUser(Number(userId));
        // Sort by id desc (newest first - approximating createdAt if needed)
        orders.value = res.data.sort((a, b) => b.id - a.id);
    } catch (e) {
        console.error("Failed to load orders", e);
    } finally {
        isLoading.value = false;
    }
};

const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString() + ' ' + new Date(dateString).toLocaleTimeString();
};

const getStatusColor = (status: string) => {
    switch (status) {
        case 'PENDING': return 'bg-yellow-100 text-yellow-800';
        case 'SHIPPING': return 'bg-blue-100 text-blue-800';
        case 'DELIVERED': return 'bg-green-100 text-green-800';
        case 'CANCELLED': return 'bg-red-100 text-red-800';
        default: return 'bg-gray-100 text-gray-800';
    }
};

onMounted(loadOrders);
</script>

<template>
    <div class="orders-view container">
        <h1>My Order History</h1>

        <div v-if="isLoading" class="loading">Loading orders...</div>

        <div v-else-if="orders.length === 0" class="no-orders">
            <p>You haven't placed any orders yet.</p>
            <router-link to="/products" class="btn-shop">Start Shopping</router-link>
        </div>

        <div v-else class="orders-list">
            <div v-for="order in orders" :key="order.id" class="order-card">
                <div class="order-header">
                    <div class="order-id">Order #{{ order.id }}</div>
                    <div class="order-date">{{ formatDate(order.createdAt) }}</div>
                </div>

                <div class="order-details">
                    <div class="detail-row">
                        <span>Status:</span>
                        <span class="status-badge" :class="getStatusColor(order.status)">{{ order.status }}</span>
                    </div>
                    <div class="detail-row">
                        <span>Items:</span>
                        <span>{{ order.items ? order.items.length : 0 }} items</span>
                    </div>
                    <div class="detail-row total">
                        <span>Total:</span>
                        <span>${{ order.finalTotal.toLocaleString() }}</span>
                    </div>
                </div>

                <!-- Could add 'View Details' button here linking to dedicated detail page if needed -->
            </div>
        </div>
    </div>
</template>

<style scoped>
.orders-view {
    padding: 3rem 1rem;
    max-width: 800px;
}

.no-orders {
    text-align: center;
    padding: 4rem;
    color: #6b7280;
}

.orders-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.order-card {
    background: white;
    padding: 1.5rem;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
}

.order-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #f3f4f6;
}

.order-id {
    font-weight: bold;
    font-size: 1.1rem;
}

.order-date {
    color: #6b7280;
}

.detail-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
}

.detail-row.total {
    margin-top: 1rem;
    font-weight: bold;
    font-size: 1.1rem;
    color: var(--color-primary);
}

.status-badge {
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 0.9rem;
    font-weight: 500;
}

.bg-yellow-100 {
    background: #fef3c7;
    color: #92400e;
}

.bg-blue-100 {
    background: #dbeafe;
    color: #1e40af;
}

.bg-green-100 {
    background: #d1fae5;
    color: #065f46;
}

.bg-red-100 {
    background: #fee2e2;
    color: #991b1b;
}

.bg-gray-100 {
    background: #f3f4f6;
    color: #374151;
}

.btn-shop {
    display: inline-block;
    background: var(--color-primary);
    color: white;
    padding: 0.8rem 1.5rem;
    border-radius: 6px;
    text-decoration: none;
    margin-top: 1rem;
}
</style>
