<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import couponService, { type CouponDTO } from '../../services/couponService';
import importService from '../../services/importService';
import Pagination from '../../components/Pagination.vue';

const coupons = ref<CouponDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingCoupon = ref<CouponDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
// Search & Pagination
const searchQuery = ref('');
const currentPage = ref(0);
const pageSize = ref(5);
const totalPages = ref(0);
const totalElements = ref(0);

const formData = ref({
    code: '',
    discountAmount: 0,
    minOrderValue: 0,
    expiryDate: '',
    discountType: 'FIXED_AMOUNT'
});

const fetchCoupons = async () => {
    isLoading.value = true;
    try {
        const params = {
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchQuery.value || undefined
        };
        const response = await couponService.searchCoupons(params);
        coupons.value = response.data.content;
        totalPages.value = response.data.totalPages;
        totalElements.value = response.data.totalElements;
    } catch (error) {
        console.error('Error fetching coupons:', error);
    } finally {
        isLoading.value = false;
    }
};

watch(searchQuery, () => {
    currentPage.value = 0;
    fetchCoupons();
});

const handlePageChange = (page: number) => {
    currentPage.value = page;
    fetchCoupons();
};

const triggerFileInput = () => {
    fileInput.value?.click();
};

const handleFileUpload = async (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
        const file = target.files[0];
        try {
            await importService.importCoupons(file!);
            alert('Import successful!');
            await fetchCoupons();
        } catch (error) {
            console.error('Import failed', error);
            alert('Import failed');
        } finally {
            if (fileInput.value) fileInput.value.value = '';
        }
    }
};

const handleSubmit = async () => {
    if (!formData.value.code) return;

    try {
        const payload = {
            code: formData.value.code,
            discountAmount: formData.value.discountAmount,
            minOrderValue: formData.value.minOrderValue,
            discountType: formData.value.discountType,
            expiryDate: formData.value.expiryDate // Keep as locale string or let backend handle if configured
        };

        if (editingCoupon.value) {
            await couponService.updateCoupon(editingCoupon.value.id, { ...editingCoupon.value, ...payload });
        } else {
            await couponService.createCoupon(payload);
        }
        await fetchCoupons();
        closeForm();
    } catch (error) {
        console.error('Error saving coupon:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa mã giảm giá này?')) return;

    try {
        await couponService.deleteCoupon(id);
        await fetchCoupons();
    } catch (error) {
        console.error('Error deleting coupon:', error);
    }
};

const openForm = (coupon?: CouponDTO) => {
    if (coupon) {
        editingCoupon.value = coupon;
        formData.value = {
            code: coupon.code,
            discountAmount: coupon.discountAmount,
            minOrderValue: coupon.minOrderValue,
            expiryDate: coupon.expiryDate ? coupon.expiryDate.slice(0, 16) : '', // format for datetime-local
            discountType: coupon.discountType || 'FIXED_AMOUNT'
        };
    } else {
        editingCoupon.value = null;
        formData.value = {
            code: '',
            discountAmount: 0,
            minOrderValue: 0,
            expiryDate: '',
            discountType: 'FIXED_AMOUNT'
        };
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingCoupon.value = null;
    formData.value = {
        code: '',
        discountAmount: 0,
        minOrderValue: 0,
        expiryDate: '',
        discountType: 'FIXED_AMOUNT'
    };
};

onMounted(() => {
    fetchCoupons();
});
</script>

<template>
    <div class="coupon-view">
        <div class="page-header">
            <div class="page-title">
                <h2>Mã giảm giá</h2>
            </div>
            <button class="btn btn-primary btn-add" @click="openForm()">
                <i class="pi pi-plus"></i>
                <span>Thêm mã giảm giá</span>
            </button>
        </div>

        <div class="action-bar card">
            <div class="filter-group">
                <div class="search-wrapper">
                    <i class="pi pi-search search-icon"></i>
                    <input type="text" v-model="searchQuery" placeholder="Tìm mã giảm giá..." class="search-input" />
                </div>
            </div>

            <div class="button-group">
                <div class="import-export">
                    <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none" accept=".xlsx, .xls" />
                    <button class="btn btn-outline" @click="triggerFileInput" title="Nhập Excel">
                        <i class="pi pi-file-import"></i>
                        <span>Nhập Excel</span>
                    </button>
                </div>
                <button class="btn btn-outline" @click="importService.downloadTemplate('coupons')" title="Tải mẫu">
                    <i class="pi pi-download"></i>
                    <span>Tải mẫu</span>
                </button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Đang tải...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Mã</th>
                        <th>Giảm giá</th>
                        <th>Đơn hàng tối thiểu</th>
                        <th>Ngày hết hạn</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="coupon in coupons" :key="coupon.id">
                        <td>{{ coupon.id }}</td>
                        <td>
                          <span class="badge badge-info">{{ coupon.code }}</span>
                        </td>
                        <td>
                          <span v-if="coupon.discountType === 'PERCENT'">{{ coupon.discountAmount }}%</span>
                          <span v-else>{{ coupon.discountAmount.toLocaleString() }}đ</span>
                        </td>
                        <td>{{ coupon.minOrderValue.toLocaleString() }}đ</td>
                        <td>{{ new Date(coupon.expiryDate).toLocaleDateString() }}</td>
                        <td>
                            <button class="btn-text" @click="openForm(coupon)">Sửa</button>
                            <button class="btn-text text-danger" @click="handleDelete(coupon.id)">Xóa</button>
                        </td>
                    </tr>
                    <tr v-if="coupons.length === 0">
                        <td colspan="6" class="text-center">Không tìm thấy mã giảm giá nào.</td>
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

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingCoupon ? 'Chỉnh sửa' : 'Thêm mới' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="code">Mã coupon</label>
                        <input id="code" v-model="formData.code" type="text" placeholder="VÍ DỤ: GIAM50K" required />
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="discountAmount">Giá trị giảm</label>
                            <input id="discountAmount" v-model="formData.discountAmount" type="number" step="0.01"
                                required />
                        </div>
                        <div class="form-group">
                            <label for="minOrderValue">Đơn tối thiểu</label>
                            <input id="minOrderValue" v-model="formData.minOrderValue" type="number" required />
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="expiryDate">Ngày hết hạn</label>
                            <input id="expiryDate" v-model="formData.expiryDate" type="datetime-local" required />
                        </div>
                        <div class="form-group">
                            <label for="discountType">Loại giảm giá</label>
                            <select id="discountType" v-model="formData.discountType" required>
                                <option value="FIXED_AMOUNT">Số tiền cố định (VNĐ)</option>
                                <option value="PERCENT">Phần trăm (%)</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn" @click="closeForm">Hủy</button>
                        <button type="submit" class="btn btn-primary">Lưu</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<style scoped>
.loading {
    text-align: center;
    padding: 3rem;
    color: var(--color-text-muted);
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

.badge-info {
    background-color: #e0f2fe;
    color: #0369a1;
    font-family: monospace;
    font-weight: 600;
    letter-spacing: 0.05em;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(4px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal {
    width: 100%;
    max-width: 500px;
    animation: modal-in 0.3s ease-out;
}

@keyframes modal-in {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

.form {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
    margin-top: 1.5rem;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

input, select {
  padding: 0.625rem 0.875rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 0.9375rem;
  transition: all 0.2s;
}

input:focus, select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(234, 179, 8, 0.1);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1rem;
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
</style>
