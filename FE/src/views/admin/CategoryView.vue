<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import categoryService, { type CategoryDTO } from '../../services/categoryService';
import importService from '../../services/importService';

const categories = ref<CategoryDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingCategory = ref<CategoryDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const searchQuery = ref('');

const filteredCategories = computed(() => {
    if (!searchQuery.value) return categories.value;
    const lowerQuery = searchQuery.value.toLowerCase();
    return categories.value.filter(category =>
        category.name.toLowerCase().includes(lowerQuery) ||
        category.id.toString().includes(lowerQuery)
    );
});

const formData = ref({
    name: '',
});

const fetchCategories = async () => {
    isLoading.value = true;
    try {
        const response = await categoryService.getAllCategories();
        categories.value = response.data;
    } catch (error) {
        console.error('Error fetching categories:', error);
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
            await importService.importCategories(file!);
            alert('Import successful!');
            await fetchCategories();
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
        if (editingCategory.value) {
            await categoryService.updateCategory(editingCategory.value.id, { ...editingCategory.value, name: formData.value.name });
        } else {
            await categoryService.createCategory({ name: formData.value.name });
        }
        await fetchCategories();
        closeForm();
    } catch (error) {
        console.error('Error saving category:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this category?')) return;

    try {
        await categoryService.deleteCategory(id);
        await fetchCategories();
    } catch (error) {
        console.error('Error deleting category:', error);
    }
};

const openForm = (category?: CategoryDTO) => {
    if (category) {
        editingCategory.value = category;
        formData.value.name = category.name;
    } else {
        editingCategory.value = null;
        formData.value.name = '';
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingCategory.value = null;
    formData.value.name = '';
};

onMounted(() => {
    fetchCategories();
});
</script>
<template>
    <div class="category-view">
        <div class="header">
            <h2>Categories</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search categories..." class="search-input" />
                <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none"
                    accept=".xlsx, .xls" />
                <button class="btn btn-secondary" @click="importService.downloadTemplate('categories')"
                    style="margin-right: 0.5rem">Download Template</button>
                <button class="btn btn-secondary" @click="triggerFileInput" style="margin-right: 0.5rem">Import
                    Excel</button>
                <button class="btn btn-primary" @click="openForm()">Add Category</button>
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
                    <tr v-for="category in filteredCategories" :key="category.id">
                        <td>{{ category.id }}</td>
                        <td>{{ category.name }}</td>
                        <td>
                            <button class="btn-text" @click="openForm(category)">Edit</button>
                            <button class="btn-text text-danger" @click="handleDelete(category.id)">Delete</button>
                        </td>
                    </tr>
                    <tr v-if="filteredCategories.length === 0">
                        <td colspan="3" class="text-center">No categories found.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingCategory ? 'Edit Category' : 'Add Category' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="name">Category Name</label>
                        <input id="name" v-model="formData.name" type="text" placeholder="Enter category name"
                            required />
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
/* Reuse styles from BrandView or move to global css ideally, but scopping here for safety */
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
    padding: 0 0.5rem;
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
}

.form-group input {
    padding: 0.625rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
}
</style>
