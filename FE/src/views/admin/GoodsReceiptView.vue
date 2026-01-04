<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import goodsReceiptService, { type GoodsReceiptDTO } from '../../services/goodsReceiptService';

const receipts = ref<GoodsReceiptDTO[]>([]);
const isLoading = ref(false);
const searchQuery = ref('');

const filteredReceipts = computed(() => {
    if (!searchQuery.value) return receipts.value;
    const query = searchQuery.value.toLowerCase();
    return receipts.value.filter(r =>
        r.id?.toString().includes(query) ||
        r.supplierName?.toLowerCase().includes(query) ||
        r.username?.toLowerCase().includes(query)
    );
});

const loadReceipts = async () => {
    isLoading.value = true;
    try {
        const res = await goodsReceiptService.getAllReceipts();
        receipts.value = res.data;
    } catch (e) {
        console.error("Error loading receipts", e);
    } finally {
        isLoading.value = false;
    }
};

onMounted(loadReceipts);
</script>

<template>
    <div class="receipt-view">
        <div class="header">
            <h2>Goods Receipts</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search receipt..." class="search-input" />
                <button class="btn btn-primary" @click="$router.push('/admin/goods-receipts/new')">New Import</button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Loading...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Supplier</th>
                        <th>Date</th>
                        <th>Imported By</th>
                        <th>Total</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="r in filteredReceipts" :key="r.id">
                        <td>{{ r.id }}</td>
                        <td>{{ r.supplierName }}</td>
                        <td>{{ new Date(r.importDate!).toLocaleDateString() }} {{ new
                            Date(r.importDate!).toLocaleTimeString() }}</td>
                        <td>{{ r.username }}</td>
                        <td>{{ r.totalAmount?.toLocaleString() }}</td>
                        <td>
                            <!-- View Detail Logic to be added later -->
                            <button class="btn-text">View</button>
                        </td>
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
    min-width: 200px;
}

.loading {
    text-align: center;
    padding: 2rem;
    color: var(--color-text-muted);
}

.card {
    background-color: var(--color-surface);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    padding: 1.5rem;
    border: 1px solid var(--border-color);
}

.table-container {
    overflow-x: auto;
}

table {
    width: 100%;
    border-collapse: collapse;
}

th,
td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid var(--border-color);
}

th {
    font-weight: 600;
    color: var(--color-text-muted);
    font-size: 0.875rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.btn {
    padding: 0.625rem 1.25rem;
    border-radius: var(--radius-md);
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
    font-size: 0.9rem;
}

.btn-primary {
    background-color: var(--color-primary);
    color: white;
}

.btn-primary:hover {
    background-color: var(--color-primary-dark);
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
</style>
