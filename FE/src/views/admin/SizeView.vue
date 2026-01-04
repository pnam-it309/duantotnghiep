<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import sizeService, { type SizeDTO } from '../../services/sizeService';
import importService from '../../services/importService';

const sizes = ref<SizeDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingSize = ref<SizeDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const searchQuery = ref('');

const filteredSizes = computed(() => {
    if (!searchQuery.value) return sizes.value;
    const lowerQuery = searchQuery.value.toLowerCase();
    return sizes.value.filter(size =>
        size.sizeValue.toLowerCase().includes(lowerQuery) ||
        size.id.toString().includes(lowerQuery)
    );
});

const formData = ref({ sizeValue: '' });

const fetchSizes = async () => {
    isLoading.value = true;
    try {
        const response = await sizeService.getAllSizes();
        sizes.value = response.data;
    } catch (error) {
        console.error('Error fetching sizes:', error);
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
            await importService.importSizes(file!);
            alert('Import successful!');
            await fetchSizes();
        } catch (error) {
            console.error('Import failed', error);
            alert('Import failed');
        } finally {
            if (fileInput.value) fileInput.value.value = '';
        }
    }
};

const handleSubmit = async () => {
    if (!formData.value.sizeValue) return;

    try {
        if (editingSize.value) {
            await sizeService.updateSize(editingSize.value.id, { ...editingSize.value, sizeValue: formData.value.sizeValue });
        } else {
            await sizeService.createSize({ sizeValue: formData.value.sizeValue });
        }
        await fetchSizes();
        closeForm();
    } catch (error) {
        console.error('Error saving size:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this size?')) return;

    try {
        await sizeService.deleteSize(id);
        await fetchSizes();
    } catch (error) {
        console.error('Error deleting size:', error);
    }
};

const openForm = (size?: SizeDTO) => {
    if (size) {
        editingSize.value = size;
        formData.value.sizeValue = size.sizeValue;
    } else {
        editingSize.value = null;
        formData.value.sizeValue = '';
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingSize.value = null;
    formData.value.sizeValue = ''; // Added to ensure reset, though original didn't have it, I am replacing the whole block so it's fine to add it in ReplacementContent
};

onMounted(() => {
    fetchSizes();
});
</script>

<template>
    <div class="size-view">
        <div class="header">
            <h2>Sizes</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search sizes..." class="search-input" />
                <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none"
                    accept=".xlsx, .xls" />
                <button class="btn btn-secondary" @click="importService.downloadTemplate('sizes')"
                    style="margin-right: 0.5rem">Download Template</button>
                <button class="btn btn-secondary" @click="triggerFileInput" style="margin-right: 0.5rem">Import
                    Excel</button>
                <button class="btn btn-primary" @click="openForm()">Add Size</button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Loading...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Size Value</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="size in filteredSizes" :key="size.id">
                        <td>{{ size.id }}</td>
                        <td>{{ size.sizeValue }}</td>
                        <td>
                            <button class="btn-text" @click="openForm(size)">Edit</button>
                            <button class="btn-text text-danger" @click="handleDelete(size.id)">Delete</button>
                        </td>
                    </tr>
                    <tr v-if="filteredSizes.length === 0">
                        <td colspan="3" class="text-center">No sizes found.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingSize ? 'Edit Size' : 'Add Size' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="sizeValue">Size Value</label>
                        <input id="sizeValue" v-model="formData.sizeValue" type="text" placeholder="e.g. XL, 42..."
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
    gap: 1rem;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

input {
    padding: 0.625rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1rem;
}
</style>
