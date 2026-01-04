<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import colorService, { type ColorDTO } from '../../services/colorService';
import importService from '../../services/importService';

const colors = ref<ColorDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingColor = ref<ColorDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const searchQuery = ref('');

const filteredColors = computed(() => {
    if (!searchQuery.value) return colors.value;
    const lowerQuery = searchQuery.value.toLowerCase();
    return colors.value.filter(color =>
        color.colorName.toLowerCase().includes(lowerQuery) ||
        color.hexCode.toLowerCase().includes(lowerQuery) ||
        color.id.toString().includes(lowerQuery)
    );
});

const formData = ref({ colorName: '', hexCode: '' });

const fetchColors = async () => {
    isLoading.value = true;
    try {
        const response = await colorService.getAllColors();
        colors.value = response.data;
    } catch (error) {
        console.error('Error fetching colors:', error);
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
            await importService.importColors(file!);
            alert('Import successful!');
            await fetchColors();
        } catch (error) {
            console.error('Import failed', error);
            alert('Import failed');
        } finally {
            if (fileInput.value) fileInput.value.value = '';
        }
    }
};

const handleSubmit = async () => {
    if (!formData.value.colorName || !formData.value.hexCode) return;

    try {
        const payload = {
            colorName: formData.value.colorName,
            hexCode: formData.value.hexCode
        };

        if (editingColor.value) {
            await colorService.updateColor(editingColor.value.id, { ...editingColor.value, ...payload });
        } else {
            await colorService.createColor(payload);
        }
        await fetchColors();
        closeForm();
    } catch (error) {
        console.error('Error saving color:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this color?')) return;

    try {
        await colorService.deleteColor(id);
        await fetchColors();
    } catch (error) {
        console.error('Error deleting color:', error);
    }
};

const openForm = (color?: ColorDTO) => {
    if (color) {
        editingColor.value = color;
        formData.value = {
            colorName: color.colorName,
            hexCode: color.hexCode
        };
    } else {
        editingColor.value = null;
        formData.value = {
            colorName: '',
            hexCode: '#000000'
        };
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingColor.value = null;
};

onMounted(() => {
    fetchColors();
});
</script>

<template>
    <div class="color-view">
        <div class="header">
            <h2>Colors</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search colors..." class="search-input" />
                <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none"
                    accept=".xlsx, .xls" />
                <button class="btn btn-secondary" @click="importService.downloadTemplate('colors')"
                    style="margin-right: 0.5rem">Download Template</button>
                <button class="btn btn-secondary" @click="triggerFileInput" style="margin-right: 0.5rem">Import
                    Excel</button>
                <button class="btn btn-primary" @click="openForm()">Add Color</button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Loading...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Hex Code</th>
                        <th>Preview</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="color in filteredColors" :key="color.id">
                        <td>{{ color.id }}</td>
                        <td>{{ color.colorName }}</td>
                        <td>{{ color.hexCode }}</td>
                        <td>
                            <div class="color-preview" :style="{ backgroundColor: color.hexCode }"
                                :title="color.hexCode"></div>
                        </td>
                        <td>
                            <button class="btn-text" @click="openForm(color)">Edit</button>
                            <button class="btn-text text-danger" @click="handleDelete(color.id)">Delete</button>
                        </td>
                    </tr>
                    <tr v-if="filteredColors.length === 0">
                        <td colspan="5" class="text-center">No colors found.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingColor ? 'Edit Color' : 'Add Color' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="colorName">Color Name</label>
                        <input id="colorName" v-model="formData.colorName" type="text" required />
                    </div>
                    <div class="form-group">
                        <label for="hexCode">Hex Code</label>
                        <div style="display: flex; gap: 0.5rem;">
                            <input id="hexCode" v-model="formData.hexCode" type="text" required style="flex:1" />
                            <input type="color" v-model="formData.hexCode"
                                style="height: 38px; width: 60px; padding: 0; border: none;" />
                        </div>
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

.color-preview {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    border: 1px solid var(--border-color);
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
