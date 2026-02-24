<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { RouterLink } from 'vue-router';
import productService, { type ProductDTO } from '../../services/productService';
import brandService, { type BrandDTO } from '../../services/brandService';
import categoryService, { type CategoryDTO } from '../../services/categoryService';
import importService from '../../services/importService';
import Pagination from '../../components/Pagination.vue';
import ScanQRModal from '../../components/ScanQRModal.vue';

const products = ref<ProductDTO[]>([]);
const brands = ref<BrandDTO[]>([]);
const categories = ref<CategoryDTO[]>([]);

const isLoading = ref(false);
const showForm = ref(false);
const editingProduct = ref<ProductDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const scanModal = ref<any>(null);

// Pagination state
const currentPage = ref(0);
const totalPages = ref(0);
const totalElements = ref(0);
const pageSize = ref(5);

// Filters
const searchQuery = ref('');
const selectedBrandId = ref(0);
const selectedCategoryId = ref(0);

const loadData = async () => {
  isLoading.value = true;
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value || undefined,
      brandId: selectedBrandId.value || undefined,
      categoryId: selectedCategoryId.value || undefined,
    };

    const [productsRes, brandsRes, categoriesRes] = await Promise.all([
      productService.searchProducts(params),
      brandService.getAllBrands(),
      categoryService.getAllCategories(),
    ]);

    products.value = productsRes.data.content;
    totalPages.value = productsRes.data.totalPages;
    totalElements.value = productsRes.data.totalElements;
    
    brands.value = brandsRes.data;
    categories.value = categoriesRes.data;
  } catch (error) {
    console.error('Error loading data:', error);
  } finally {
    isLoading.value = false;
  }
};

watch([searchQuery, selectedBrandId, selectedCategoryId], () => {
  currentPage.value = 0;
  loadData();
});

const handlePageChange = (page: number) => {
  currentPage.value = page;
  loadData();
};

const handleQRScanned = (code: string) => {
  searchQuery.value = code;
  alert('Scanned QR: ' + code + '. Searching...');
};

