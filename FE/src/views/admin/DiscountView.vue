<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import discountService, { type DiscountDTO } from '../../services/discountService';
import productService, { type ProductDTO } from '../../services/productService';
import importService from '../../services/importService';

const discounts = ref<DiscountDTO[]>([]);
const products = ref<ProductDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingDiscount = ref<DiscountDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

// Filters
const searchQuery = ref('');
const activeFilter = ref('all');

const filteredDiscounts = computed(() => {
    return discounts.value.filter(discount => {
        const query = searchQuery.value.toLowerCase();
        const productName = discount.productName ? discount.productName.toLowerCase() : '';
        const matchesSearch = !query ||
            productName.includes(query) ||
            discount.id.toString().includes(query);

        const matchesStatus = activeFilter.value === 'all' ||
            (activeFilter.value === 'active' && discount.active) ||
            (activeFilter.value === 'inactive' && !discount.active);

        return matchesSearch && matchesStatus;
    });
});

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
        const [discountsRes, productsRes] = await Promise.all([
            discountService.getAllDiscounts(),
            productService.getAllProducts()
        ]);
        discounts.value = discountsRes.data;
        products.value = productsRes.data;
    } catch (error) {
        console.error('Error loading discounts:', error);
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
            startDate: formData.value.startDate ? new Date(formData.value.startDate).toISOString() : new Date().toISOString(),
            endDate: formData.value.endDate ? new Date(formData.value.endDate).toISOString() : new Date().toISOString(),
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
    if (!confirm('Are you sure you want to delete this discount?')) return;

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
        <div class="header">
            <h2>Discounts</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search product..." class="search-input" />

                <select v-model="activeFilter" class="filter-select">
                    <option value="all">All Status</option>
                    <option value="active">Active</option>
                    <option value="inactive">Inactive</option>
                </select>

                <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none"
                    accept=".xlsx, .xls" />
                <button class="btn btn-secondary" @click="importService.downloadTemplate('discounts')"
                    style="margin-right: 0.5rem">Download Template</button>
                <button class="btn btn-secondary" @click="triggerFileInput" style="margin-right: 0.5rem">Import
                    Excel</button>
                <button class="btn btn-primary" @click="openForm()">Add Discount</button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Loading...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Product</th>
                        <th>Percent</th>
                        <th>Start</th>
                        <th>End</th>
                        <th>Active</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="discount in filteredDiscounts" :key="discount.id">
                        <td>{{ discount.id }}</td>
                        <td>{{ discount.productName || discount.productId }}</td>
                        <td>{{ discount.discountPercent }}%</td>
                        <td>{{ new Date(discount.startDate).toLocaleDateString() }}</td>
                        <td>{{ new Date(discount.endDate).toLocaleDateString() }}</td>
                        <td>
                            <span :class="{ 'badge-success': discount.active, 'badge-warning': !discount.active }">
                                {{ discount.active ? 'Yes' : 'No' }}
                            </span>
                        </td>
                        <td>
                            <button class="btn-text" @click="openForm(discount)">Edit</button>
                            <button class="btn-text text-danger" @click="handleDelete(discount.id)">Delete</button>
                        </td>
                    </tr>
                    <tr v-if="filteredDiscounts.length === 0">
                        <td colspan="7" class="text-center">No discounts found.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingDiscount ? 'Edit Discount' : 'Add Discount' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="productId">Product</label>
                        <select id="productId" v-model="formData.productId" required>
                            <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="discountPercent">Discount Percent %</label>
                        <input id="discountPercent" v-model="formData.discountPercent" type="number" step="0.1"
                            required />
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="startDate">Start Date</label>
                            <input id="startDate" v-model="formData.startDate" type="datetime-local" required />
                        </div>
                        <div class="form-group">
                            <label for="endDate">End Date</label>
                            <input id="endDate" v-model="formData.endDate" type="datetime-local" required />
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
    margin-right: 0.5rem;
    min-width: 200px;
}

.filter-select {
    padding: 0.5rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-right: 0.5rem;
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
    max-width: 500px;
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

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

input,
select {
    padding: 0.625rem;
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
    gap: 0.75rem;
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
