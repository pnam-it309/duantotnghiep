<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import orderService, { type OrderDTO, type OrderStatusHistoryDTO } from '../../services/orderService';
import returnService from '../../services/returnService';

const route = useRoute();
const router = useRouter();
const orderId = Number(route.params.id);

const order = ref<OrderDTO | null>(null);
const history = ref<OrderStatusHistoryDTO[]>([]);
const isLoading = ref(false);

const showShipModal = ref(false);
const selectedCarrier = ref('GHN');

const showReturnModal = ref(false);
const returnReason = ref('');

const loadData = async () => {
    isLoading.value = true;
    try {
        const orderRes = await orderService.getOrderById(orderId);
        order.value = orderRes.data;
        const historyRes = await orderService.getOrderHistory(orderId);
        history.value = historyRes.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
};

const handleShipOrder = async () => {
    if (!order.value) return;
    try {
        await orderService.shipOrder(order.value.id, selectedCarrier.value);
        showShipModal.value = false;
        await loadData(); // Reload to see updates
    } catch (e) {
        alert('Failed to ship order');
    }
};

const handleReturnRequest = async () => {
    if (!order.value) return;
    try {
        await returnService.createRequest(order.value.id, returnReason.value);
        showReturnModal.value = false;
        alert('Return request created!');
    } catch (e) {
        alert('Failed to create return request');
    }
};

const formatDate = (dateStr?: string) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString();
};

onMounted(loadData);
</script>

<template>
    <div class="order-detail-view" v-if="order">
        <div class="header">
            <button @click="router.back()" class="btn btn-outline">&larr; Back</button>
            <h2>Order #{{ order.id }}</h2>
            <span class="status-badge" :class="order.status">{{ order.status }}</span>
        </div>

        <div class="content-grid">
            <!-- Left: Info & Items -->
            <div class="main-info">
                <div class="card mb-4">
                    <h3>Customer Info</h3>
                    <p><strong>User ID:</strong> {{ order.userId }}</p>
                    <p><strong>Username:</strong> {{ order.username }}</p>
                    <p><strong>Created At:</strong> {{ formatDate(order.createdAt) }}</p>
                </div>

                <div class="card mb-4">
                    <h3>Shipping Info</h3>
                    <div v-if="order.trackingCode">
                        <p><strong>Carrier:</strong> {{ order.carrierName }}</p>
                        <p><strong>Tracking Code:</strong> <span class="highlight">{{ order.trackingCode }}</span></p>
                    </div>
                    <div v-else>
                        <p class="text-muted">Not shipped yet.</p>
                    </div>
                </div>

                <div class="card">
                    <h3>Items</h3>
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Product</th>
                                <th>Sku</th>
                                <th>Price</th>
                                <th>Qty</th>
                                <th>Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="item in order.items" :key="item.id">
                                <td>{{ item.productName }}</td>
                                <td>{{ item.variantSku }}</td>
                                <td>${{ item.price }}</td>
                                <td>{{ item.quantity }}</td>
                                <td>${{ item.price * item.quantity }}</td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr>
                                <td colspan="4" class="text-right"><strong>Total:</strong></td>
                                <td><strong>${{ order.finalTotal }}</strong></td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>

            <!-- Right: Timeline & Actions -->
            <div class="sidebar">
                <div class="card actions-card mb-4">
                    <h3>Actions</h3>
                    <div class="action-buttons">
                        <button
                            v-if="order.status !== 'SHIPPING' && order.status !== 'DELIVERED' && order.status !== 'CANCELLED'"
                            @click="showShipModal = true" class="btn btn-primary full-width">
                            Ship Order
                        </button>

                        <button v-if="order.status === 'DELIVERED'" @click="showReturnModal = true"
                            class="btn btn-warning full-width">
                            Request Return
                        </button>
                    </div>
                </div>

                <div class="card timeline-card">
                    <h3>Order Timeline</h3>
                    <div class="timeline">
                        <div v-for="h in history" :key="h.id" class="timeline-item">
                            <div class="time">{{ formatDate(h.changedAt) }}</div>
                            <div class="content">
                                <div class="status">{{ h.status }}</div>
                                <div class="note">{{ h.note }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modals -->
        <div v-if="showShipModal" class="modal-overlay">
            <div class="modal">
                <h3>Ship Order</h3>
                <div class="form-group">
                    <label>Select Carrier</label>
                    <select v-model="selectedCarrier">
                        <option value="GHN">Giao Hàng Nhanh (GHN)</option>
                        <option value="VTP">Viettel Post</option>
                    </select>
                </div>
                <div class="modal-actions">
                    <button @click="showShipModal = false" class="btn btn-outline">Cancel</button>
                    <button @click="handleShipOrder" class="btn btn-primary">Confirm Shipping</button>
                </div>
            </div>
        </div>

        <div v-if="showReturnModal" class="modal-overlay">
            <div class="modal">
                <h3>Request Return</h3>
                <div class="form-group">
                    <label>Reason</label>
                    <textarea v-model="returnReason" rows="3"></textarea>
                </div>
                <div class="modal-actions">
                    <button @click="showReturnModal = false" class="btn btn-outline">Cancel</button>
                    <button @click="handleReturnRequest" class="btn btn-warning">Submit Request</button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.order-detail-view {
    padding: 1rem;
}

.header {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 2rem;
}

.status-badge {
    padding: 0.25rem 0.75rem;
    border-radius: 999px;
    font-size: 0.875rem;
    font-weight: 500;
    color: white;
    background-color: #6c757d;
}

.status-badge.DELIVERED {
    background-color: #28a745;
}

.status-badge.SHIPPING {
    background-color: #17a2b8;
}

.status-badge.CANCELLED {
    background-color: #dc3545;
}

.status-badge.PENDING {
    background-color: #ffc107;
    color: black;
}

.content-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 1.5rem;
}

.card {
    background: var(--color-surface);
    padding: 1.5rem;
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-color);
}

.mb-4 {
    margin-bottom: 1.5rem;
}

.table {
    width: 100%;
    border-collapse: collapse;
}

.table th,
.table td {
    padding: 0.75rem;
    text-align: left;
    border-bottom: 1px solid var(--border-color);
}

.text-right {
    text-align: right;
}

.full-width {
    width: 100%;
    margin-bottom: 0.5rem;
}

.timeline {
    border-left: 2px solid var(--border-color);
    padding-left: 1.5rem;
    margin-top: 1rem;
}

.timeline-item {
    position: relative;
    margin-bottom: 1.5rem;
}

.timeline-item::before {
    content: '';
    position: absolute;
    left: -1.9rem;
    top: 5px;
    width: 10px;
    height: 10px;
    background: var(--color-primary);
    border-radius: 50%;
}

.time {
    font-size: 0.75rem;
    color: var(--color-text-muted);
}

.status {
    font-weight: 600;
}

.note {
    font-size: 0.875rem;
}

.highlight {
    font-family: monospace;
    background: #f8f9fa;
    padding: 2px 6px;
    border-radius: 4px;
}

/* Modal */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal {
    background: white;
    padding: 2rem;
    border-radius: 8px;
    width: 400px;
}

.modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1.5rem;
}

.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
}

.form-group select,
.form-group textarea {
    width: 100%;
    padding: 0.5rem;
}

@media (max-width: 800px) {
    .content-grid {
        grid-template-columns: 1fr;
    }
}
</style>