const exportExcel = () => {
  window.open(`${import.meta.env.VITE_API_BASE_URL || '/api'}/export/products/excel`, '_blank');
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const handleFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  if (target.files && target.files.length > 0) {
    const file = target.files[0];
    try {
      await importService.importProducts(file!);
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
  if (!formData.value.name || !formData.value.categoryId || !formData.value.brandId) {
    alert('Vui lòng điền đầy đủ Tên, Thương hiệu và Danh mục.');
    return;
  }

  try {
    const payload = {
      name: formData.value.name,
      description: formData.value.description,
      categoryId: formData.value.categoryId,
      brandId: formData.value.brandId,
      active: formData.value.active,
    };

    if (editingProduct.value) {
      if (editingProduct.value.id !== undefined) {
        await productService.updateProduct(editingProduct.value.id, {
          ...editingProduct.value,
          ...payload
        });
      }
    } else {
      await productService.createProduct(payload);
    }
    await loadData();
    closeForm();
  } catch (error) {
    console.error('Error saving product:', error);
  }
};

const handleDelete = async (id: number) => {
  if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return;

  try {
    await productService.deleteProduct(id);
    await loadData();
  } catch (error) {
    console.error('Error deleting product:', error);
  }
};

const openForm = (product?: ProductDTO) => {
  if (product) {
    editingProduct.value = product;
    formData.value = {
      name: product.name,
      description: product.description,
      categoryId: product.categoryId,
      brandId: product.brandId,
      active: product.active,
    };
  } else {
    editingProduct.value = null;
    formData.value = {
      name: '',
      description: '',
      categoryId: categories.value.length > 0 && categories.value[0] ? categories.value[0].id : 0,
      brandId: brands.value.length > 0 && brands.value[0] ? brands.value[0].id : 0,
      active: true,
    };
  }
  showForm.value = true;
};

const closeForm = () => {
  showForm.value = false;
  editingProduct.value = null;
};

const formData = ref({
  name: '',
  description: '',
  categoryId: 0,
  brandId: 0,
  active: true,
});

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="product-view">
    <div class="page-header">
      <div class="page-title">
        <h2>Quản lý sản phẩm</h2>
      </div>
      <button class="btn btn-primary btn-add" @click="openForm()">
        <i class="pi pi-plus"></i>
        <span>Thêm sản phẩm</span>
      </button>
    </div>

    <!-- Action Bar -->
    <div class="action-bar card">
      <div class="filter-group">
        <div class="search-wrapper">
          <i class="pi pi-search search-icon"></i>
          <input type="text" v-model="searchQuery" placeholder="Tìm tên, mã sản phẩm..." class="search-input" />
        </div>

        <button class="btn btn-outline btn-scan" @click="scanModal.open()" title="Quét mã QR">
          <i class="pi pi-qrcode"></i>
          <span>Quét mã</span>
        </button>

        <div class="select-group">
          <div class="select-wrapper">
            <select v-model="selectedBrandId" class="filter-select">
              <option :value="0">Tất cả thương hiệu</option>
              <option v-for="brand in brands" :key="brand.id" :value="brand.id">{{ brand.name }}</option>
            </select>
            <i class="pi pi-chevron-down select-icon"></i>
          </div>

          <div class="select-wrapper">
            <select v-model="selectedCategoryId" class="filter-select">
              <option :value="0">Tất cả danh mục</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
            </select>
            <i class="pi pi-chevron-down select-icon"></i>
          </div>
        </div>
      </div>

      <div class="button-group">
        <div class="import-export">
          <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none" accept=".xlsx, .xls" />
          <button class="btn btn-outline" @click="exportExcel()" title="Xuất Excel">
            <i class="pi pi-file-export"></i>
            <span>Xuất file</span>
          </button>
          <button class="btn btn-outline" @click="triggerFileInput()" title="Nhập Excel">
            <i class="pi pi-file-import"></i>
            <span>Nhập file</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="isLoading" class="loading">Đang tải...</div>

    <div v-else class="card table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên sản phẩm</th>
            <th>Thương hiệu</th>
            <th>Danh mục</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in products" :key="product.id">
            <td>{{ product.id }}</td>
            <td>{{ product.name }}</td>
            <td>{{ product.brandName }}</td>
            <td>{{ product.categoryName }}</td>
            <td>
              <span class="badge" :class="{ 'badge-success': product.active, 'badge-warning': !product.active }">
                {{ product.active ? 'Hoạt động' : 'Tạm ngưng' }}
              </span>
            </td>
            <td>
              <button class="btn-text" @click="openForm(product)">Sửa</button>
              <RouterLink :to="`/admin/products/${product.id}/variants`" class="btn-text">Phiên bản</RouterLink>
              <button class="btn-text text-danger" @click="handleDelete(product.id)">Xóa</button>
            </td>
          </tr>
          <tr v-if="products.length === 0">
            <td colspan="6" class="text-center">Không tìm thấy sản phẩm nào.</td>
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

    <!-- Scan Modal -->
    <ScanQRModal ref="scanModal" @scanned="handleQRScanned" />

    <!-- Modal Overlay -->
    <div v-if="showForm" class="modal-overlay">
      <div class="modal card">
        <h3>{{ editingProduct ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới' }}</h3>
        <form @submit.prevent="handleSubmit" class="form">

          <div class="form-group">
            <label for="name">Tên sản phẩm</label>
            <input id="name" v-model="formData.name" type="text" required />
          </div>

          <div class="form-group">
            <label for="description">Mô tả</label>
            <textarea id="description" v-model="formData.description" rows="3"></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="brand">Thương hiệu</label>
              <select id="brand" v-model="formData.brandId" required>
                <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
              </select>
            </div>

            <div class="form-group">
              <label for="category">Danh mục</label>
              <select id="category" v-model="formData.categoryId" required>
                <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
          </div>

          <div class="form-group checkbox">
            <label>
              <input type="checkbox" v-model="formData.active" />
              Kích hoạt
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

input,
select,
textarea {
  padding: 0.625rem 0.875rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 0.9375rem;
  transition: all 0.2s;
}

input:focus, select:focus, textarea:focus {
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
  margin: 0;
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
