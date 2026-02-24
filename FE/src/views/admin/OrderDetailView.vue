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
        alert('Lỗi khi cập nhật trạng thái giao hàng');
    }
};

const handleReturnRequest = async () => {
    if (!order.value) return;
    try {
        await returnService.createRequest(order.value.id, returnReason.value);
        showReturnModal.value = false;
        alert('Đã tạo yêu cầu trả hàng!');
    } catch (e) {
        alert('Lỗi khi tạo yêu cầu trả hàng');
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
        <div class="page-header">
            <div class="page-title">
                <div class="back-nav">
                    <button @click="router.back()" class="btn-icon-link">
                        <i class="pi pi-arrow-left"></i>
                        <span>Quay lại</span>
                    </button>
                </div>
                <h2>Chi tiết đơn hàng #{{ order.id }}</h2>
            </div>
            <div class="header-actions">
                <span class="badge" :class="order.status">{{ order.status }}</span>
                <a :href="`${import.meta.env.VITE_API_BASE_URL || '/api'}/export/orders/pdf?orderId=${order.id}`" 
                   class="btn btn-outline" target="_blank">
                    <i class="pi pi-file-pdf"></i>
                    <span>Tải Hóa đơn</span>
                </a>
            </div>
        </div>

        <div class="content-grid">
            <!-- Left: Info & Items -->
            <div class="main-column">
                <div class="info-cards">
                    <div class="card shadow-sm">
                        <div class="card-header-v2">
                            <i class="pi pi-user"></i>
                            <h3>Khách hàng</h3>
                        </div>
                        <div class="card-body-v2">
                            <div class="info-row">
                                <span>Tên tài khoản:</span>
                                <strong>{{ order.username }}</strong>
                            </div>
                            <div class="info-row">
                                <span>ID khách hàng:</span>
                                <span class="badge badge-outline">#{{ order.userId }}</span>
                            </div>
                            <div class="info-row">
                                <span>Ngày đặt hàng:</span>
                                <span>{{ formatDate(order.createdAt) }}</span>
                            </div>
                        </div>
                    </div>

                    <div class="card shadow-sm">
                        <div class="card-header-v2">
                            <i class="pi pi-truck"></i>
                            <h3>Vận chuyển</h3>
                        </div>
                        <div class="card-body-v2">
                            <div v-if="order.trackingCode">
                                <div class="info-row">
                                    <span>Đơn vị:</span>
                                    <strong>{{ order.carrierName }}</strong>
                                </div>
                                <div class="info-row">
                                    <span>Mã vận đơn:</span>
                                    <span class="tracking-code">{{ order.trackingCode }}</span>
                                </div>
                            </div>
                            <div v-else class="empty-state-sm">
                                <i class="pi pi-info-circle"></i>
                                <span>Chờ xử lý giao hàng</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card table-card shadow-sm mt-6">
                    <div class="card-header-v2">
                        <i class="pi pi-shopping-bag"></i>
                        <h3>Sản phẩm đã mua</h3>
                    </div>
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Sản phẩm</th>
                                <th>Sku</th>
                                <th class="text-right">Giá</th>
                                <th class="text-center">Số lượng</th>
                                <th class="text-right">Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="item in order.items" :key="item.id">
                                <td>
                                    <div class="product-cell">
                                        <strong>{{ item.productName }}</strong>
                                    </div>
                                </td>
                                <td><span class="sku-badge">{{ item.variantSku }}</span></td>
                                <td class="text-right">{{ item.price.toLocaleString() }}đ</td>
                                <td class="text-center">{{ item.quantity }}</td>
                                <td class="text-right"><strong>{{ (item.price * item.quantity).toLocaleString() }}đ</strong></td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr class="summary-row">
                                <td colspan="4" class="text-right">Tạm tính:</td>
                                <td class="text-right">{{ (order.finalTotal + (order.discountTotal || 0)).toLocaleString() }}đ</td>
                            </tr>
                            <tr class="summary-row promo" v-if="order.discountTotal">
                                <td colspan="4" class="text-right">Giảm giá:</td>
                                <td class="text-right">-{{ order.discountTotal.toLocaleString() }}đ</td>
                            </tr>
                            <tr class="summary-row total">
                                <td colspan="4" class="text-right">TỔNG CỘNG:</td>
                                <td class="text-right price-highlight">{{ order.finalTotal.toLocaleString() }}đ</td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>

            <!-- Right: Timeline & Actions -->
            <div class="side-column">
                <div class="card actions-card shadow-sm mb-6">
                    <h3>Xử lý đơn hàng</h3>
                    <div class="action-list mt-4">
                        <button
                            v-if="order.status !== 'SHIPPING' && order.status !== 'DELIVERED' && order.status !== 'CANCELLED'"
                            @click="showShipModal = true" class="btn btn-primary btn-block">
                            <i class="pi pi-send"></i>
                            <span>Giao hàng ngay</span>
                        </button>

                        <button v-if="order.status === 'DELIVERED'" @click="showReturnModal = true"
                            class="btn btn-warning btn-block">
                            <i class="pi pi-refresh"></i>
                            <span>Yêu cầu trả hàng</span>
                        </button>
                        
                        <button v-if="order.status === 'PENDING'" class="btn btn-outline-danger btn-block">
                           <i class="pi pi-times"></i>
                           <span>Hủy đơn hàng</span>
                        </button>
                    </div>
                </div>

                <div class="card timeline-card shadow-sm">
                    <h3>Lịch sử thay đổi</h3>
                    <div class="timeline mt-4">
                        <div v-for="h in history" :key="h.id" class="timeline-item">
                            <div class="timeline-marker"></div>
                            <div class="timeline-content">
                                <div class="timeline-header">
                                    <span class="timeline-status">{{ h.status }}</span>
                                    <span class="timeline-time">{{ formatDate(h.changedAt) }}</span>
                                </div>
                                <div class="timeline-note" v-if="h.note">{{ h.note }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modals -->
        <div v-if="showShipModal" class="modal-overlay">
            <div class="modal card">
                <div class="modal-header">
                    <h3>Xác nhận giao hàng</h3>
                    <p>Vui lòng chọn đơn vị vận chuyển để tiếp tục</p>
                </div>
                <div class="form-group mt-4">
                    <label>Đối tác vận chuyển</label>
                    <div class="select-wrapper">
                        <select v-model="selectedCarrier" class="form-control">
                            <option value="GHN">Giao Hàng Nhanh (GHN)</option>
                            <option value="VTP">Viettel Post</option>
                            <option value="J&T">J&T Express</option>
                        </select>
                        <i class="pi pi-chevron-down select-icon"></i>
                    </div>
                </div>
                <div class="form-actions mt-6">
                    <button @click="showShipModal = false" class="btn">Hủy</button>
                    <button @click="handleShipOrder" class="btn btn-primary">Xác nhận</button>
                </div>
            </div>
        </div>

        <div v-if="showReturnModal" class="modal-overlay">
            <div class="modal card">
                <div class="modal-header">
                    <h3>Yêu cầu trả hàng</h3>
                    <p>Nhập lý do khách hàng muốn trả lại sản phẩm</p>
                </div>
                <div class="form-group mt-4">
                    <label>Lý do trả hàng</label>
                    <textarea v-model="returnReason" rows="3" class="form-control" placeholder="Mô tả lý do..."></textarea>
                </div>
                <div class="form-actions mt-6">
                    <button @click="showReturnModal = false" class="btn">Hủy</button>
                    <button @click="handleReturnRequest" class="btn btn-warning">Gửi yêu cầu</button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.order-detail-view {
    animation: fade-in 0.4s ease-out;
}

@keyframes fade-in {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.back-nav {
    margin-bottom: 0.5rem;
}

.btn-icon-link {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background: none;
    border: none;
    color: var(--color-primary);
    font-weight: 600;
    cursor: pointer;
    padding: 0;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 1.25rem;
}

.content-grid {
    display: grid;
    grid-template-columns: 1fr 340px;
    gap: 1.5rem;
}

.info-cards {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1.5rem;
}

.card-header-v2 {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1.25rem;
    border-bottom: 1px solid #f1f5f9;
}

.card-header-v2 i {
    color: var(--color-primary);
    font-size: 1.125rem;
}

.card-header-v2 h3 {
    margin: 0;
    font-size: 1rem;
    font-weight: 700;
}

.card-body-v2 {
    padding: 1.25rem;
}

.info-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.75rem;
    font-size: 0.875rem;
}

.info-row span:first-child {
    color: var(--color-text-muted);
}

.tracking-code {
    font-family: monospace;
    background: #f1f5f9;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-weight: 700;
    color: #475569;
}

.empty-state-sm {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: var(--color-text-muted);
    font-size: 0.875rem;
    padding: 1rem;
    background: #f8fafc;
    border-radius: 8px;
}

.table-card {
    padding: 0;
}

.product-cell {
    display: flex;
    flex-direction: column;
}

.sku-badge {
    background: #f1f5f9;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-family: monospace;
    font-size: 0.75rem;
}

.summary-row td {
    border: none !important;
    padding: 0.5rem 1rem;
    font-size: 0.875rem;
}

.summary-row.promo td {
    color: #ef4444;
}

.summary-row.total td {
    border-top: 1px solid #e2e8f0 !important;
    padding-top: 1rem;
    margin-top: 0.5rem;
}

.price-highlight {
    font-size: 1.25rem;
    color: var(--color-primary);
}

.btn-block {
    width: 100%;
    margin-bottom: 1rem;
    height: 3rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
}

.btn-outline-danger {
    border: 1px solid #fee2e2;
    color: #ef4444;
    background: white;
}

.btn-outline-danger:hover {
    background: #fef2f2;
}

/* Timeline */
.timeline {
    position: relative;
}

.timeline::before {
    content: '';
    position: absolute;
    left: 7px;
    top: 0;
    bottom: 0;
    width: 2px;
    background: #f1f5f9;
}

.timeline-item {
    position: relative;
    padding-left: 2rem;
    margin-bottom: 1.5rem;
}

.timeline-marker {
    position: absolute;
    left: 0;
    top: 4px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: white;
    border: 3px solid var(--color-primary);
    z-index: 1;
}

.timeline-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.25rem;
}

.timeline-status {
    font-weight: 700;
    font-size: 0.875rem;
}

.timeline-time {
    font-size: 0.75rem;
    color: var(--color-text-muted);
}

.timeline-note {
    font-size: 0.8125rem;
    color: var(--color-text-muted);
    background: #f8fafc;
    padding: 0.5rem;
    border-radius: 6px;
}

label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

.mb-6 { margin-bottom: 1.5rem; }
.mt-6 { margin-top: 1.5rem; }
.mt-4 { margin-top: 1rem; }

@media (max-width: 1024px) {
    .content-grid { grid-template-columns: 1fr; }
    .info-cards { grid-template-columns: 1fr; }
}
</style>
