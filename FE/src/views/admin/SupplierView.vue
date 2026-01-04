<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import supplierService, { type SupplierDTO } from '../../services/supplierService';

const suppliers = ref<SupplierDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingSupplier = ref<SupplierDTO | null>(null);

const searchQuery = ref('');

const filteredSuppliers = computed(() => {
    if (!searchQuery.value) return suppliers.value;
    const query = searchQuery.value.toLowerCase();
    return suppliers.value.filter(s =>
        s.name.toLowerCase().includes(query) ||
        (s.phoneNumber && s.phoneNumber.includes(query)) ||
        s.id.toString().includes(query)
    );
});

const formData = ref({
    name: '',
    address: '',
    phoneNumber: '',
    email: ''
});

const fetchSuppliers = async () => {
    isLoading.value = true;
    try {
        const res = await supplierService.getAllSuppliers();
        suppliers.value = res.data;
    } catch (error) {
        console.error('Error fetching suppliers:', error);
    } finally {
        isLoading.value = false;
    }
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
        if (editingSupplier.value) {
            await supplierService.updateSupplier(editingSupplier.value.id, formData.value);
        } else {
            await supplierService.createSupplier(formData.value);
        }
        await fetchSuppliers();
        closeForm();
    } catch (error) {
        console.error('Error saving supplier:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this supplier?')) return;
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
        <div class="header">
            <h2>Suppliers</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search supplier..." class="search-input" />
                <button class="btn btn-primary" @click="openForm()">Add Supplier</button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Loading...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Phone</th>
                        <th>Email</th>
                        <th>Address</th>
                        <th>Active</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="supplier in filteredSuppliers" :key="supplier.id">
                        <td>{{ supplier.id }}</td>
                        <td>{{ supplier.name }}</td>
                        <td>{{ supplier.phoneNumber }}</td>
                        <td>{{ supplier.email }}</td>
                        <td>{{ supplier.address }}</td>
                        <td>
                            <span :class="{ 'badge-success': supplier.active, 'badge-warning': !supplier.active }">
                                {{ supplier.active ? 'Yes' : 'No' }}
                            </span>
                        </td>
                        <td>
                            <button class="btn-text" @click="openForm(supplier)">Edit</button>
                            <button class="btn-text text-danger" @click="handleDelete(supplier.id)">Delete</button>
                        </td>
                    </tr>
                    <tr v-if="filteredSuppliers.length === 0">
                        <td colspan="7" class="text-center">No suppliers found.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingSupplier ? 'Edit Supplier' : 'Add Supplier' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label>Name</label>
                        <input v-model="formData.name" type="text" required />
                    </div>
                    <div class="form-group">
                        <label>Phone Number</label>
                        <input v-model="formData.phoneNumber" type="text" />
                    </div>
                    <div class="form-group">
                        <label>Email</label>
                        <input v-model="formData.email" type="email" />
                    </div>
                    <div class="form-group">
                        <label>Address</label>
                        <input v-model="formData.address" type="text" />
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

.loading {
    text-align: center;
    padding: 2rem;
    color: var(--color-text-muted);
}

.card {
    background-color: var(--color-surface);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    padding: 1.5rem;
    border: 1px solid var(--border-color);
}

.table-container {
    overflow-x: auto;
}

table {
    width: 100%;
    border-collapse: collapse;
}

th,
td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid var(--border-color);
}

th {
    font-weight: 600;
    color: var(--color-text-muted);
    font-size: 0.875rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.btn {
    padding: 0.625rem 1.25rem;
    border-radius: var(--radius-md);
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
    font-size: 0.9rem;
}

.btn-primary {
    background-color: var(--color-primary);
    color: white;
}

.btn-primary:hover {
    background-color: var(--color-primary-dark);
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

.text-center {
    text-align: center;
}

.badge-success {
    background-color: #dcfce7;
    color: #166534;
    padding: 0.25rem 0.5rem;
    border-radius: 9999px;
    font-size: 0.75rem;
    font-weight: 500;
}

.badge-warning {
    background-color: #fef9c3;
    color: #854d0e;
    padding: 0.25rem 0.5rem;
    border-radius: 9999px;
    font-size: 0.75rem;
    font-weight: 500;
}

/* Modal */
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
