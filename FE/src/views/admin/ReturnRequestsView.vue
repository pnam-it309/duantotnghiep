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
    const actionText = status === 'APPROVED' ? 'chấp nhận' : 'từ chối';
    if (!confirm(`Bạn có chắc chắn muốn ${actionText} yêu cầu này?`)) return;
    try {
        await returnService.updateStatus(id, status);
        await loadRequests();
    } catch (e) {
        alert('Thao tác thất bại');
    }
};

onMounted(loadRequests);
</script>

<template>
    <div class="return-requests-view">
        <div class="page-header">
            <div class="page-title">
                <h2>Yêu cầu trả hàng</h2>
            </div>
            <button @click="loadRequests" class="btn btn-outline">
                <i class="pi pi-refresh"></i>
                <span>Làm mới</span>
            </button>
        </div>

        <div class="action-bar card">
            <div class="filter-group">
                <div class="search-wrapper">
                    <i class="pi pi-search search-icon"></i>
                    <input type="text" placeholder="Tìm ID đơn hàng, lý do..." class="search-input" />
                </div>
            </div>
        </div>

        <div v-if="isLoading" class="loading">
          <div class="loader"></div>
          <span>Đang tải yêu cầu...</span>
        </div>

        <div v-else class="card table-container">
            <table class="table">
                <thead>
                    <tr>
                        <th>Mã yêu cầu</th>
                        <th>Mã đơn hàng</th>
                        <th>Lý do trả hàng</th>
                        <th>Tiền hoàn</th>
                        <th>Trạng thái</th>
                        <th>Ngày tạo</th>
                        <th class="text-right">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="req in requests" :key="req.id">
                        <td><span class="id-badge">#{{ req.id }}</span></td>
                        <td>
                            <router-link :to="`/admin/orders/${req.orderId}`" class="order-link">
                                <i class="pi pi-external-link"></i>
                                #{{ req.orderId }}
                            </router-link>
                        </td>
                        <td><div class="reason-cell" :title="req.reason">{{ req.reason }}</div></td>
                        <td><span class="price-val">{{ req.refundAmount?.toLocaleString() }}đ</span></td>
                        <td>
                            <span class="badge" :class="req.status">{{ req.status }}</span>
                        </td>
                        <td>
                          <div class="date-cell">
                            <span>{{ new Date(req.createdAt).toLocaleDateString() }}</span>
                            <small>{{ new Date(req.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }}</small>
                          </div>
                        </td>
                        <td class="text-right">
                            <div v-if="req.status === 'PENDING'" class="actions-group">
                                <button @click="handleAction(req.id, 'APPROVED')"
                                    class="btn-icon btn-approve" title="Chấp nhận">
                                    <i class="pi pi-check"></i>
                                </button>
                                <button @click="handleAction(req.id, 'REJECTED')"
                                    class="btn-icon btn-reject" title="Từ chối">
                                    <i class="pi pi-times"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                    <tr v-if="requests.length === 0">
                        <td colspan="7" class="text-center empty-state">Không có yêu cầu trả hàng nào.</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<style scoped>
.loading {
    text-align: center;
    padding: 4rem;
    color: var(--color-text-muted);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
}

.id-badge {
    background: #f1f5f9;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-family: monospace;
    font-weight: 600;
    font-size: 0.8125rem;
}

.order-link {
    color: var(--color-primary);
    font-weight: 600;
    text-decoration: none;
    display: flex;
    align-items: center;
    gap: 0.25rem;
}

.order-link:hover {
    text-decoration: underline;
}

.reason-cell {
    max-width: 250px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.price-val {
    font-weight: 700;
    color: var(--color-text-main);
}

.date-cell {
  display: flex;
  flex-direction: column;
}

.date-cell small {
  color: var(--color-text-muted);
  font-size: 0.75rem;
}

.actions-group {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
}

.btn-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s;
}

.btn-approve {
    background: #f0fdf4;
    color: #16a34a;
}
.btn-approve:hover {
    background: #dcfce7;
    transform: scale(1.1);
}

.btn-reject {
    background: #fef2f2;
    color: #dc2626;
}
.btn-reject:hover {
    background: #fee2e2;
    transform: scale(1.1);
}

.text-right { text-align: right; }
.empty-state { padding: 3rem !important; color: var(--color-text-muted); font-style: italic; }

.loader {
    width: 32px;
    height: 32px;
    border: 3px solid #f3f3f3;
    border-top: 3px solid var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>
