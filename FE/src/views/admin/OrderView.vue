<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import orderService, { type OrderDTO } from '../../services/orderService';
import Pagination from '../../components/Pagination.vue';

const router = useRouter();
const orders = ref<OrderDTO[]>([]);
const isLoading = ref(false);

// Pagination state
const currentPage = ref(0);
const totalPages = ref(0);
const totalElements = ref(0);
const pageSize = ref(5);

// Filters
const searchQuery = ref('');
const statusFilter = ref('all');

const fetchOrders = async () => {
    isLoading.value = true;
    try {
        const params: any = {
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchQuery.value || undefined,
            status: statusFilter.value === 'all' ? undefined : statusFilter.value
        };
        const response = await orderService.searchOrders(params);
        orders.value = response.data.content;
        totalPages.value = response.data.totalPages;
        totalElements.value = response.data.totalElements;
    } catch (error) {
        console.error('Error fetching orders:', error);
    } finally {
        isLoading.value = false;
    }
};

watch([searchQuery, statusFilter], () => {
    currentPage.value = 0;
    fetchOrders();
});

const handlePageChange = (page: number) => {
    currentPage.value = page;
    fetchOrders();
};

const viewDetails = (order: OrderDTO) => {
    router.push(`/admin/orders/${order.id}`);
};

const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa đơn hàng này?')) return;
    try {
        await orderService.deleteOrder(id);
        await fetchOrders();
    } catch (error) {
        console.error('Error deleting order:', error);
    }
};

const exportExcel = () => {
  window.open(`${import.meta.env.VITE_API_BASE_URL || '/api'}/export/orders/excel`, '_blank');
};

onMounted(() => {
    fetchOrders();
});
</script>

<template>
    <div class="order-view">
        <div class="page-header">
            <div class="page-title">
                <h2>Quản lý đơn hàng</h2>
            </div>
        </div>

        <div class="action-bar card">
            <div class="filter-group">
                <div class="search-wrapper">
                    <i class="pi pi-search search-icon"></i>
                    <input type="text" v-model="searchQuery" placeholder="Tìm ID đơn hàng hoặc khách hàng..." class="search-input" />
                </div>

                <div class="select-group">
                    <div class="select-wrapper">
                        <select v-model="statusFilter" class="filter-select">
                            <option value="all">Tất cả trạng thái</option>
                            <option value="PENDING">Chờ xử lý</option>
                            <option value="PROCESSING">Đang xử lý</option>
                            <option value="SHIPPED">Đang giao</option>
                            <option value="DELIVERED">Đã giao</option>
                            <option value="CANCELLED">Đã hủy</option>
                        </select>
                        <i class="pi pi-chevron-down select-icon"></i>
                    </div>
                </div>
            </div>

            <div class="button-group">
                <button class="btn btn-outline" @click="exportExcel()">
                    <i class="pi pi-file-export"></i>
                    <span>Xuất Excel</span>
                </button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Đang tải...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>Mã đơn</th>
                        <th>Khách hàng</th>
                        <th>Ngày đặt</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="order in orders" :key="order.id">
                        <td><strong>#{{ order.id }}</strong></td>
                        <td>{{ order.username || order.userId }}</td>
                        <td>{{ order.createdAt ? new Date(order.createdAt).toLocaleDateString() : '-' }}</td> 
                        <td><span class="price-val">{{ order.finalTotal.toLocaleString() }}đ</span></td>
                        <td>
                            <span class="badge" :class="order.status.toLowerCase()">{{ order.status }}</span>
                        </td>
                        <td>
                            <button class="btn-text" @click="viewDetails(order)">Chi tiết</button>
                            <button class="btn-text text-danger" @click="handleDelete(order.id)">Xóa</button>
                        </td>
                    </tr>
                    <tr v-if="orders.length === 0">
                        <td colspan="6" class="text-center">Không tìm thấy đơn hàng nào.</td>
                    </tr>
                </tbody>
            </table>

            <Pagination 
                :current-page="currentPage" 
                :total-pages="totalPages" 
                :total-elements="totalElements"
                :page-size="pageSize"
                @page-change="handlePageChange"
            />
        </div>
    </div>
</template>

<style scoped>
.loading {
    text-align: center;
    padding: 3rem;
    color: var(--color-text-muted);
}

.price-val {
    font-weight: 600;
    color: var(--color-text-main);
}

.text-center {
    text-align: center;
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

.text-danger {
    color: var(--color-danger);
}

/* Status Badges */
.badge.pending {
    background-color: #FEF3C7;
    color: #D97706;
}

.badge.processing {
    background-color: #DBEAFE;
    color: #2563EB;
}

.badge.shipped {
    background-color: #E0E7FF;
    color: #4F46E5;
}

.badge.delivered {
    background-color: #D1FAE5;
    color: #059669;
}

.badge.cancelled {
    background-color: #FEE2E2;
    color: #DC2626;
}

/* Table Enhancements */
.table-container {
    border: 1px solid var(--border-color);
    box-shadow: var(--shadow-sm);
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

strong {
    color: var(--color-primary);
}
</style>
