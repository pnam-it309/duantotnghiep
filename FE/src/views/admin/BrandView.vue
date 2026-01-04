<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import brandService, { type BrandDTO } from '../../services/brandService';
import importService from '../../services/importService';

const brands = ref<BrandDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingBrand = ref<BrandDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const searchQuery = ref('');

const filteredBrands = computed(() => {
  if (!searchQuery.value) return brands.value;
  const lowerQuery = searchQuery.value.toLowerCase();
  return brands.value.filter(brand =>
    brand.name.toLowerCase().includes(lowerQuery) ||
    brand.id.toString().includes(lowerQuery)
  );
});

const formData = ref({
  name: '',
});

const fetchBrands = async () => {
  isLoading.value = true;
  try {
    const response = await brandService.getAllBrands();
    brands.value = response.data;
  } catch (error) {
    console.error('Error fetching brands:', error);
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
  if (!confirm('Are you sure you want to delete this brand?')) return;

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
    <div class="header">
      <h2>Brands</h2>
      <div class="actions">
        <input type="text" v-model="searchQuery" placeholder="Search brands..." class="search-input" />
        <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none" accept=".xlsx, .xls" />
        <button class="btn btn-secondary" @click="importService.downloadTemplate('brands')"
          style="margin-right: 0.5rem">Download Template</button>
        <button class="btn btn-secondary" @click="triggerFileInput" style="margin-right: 0.5rem">Import Excel</button>
        <button class="btn btn-primary" @click="openForm()">Add Brand</button>
      </div>
    </div>

    <div v-if="isLoading" class="loading">Loading...</div>

    <div v-else class="card table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="brand in filteredBrands" :key="brand.id">
            <td>{{ brand.id }}</td>
            <td>{{ brand.name }}</td>
            <td>
              <button class="btn-text" @click="openForm(brand)">Edit</button>
              <button class="btn-text text-danger" @click="handleDelete(brand.id)">Delete</button>
            </td>
          </tr>
          <tr v-if="filteredBrands.length === 0">
            <td colspan="3" class="text-center">No brands found.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal Overlay -->
    <div v-if="showForm" class="modal-overlay">
      <div class="modal card">
        <h3>{{ editingBrand ? 'Edit Brand' : 'Add Brand' }}</h3>
        <form @submit.prevent="handleSubmit" class="form">
          <div class="form-group">
            <label for="name">Brand Name</label>
            <input id="name" v-model="formData.name" type="text" placeholder="Enter brand name" required />
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
}

.search-input {
  padding: 0.5rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  margin-right: 0.5rem;
  min-width: 200px;
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
  font-weight: 500;
  padding: 0 0.5rem;
}

.btn-text:hover {
  text-decoration: underline;
}

.btn-text.text-danger {
  color: var(--color-danger);
}

/* Modal Styles */
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
  max-width: 400px;
}

.form {
  margin-top: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text-main);
}

.form-group input {
  padding: 0.625rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
}

.form-group input:focus {
  outline: 2px solid var(--color-primary);
  border-color: transparent;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}
</style>
