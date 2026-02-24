<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import supplierService, { type SupplierDTO } from '../../services/supplierService';
import Pagination from '../../components/Pagination.vue';

const suppliers = ref<SupplierDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingSupplier = ref<SupplierDTO | null>(null);

// Search & Pagination
const searchQuery = ref('');
const currentPage = ref(0);
const pageSize = ref(5);
const totalPages = ref(0);
const totalElements = ref(0);

const formData = ref({
    name: '',
    address: '',
    phoneNumber: '',
    email: ''
});

const fetchSuppliers = async () => {
    isLoading.value = true;
    try {
        const params = {
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchQuery.value || undefined
        };
        const response = await supplierService.getSuppliers(params);
        suppliers.value = response.data.content;
        totalPages.value = response.data.totalPages;
        totalElements.value = response.data.totalElements;
    } catch (error) {
        console.error('Error fetching suppliers:', error);
    } finally {
        isLoading.value = false;
    }
};

watch(searchQuery, () => {
    currentPage.value = 0;
    fetchSuppliers();
});

const handlePageChange = (page: number) => {
    currentPage.value = page;
    fetchSuppliers();
};

const openForm = (supplier?: SupplierDTO) => {
    if (supplier) {
        editingSupplier.value = supplier;
        formData.value = {
            name: supplier.name,
            address: supplier.address || '',
            phoneNumber: supplier.phoneNumber || '',
            email: supplier.email || ''
        };
    } else {
        editingSupplier.value = null;
        formData.value = { name: '', address: '', phoneNumber: '', email: '' };
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingSupplier.value = null;
};

const handleSubmit = async () => {
    try {
        const payload = {
            ...formData.value,
            phoneNumber: formData.value.phoneNumber || undefined,
            email: formData.value.email || undefined
        };
        if (editingSupplier.value) {
            await supplierService.updateSupplier(editingSupplier.value.id, payload);
        } else {
            await supplierService.createSupplier(payload);
        }
        await fetchSuppliers();
        closeForm();
    } catch (error) {
        console.error('Error saving supplier:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa nhà cung cấp này?')) return;
    try {
        await supplierService.deleteSupplier(id);
        await fetchSuppliers();
    } catch (error) {
        console.error('Error deleting supplier:', error);
    }
};

onMounted(() => {
    fetchSuppliers();
});
</script>

<template>
    <div class="supplier-view">
        <div class="page-header">
            <div class="page-title">
                <h2>Nhà cung cấp</h2>
            </div>
            <button class="btn btn-primary btn-add" @click="openForm()">
                <i class="pi pi-plus"></i>
                <span>Thêm nhà cung cấp</span>
            </button>
        </div>

        <div class="action-bar card">
            <div class="filter-group">
                <div class="search-wrapper">
                    <i class="pi pi-search search-icon"></i>
                    <input type="text" v-model="searchQuery" placeholder="Tìm tên, số điện thoại, email..." class="search-input" />
                </div>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Đang tải...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Tên</th>
                        <th>Số điện thoại</th>
                        <th>Email</th>
                        <th>Địa chỉ</th>
                        <th>Hoạt động</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="supplier in suppliers" :key="supplier.id">
                        <td>{{ supplier.id }}</td>
                        <td>{{ supplier.name }}</td>
                        <td>{{ supplier.phoneNumber }}</td>
                        <td>{{ supplier.email }}</td>
                        <td>{{ supplier.address }}</td>
                        <td>
                             <span class="badge" :class="{ 'badge-success': supplier.active, 'badge-warning': !supplier.active }">
                                 {{ supplier.active ? 'Có' : 'Không' }}
                             </span>
                         </td>
                         <td>
                             <button class="btn-text" @click="openForm(supplier)">Sửa</button>
                             <button class="btn-text text-danger" @click="handleDelete(supplier.id)">Xóa</button>
                         </td>
                     </tr>
                     <tr v-if="suppliers.length === 0">
                         <td colspan="7" class="text-center">Không tìm thấy nhà cung cấp nào.</td>
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
                <h3>{{ editingSupplier ? 'Chỉnh sửa' : 'Thêm mới' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label>Tên nhà cung cấp</label>
                        <input v-model="formData.name" type="text" required />
                    </div>
                    <div class="form-group">
                        <label>Số điện thoại</label>
                        <input v-model="formData.phoneNumber" type="text" />
                    </div>
                    <div class="form-group">
                        <label>Email</label>
                        <input v-model="formData.email" type="email" />
                    </div>
                    <div class="form-group">
                        <label>Địa chỉ</label>
                        <input v-model="formData.address" type="text" />
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

/* Modal */
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

label {
    font-size: 0.8125rem;
    font-weight: 600;
    color: var(--color-text-muted);
    text-transform: uppercase;
    letter-spacing: 0.025em;
}

input {
    padding: 0.625rem 0.875rem;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    font-size: 0.9375rem;
    transition: all 0.2s;
}

input:focus {
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
