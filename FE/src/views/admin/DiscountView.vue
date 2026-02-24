<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import discountService, { type DiscountDTO } from '../../services/discountService';
import productService, { type ProductDTO } from '../../services/productService';
import importService from '../../services/importService';
import Pagination from '../../components/Pagination.vue';

const discounts = ref<DiscountDTO[]>([]);
const products = ref<ProductDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingDiscount = ref<DiscountDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

// Filters
// Search & Pagination
const searchQuery = ref('');
const activeFilter = ref('all');
const currentPage = ref(0);
const pageSize = ref(5);
const totalPages = ref(0);
const totalElements = ref(0);

const formData = ref({
    productId: 0,
    discountPercent: 0,
    startDate: '',
    endDate: '',
    active: true
});

const loadData = async () => {
    isLoading.value = true;
    try {
        const params = {
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchQuery.value || undefined,
            active: activeFilter.value === 'all' ? undefined : (activeFilter.value === 'active')
        };
        const [discountsRes, productsRes] = await Promise.all([
            discountService.searchDiscounts(params),
            productService.getAllProducts()
        ]);
        discounts.value = discountsRes.data.content;
        totalPages.value = discountsRes.data.totalPages;
        totalElements.value = discountsRes.data.totalElements;
        products.value = Array.isArray(productsRes.data) ? productsRes.data : (productsRes.data as any).content;
    } catch (error) {
        console.error('Error loading discounts:', error);
    } finally {
        isLoading.value = false;
    }
};

watch([searchQuery, activeFilter], () => {
    currentPage.value = 0;
    loadData();
});

const handlePageChange = (page: number) => {
    currentPage.value = page;
    loadData();
};

const triggerFileInput = () => {
    fileInput.value?.click();
};

const handleFileUpload = async (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
        const file = target.files[0];
        try {
            await importService.importDiscounts(file!);
            alert('Import successful!');
            await loadData();
        } catch (error) {
            console.error('Import failed', error);
            alert('Import failed');
        } finally {
            if (fileInput.value) fileInput.value.value = '';
        }
    }
};

const handleSubmit = async () => {
    if (!formData.value.productId) return;

    try {
        const payload = {
            productId: formData.value.productId,
            discountPercent: formData.value.discountPercent,
            startDate: formData.value.startDate,
            endDate: formData.value.endDate,
            active: formData.value.active
        };

        if (editingDiscount.value) {
            if (editingDiscount.value.id !== undefined) {
                await discountService.updateDiscount(editingDiscount.value.id, { ...editingDiscount.value, ...payload });
            }
        } else {
            await discountService.createDiscount(payload);
        }
        await loadData();
        closeForm();
    } catch (error) {
        console.error('Error saving discount:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa giảm giá này?')) return;

    try {
        await discountService.deleteDiscount(id);
        await loadData();
    } catch (error) {
        console.error('Error deleting discount:', error);
    }
};

const openForm = (discount?: DiscountDTO) => {
    if (discount) {
        editingDiscount.value = discount;
        formData.value = {
            productId: discount.productId,
            discountPercent: discount.discountPercent,
            startDate: discount.startDate ? discount.startDate.slice(0, 16) : '',
            endDate: discount.endDate ? discount.endDate.slice(0, 16) : '',
            active: discount.active
        };
    } else {
        editingDiscount.value = null;
        formData.value = {
            productId: products.value.length > 0 && products.value[0] ? products.value[0].id : 0,
            discountPercent: 0,
            startDate: '',
            endDate: '',
            active: true
        };
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingDiscount.value = null;
    formData.value = {
        productId: products.value.length > 0 && products.value[0] ? products.value[0].id : 0,
        discountPercent: 0,
        startDate: '',
        endDate: '',
        active: true
    };
};

onMounted(() => {
    loadData();
});
</script>

<template>
    <div class="discount-view">
        <div class="page-header">
            <div class="page-title">
                <h2>Giảm giá</h2>
            </div>
            <button class="btn btn-primary btn-add" @click="openForm()">
                <i class="pi pi-plus"></i>
                <span>Thêm giảm giá</span>
            </button>
        </div>

        <div class="action-bar card">
            <div class="filter-group">
                <div class="search-wrapper">
                    <i class="pi pi-search search-icon"></i>
                    <input type="text" v-model="searchQuery" placeholder="Tìm sản phẩm..." class="search-input" />
                </div>

                <div class="select-group">
                    <div class="select-wrapper">
                        <select v-model="activeFilter" class="filter-select">
                            <option value="all">Tất cả trạng thái</option>
                            <option value="active">Đang hoạt động</option>
                            <option value="inactive">Tạm ngưng</option>
                        </select>
                        <i class="pi pi-chevron-down select-icon"></i>
                    </div>
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
                <button class="btn btn-outline" @click="importService.downloadTemplate('discounts')" title="Tải mẫu">
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
                        <th>Sản phẩm</th>
                        <th>Phần trăm</th>
                        <th>Bắt đầu</th>
                        <th>Kết thúc</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="discount in discounts" :key="discount.id">
                        <td>{{ discount.id }}</td>
                        <td><strong>{{ discount.productName || discount.productId }}</strong></td>
                        <td>
                          <span class="badge badge-danger">-{{ discount.discountPercent }}%</span>
                        </td>
                        <td>{{ new Date(discount.startDate).toLocaleDateString() }}</td>
                        <td>{{ new Date(discount.endDate).toLocaleDateString() }}</td>
                        <td>
                            <span class="badge" :class="{ 'badge-success': discount.active, 'badge-warning': !discount.active }">
                                {{ discount.active ? 'Hoạt động' : 'Tạm ngưng' }}
                            </span>
                        </td>
                        <td>
                            <button class="btn-text" @click="openForm(discount)">Sửa</button>
                            <button class="btn-text text-danger" @click="handleDelete(discount.id)">Xóa</button>
                        </td>
                    </tr>
                    <tr v-if="discounts.length === 0">
                        <td colspan="7" class="text-center">Không tìm thấy giảm giá nào.</td>
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
                <h3>{{ editingDiscount ? 'Chỉnh sửa' : 'Thêm mới' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="productId">Sản phẩm áp dụng</label>
                        <select id="productId" v-model="formData.productId" required>
                            <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="discountPercent">Phần trăm giảm (%)</label>
                        <input id="discountPercent" v-model="formData.discountPercent" type="number" step="0.1"
                            required />
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="startDate">Ngày bắt đầu</label>
                            <input id="startDate" v-model="formData.startDate" type="datetime-local" required />
                        </div>
                        <div class="form-group">
                            <label for="endDate">Ngày kết thúc</label>
                            <input id="endDate" v-model="formData.endDate" type="datetime-local" required />
                        </div>
                    </div>
                    <div class="form-group checkbox">
                        <label>
                            <input type="checkbox" v-model="formData.active" />
                            Kích hoạt chương trình
                        </label>
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

.checkbox {
    flex-direction: row;
    align-items: center;
    gap: 0.75rem;
}

.checkbox input {
    width: 18px;
    height: 18px;
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
