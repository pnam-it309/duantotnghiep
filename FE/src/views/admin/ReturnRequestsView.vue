<script setup lang="ts">
import { ref, onMounted } from 'vue';
import returnService, { type ReturnRequestDTO } from '../../services/returnService';

const requests = ref<ReturnRequestDTO[]>([]);
const isLoading = ref(false);

const loadRequests = async () => {
    isLoading.value = true;
    try {
        const res = await returnService.getAllRequests();
        requests.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
};

const handleAction = async (id: number, status: 'APPROVED' | 'REJECTED') => {
    if (!confirm(`Are you sure you want to ${status} this request?`)) return;
    try {
        await returnService.updateStatus(id, status);
        await loadRequests();
    } catch (e) {
        alert('Action failed');
    }
};

onMounted(loadRequests);
</script>

<template>
    <div class="return-requests-view">
        <div class="header">
            <h2>Return Requests</h2>
            <button @click="loadRequests" class="btn btn-outline">Refresh</button>
        </div>

        <div v-if="isLoading" class="loading">Loading...</div>

        <table v-else class="table card">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Order ID</th>
                    <th>Reason</th>
                    <th>Refund Amount</th>
                    <th>Status</th>
                    <th>Created At</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="req in requests" :key="req.id">
                    <td>{{ req.id }}</td>
                    <td>
                        <router-link :to="`/admin/orders/${req.orderId}`">#{{ req.orderId }}</router-link>
                    </td>
                    <td>{{ req.reason }}</td>
                    <td>${{ req.refundAmount }}</td>
                    <td>
                        <span class="status-badge" :class="req.status">{{ req.status }}</span>
                    </td>
                    <td>{{ new Date(req.createdAt).toLocaleString() }}</td>
                    <td>
                        <div v-if="req.status === 'PENDING'" class="actions">
                            <button @click="handleAction(req.id, 'APPROVED')"
                                class="btn btn-sm btn-success">Approve</button>
                            <button @click="handleAction(req.id, 'REJECTED')"
                                class="btn btn-sm btn-danger">Reject</button>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<style scoped>
.return-requests-view {
    padding: 1rem;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
}

.table {
    width: 100%;
    border-collapse: collapse;
}

.card {
    background: var(--color-surface);
    padding: 1rem;
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-color);
}

.table th,
.table td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid var(--border-color);
}

.status-badge {
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-size: 0.85rem;
    font-weight: bold;
}

.status-badge.PENDING {
    background: #ffeeba;
    color: #856404;
}

.status-badge.APPROVED {
    background: #d4edda;
    color: #155724;
}

.status-badge.REJECTED {
    background: #f8d7da;
    color: #721c24;
}

.actions {
    display: flex;
    gap: 0.5rem;
}

.btn-sm {
    padding: 0.25rem 0.5rem;
    font-size: 0.75rem;
}

.btn-success {
    background: #28a745;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.btn-danger {
    background: #dc3545;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}
</style>
