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
        <div class="page-header">
            <div class="page-title">
                <h2>Nhập kho</h2>
            </div>
            <button class="btn btn-primary btn-add" @click="$router.push('/admin/goods-receipts/new')">
                <i class="pi pi-plus"></i>
                <span>Nhập hàng mới</span>
            </button>
        </div>

        <div class="action-bar card">
            <div class="filter-group">
                <div class="search-wrapper">
                    <i class="pi pi-search search-icon"></i>
                    <input type="text" v-model="searchQuery" placeholder="Tìm ID, nhà cung cấp, người nhập..." class="search-input" />
                </div>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Đang tải...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>Mã phiếu</th>
                        <th>Nhà cung cấp</th>
                        <th>Ngày nhập</th>
                        <th>Người nhập</th>
                        <th>Tổng tiền</th>
                        <th class="text-right">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="r in filteredReceipts" :key="r.id">
                        <td><span class="id-badge">#{{ r.id }}</span></td>
                        <td><strong>{{ r.supplierName }}</strong></td>
                        <td>
                          <div class="date-cell">
                            <span>{{ new Date(r.importDate!).toLocaleDateString() }}</span>
                            <small>{{ new Date(r.importDate!).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }}</small>
                          </div>
                        </td>
                        <td>{{ r.username }}</td>
                        <td><span class="price-val">{{ r.totalAmount?.toLocaleString() }}đ</span></td>
                        <td class="text-right">
                            <button class="btn-text">Chi tiết</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<style scoped>
.loading {
    text-align: center;
    padding: 3rem;
    color: var(--color-text-muted);
}

.id-badge {
    background: #f1f5f9;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-family: monospace;
    font-weight: 600;
    font-size: 0.8125rem;
    color: #475569;
}

.date-cell {
  display: flex;
  flex-direction: column;
}

.date-cell small {
  color: var(--color-text-muted);
  font-size: 0.75rem;
}

.price-val {
    font-weight: 600;
    color: var(--color-text-main);
}

.btn-text {
    background: none;
    border: none;
    color: var(--color-primary);
    font-weight: 500;
    padding: 0 0.5rem;
    font-size: 0.875rem;
}

.btn-text:hover {
    text-decoration: underline;
}

.text-right {
    text-align: right;
}

table th {
    background-color: #f9fafb;
    padding: 1rem;
}

table td {
    padding: 1rem;
    font-size: 0.875rem;
    color: var(--color-text-main);
}
</style>
