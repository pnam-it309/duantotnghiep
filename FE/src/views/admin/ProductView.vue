<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { RouterLink } from 'vue-router';
import productService, { type ProductDTO } from '../../services/productService';
import brandService, { type BrandDTO } from '../../services/brandService';
import categoryService, { type CategoryDTO } from '../../services/categoryService';
import importService from '../../services/importService';

const products = ref<ProductDTO[]>([]);
const brands = ref<BrandDTO[]>([]);
const categories = ref<CategoryDTO[]>([]);

const isLoading = ref(false);
const showForm = ref(false);
const editingProduct = ref<ProductDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

// Filters
const searchQuery = ref('');
const selectedBrandId = ref(0);
const selectedCategoryId = ref(0);
const selectedActiveStatus = ref('all');

const filteredProducts = computed(() => {
  return products.value.filter(product => {
    // Search Filter
    const query = searchQuery.value.toLowerCase();
    const matchesSearch = !query ||
      product.name.toLowerCase().includes(query) ||
      product.description?.toLowerCase().includes(query) ||
      product.id.toString().includes(query);

    // Brand Filter
    const matchesBrand = selectedBrandId.value === 0 || product.brandId === selectedBrandId.value;

    // Category Filter
    const matchesCategory = selectedCategoryId.value === 0 || product.categoryId === selectedCategoryId.value;

    // Status Filter
    const matchesStatus = selectedActiveStatus.value === 'all' ||
      (selectedActiveStatus.value === 'active' && product.active) ||
      (selectedActiveStatus.value === 'inactive' && !product.active);

    return matchesSearch && matchesBrand && matchesCategory && matchesStatus;
  });
});

const formData = ref({
  name: '',
  description: '',
  categoryId: 0,
  brandId: 0,
  active: true,
});

const loadData = async () => {
  isLoading.value = true;
  try {
    const [productsRes, brandsRes, categoriesRes] = await Promise.all([
      productService.getAllProducts(),
      brandService.getAllBrands(),
      categoryService.getAllCategories(),
    ]);
    products.value = productsRes.data;
    brands.value = brandsRes.data;
    categories.value = categoriesRes.data;
  } catch (error) {
    console.error('Error loading data:', error);
  } finally {
    isLoading.value = false;
  }
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
    alert('Please fill in Name, Brand, and Category.');
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
  if (!confirm('Are you sure you want to delete this product?')) return;

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

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="product-view">
    <div class="header">
      <h2>Products</h2>
      <div class="actions">
        <input type="text" v-model="searchQuery" placeholder="Search products..." class="search-input" />

        <select v-model="selectedBrandId" class="filter-select">
          <option :value="0">All Brands</option>
          <option v-for="brand in brands" :key="brand.id" :value="brand.id">{{ brand.name }}</option>
        </select>

        <select v-model="selectedCategoryId" class="filter-select">
          <option :value="0">All Categories</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
        </select>

        <select v-model="selectedActiveStatus" class="filter-select">
          <option value="all">All Status</option>
          <option value="active">Active</option>
          <option value="inactive">Inactive</option>
        </select>

        <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none" accept=".xlsx, .xls" />
        <button class="btn btn-secondary" @click="importService.downloadTemplate('products')"
          style="margin-right: 0.5rem">Download Template</button>
        <button class="btn btn-secondary" @click="triggerFileInput" style="margin-right: 0.5rem">Import Excel</button>
        <button class="btn btn-primary" @click="openForm()">Add Product</button>
      </div>
    </div>

    <div v-if="isLoading" class="loading">Loading...</div>

    <div v-else class="card table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Brand</th>
            <th>Category</th>
            <th>Active</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="product in filteredProducts" :key="product.id">
            <td>{{ product.id }}</td>
            <td>{{ product.name }}</td>
            <td>{{ product.brandName }}</td>
            <td>{{ product.categoryName }}</td>
            <td>
              <span :class="{ 'badge-success': product.active, 'badge-warning': !product.active }">
                {{ product.active ? 'Active' : 'Inactive' }}
              </span>
            </td>
            <td>
              <button class="btn-text" @click="openForm(product)">Edit</button>
              <RouterLink :to="`/admin/products/${product.id}/variants`" class="btn-text">Variants</RouterLink>
              <button class="btn-text text-danger" @click="handleDelete(product.id)">Delete</button>
            </td>
          </tr>
          <tr v-if="filteredProducts.length === 0">
            <td colspan="6" class="text-center">No products found.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal Overlay -->
    <div v-if="showForm" class="modal-overlay">
      <div class="modal card">
        <h3>{{ editingProduct ? 'Edit Product' : 'Add Product' }}</h3>
        <form @submit.prevent="handleSubmit" class="form">

          <div class="form-group">
            <label for="name">Product Name</label>
            <input id="name" v-model="formData.name" type="text" required />
          </div>

          <div class="form-group">
            <label for="description">Description</label>
            <textarea id="description" v-model="formData.description" rows="3"></textarea>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label for="brand">Brand</label>
              <select id="brand" v-model="formData.brandId" required>
                <option v-for="b in brands" :key="b.id" :value="b.id">{{ b.name }}</option>
              </select>
            </div>

            <div class="form-group">
              <label for="category">Category</label>
              <select id="category" v-model="formData.categoryId" required>
                <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
          </div>

          <div class="form-group checkbox">
            <label>
              <input type="checkbox" v-model="formData.active" />
              Active
            </label>
          </div>

          <div class="form-actions">
            <button type="button" class="btn" @click="closeForm">Cancel</button>
            <button type="submit" class="btn btn-primary">Save</button>
          </div>
        </form>
      </div>
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

.filter-select {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}

.loading {
  text-align: center;
  padding: 2rem;
  color: var(--color-text-muted);
}

.text-center {
  text-align: center;
}

.btn-text {
  background: none;
  border: none;
  color: var(--color-primary);
  opacity: 0.9;
  padding: 0 0.5rem;
}

.btn-text:hover {
  opacity: 1;
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
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  width: 100%;
  max-width: 500px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1rem;
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
  font-size: 0.875rem;
  font-weight: 500;
}

input,
select,
textarea {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}

.checkbox {
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1rem;
}

.badge-success {
  color: var(--color-secondary);
  font-weight: 600;
}

.badge-warning {
  color: var(--color-warning);
  font-weight: 600;
}
</style>
