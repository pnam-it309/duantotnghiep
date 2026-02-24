<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import brandService, { type BrandDTO } from '../../services/brandService';
import importService from '../../services/importService';
import Pagination from '../../components/Pagination.vue';

const brands = ref<BrandDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingBrand = ref<BrandDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
// Search & Pagination
const searchQuery = ref('');
const currentPage = ref(0);
const pageSize = ref(5);
const totalPages = ref(0);
const totalElements = ref(0);

const formData = ref({
  name: '',
});

const fetchBrands = async () => {
  isLoading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value || undefined
    };
    const response = await brandService.getBrands(params);
    brands.value = response.data.content;
    totalPages.value = response.data.totalPages;
    totalElements.value = response.data.totalElements;
  } catch (error) {
    console.error('Error fetching brands:', error);
  } finally {
    isLoading.value = false;
  }
};

watch(searchQuery, () => {
  currentPage.value = 0;
  fetchBrands();
});

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchBrands();
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const handleFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    const file = target.files[0];
    try {
      await importService.importBrands(file!);
      alert('Import successful!');
      await fetchBrands();
    } catch (error) {
      console.error('Import failed', error);
      alert('Import failed');
    } finally {
      if (fileInput.value) fileInput.value.value = '';
    }
  }
};


const handleSubmit = async () => {
  if (!formData.value.name) return;

  try {
    if (editingBrand.value) {
      await brandService.updateBrand(editingBrand.value.id, { ...editingBrand.value, name: formData.value.name });
    } else {
      await brandService.createBrand({ name: formData.value.name });
    }
    await fetchBrands();
    closeForm();
  } catch (error) {
    console.error('Error saving brand:', error);
  }
};

const handleDelete = async (id: number) => {
  if (!confirm('Bạn có chắc chắn muốn xóa thương hiệu này?')) return;

  try {
    await brandService.deleteBrand(id);
    await fetchBrands();
  } catch (error) {
    console.error('Error deleting brand:', error);
  }
};

const openForm = (brand?: BrandDTO) => {
  if (brand) {
    editingBrand.value = brand;
    formData.value.name = brand.name;
  } else {
    editingBrand.value = null;
    formData.value.name = '';
  }
  showForm.value = true;
};

const closeForm = () => {
  showForm.value = false;
  editingBrand.value = null;
  formData.value.name = '';
};

onMounted(() => {
  fetchBrands();
});
</script>

<template>
  <div class="brand-view">
    <div class="page-header">
      <div class="page-title">
        <h2>Thương hiệu</h2>
      </div>
      <button class="btn btn-primary btn-add" @click="openForm()">
        <i class="pi pi-plus"></i>
        <span>Thêm thương hiệu</span>
      </button>
    </div>

    <div class="action-bar card">
      <div class="filter-group">
        <div class="search-wrapper">
          <i class="pi pi-search search-icon"></i>
          <input type="text" v-model="searchQuery" placeholder="Tìm tên thương hiệu..." class="search-input" />
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
        <button class="btn btn-outline" @click="importService.downloadTemplate('brands')" title="Tải mẫu">
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
            <th>Tên thương hiệu</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="brand in brands" :key="brand.id">
            <td>{{ brand.id }}</td>
            <td>{{ brand.name }}</td>
            <td>
              <button class="btn-text" @click="openForm(brand)">Sửa</button>
              <button class="btn-text text-danger" @click="handleDelete(brand.id)">Xóa</button>
            </td>
          </tr>
          <tr v-if="brands.length === 0">
            <td colspan="3" class="text-center">Không tìm thấy thương hiệu nào.</td>
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

    <!-- Modal Overlay -->
    <div v-if="showForm" class="modal-overlay">
      <div class="modal card">
        <h3>{{ editingBrand ? 'Chỉnh sửa' : 'Thêm mới' }}</h3>
        <form @submit.prevent="handleSubmit" class="form">
          <div class="form-group">
            <label for="name">Tên thương hiệu</label>
            <input id="name" v-model="formData.name" type="text" placeholder="Nhập tên thương hiệu" required />
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

/* Modal Styles */
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
