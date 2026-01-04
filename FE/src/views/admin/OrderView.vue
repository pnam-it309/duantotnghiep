<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import orderService, { type OrderDTO } from '../../services/orderService';

const router = useRouter();
const orders = ref<OrderDTO[]>([]);
const isLoading = ref(false);

// Filters
const searchQuery = ref('');
const statusFilter = ref('all');

const filteredOrders = computed(() => {
    return orders.value.filter(order => {
        const query = searchQuery.value.toLowerCase();
        const matchesSearch = !query ||
            order.id.toString().includes(query) ||
            (order.username && order.username.toLowerCase().includes(query));

        const matchesStatus = statusFilter.value === 'all' || order.status === statusFilter.value;

        return matchesSearch && matchesStatus;
    });
});

const fetchOrders = async () => {
    isLoading.value = true;
    try {
        const response = await orderService.getAllOrders();
        orders.value = response.data;
    } catch (error) {
        console.error('Error fetching orders:', error);
    } finally {
        isLoading.value = false;
    }
};

const viewDetails = (order: OrderDTO) => {
    router.push(`/admin/orders/${order.id}`);
};

const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this order?')) return;
    try {
        await orderService.deleteOrder(id);
        await fetchOrders();
    } catch (error) {
        console.error('Error deleting order:', error);
    }
};

onMounted(() => {
    fetchOrders();
});
</script>

<template>
    <div class="order-view">
        <div class="header">
            <h2>Orders</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search order ID or user..."
                    class="search-input" />
                <select v-model="statusFilter" class="filter-select">
                    <option value="all">All Status</option>
                    <option value="PENDING">Pending</option>
                    <option value="PROCESSING">Processing</option>
                    <option value="SHIPPED">Shipped</option>
                    <option value="DELIVERED">Delivered</option>
                    <option value="CANCELLED">Cancelled</option>
                </select>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Loading...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>User</th>
                        <th>Date (Mock)</th>
                        <th>Total</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="order in filteredOrders" :key="order.id">
                        <td>{{ order.id }}</td>
                        <td>{{ order.username || order.userId }}</td>
                        <td>-</td> <!-- Date not in DTO yet, placeholder -->
                        <td>${{ order.finalTotal }}</td>
                        <td>
                            <span class="status-badge" :class="order.status.toLowerCase()">{{ order.status }}</span>
                        </td>
                        <td>
                            <button class="btn-text" @click="viewDetails(order)">View</button>
                            <button class="btn-text text-danger" @click="handleDelete(order.id)">Delete</button>
                        </td>
                    </tr>
                    <tr v-if="filteredOrders.length === 0">
                        <td colspan="6" class="text-center">No orders found.</td>
                    </tr>
                </tbody>
            </table>
        </div>


    </div>
</template>

<style scoped>
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    flex-wrap: wrap;
    gap: 1rem;
}

.actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.search-input {
    padding: 0.5rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-right: 0.5rem;
    min-width: 200px;
}

.filter-select {
    padding: 0.5rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
}

.loading {
    text-align: center;
    padding: 2rem;
    color: var(--color-text-muted);
}

.text-center {
    text-align: center;
}

.btn-text {
    background: none;
    border: none;
    color: var(--color-primary);
    padding: 0 0.5rem;
}

.btn-text:hover {
    text-decoration: underline;
}

.text-danger {
    color: var(--color-danger);
}

.status-badge {
    padding: 0.25rem 0.5rem;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
}

.status-badge.pending {
    background-color: #FEF3C7;
    color: #D97706;
}

.status-badge.processing {
    background-color: #DBEAFE;
    color: #2563EB;
}

.status-badge.shipped {
    background-color: #E0E7FF;
    color: #4F46E5;
}

.status-badge.delivered {
    background-color: #D1FAE5;
    color: #059669;
}

.status-badge.cancelled {
    background-color: #FEE2E2;
    color: #DC2626;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal {
    width: 100%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
}

.order-info {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.5rem;
    margin-bottom: 1.5rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--border-color);
}

.order-info .total {
    grid-column: span 2;
    font-size: 1.25rem;
    color: var(--color-primary);
    margin-top: 0.5rem;
}

.status-actions {
    grid-column: span 2;
    margin-top: 1rem;
    display: flex;
    gap: 0.5rem;
    align-items: center;
}

.status-actions select {
    padding: 0.25rem;
}

.items-list {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-bottom: 1rem;
}

.item-row {
    display: flex;
    justify-content: space-between;
    padding: 0.5rem;
    background-color: var(--color-background);
    border-radius: var(--radius-md);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
}
</style>
