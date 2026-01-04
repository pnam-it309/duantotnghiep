<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import couponService, { type CouponDTO } from '../../services/couponService';
import importService from '../../services/importService';

const coupons = ref<CouponDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingCoupon = ref<CouponDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const searchQuery = ref('');

const filteredCoupons = computed(() => {
    if (!searchQuery.value) return coupons.value;
    const lowerQuery = searchQuery.value.toLowerCase();
    return coupons.value.filter(coupon =>
        coupon.code.toLowerCase().includes(lowerQuery) ||
        coupon.id.toString().includes(lowerQuery)
    );
});

const formData = ref({
    code: '',
    discountAmount: 0,
    minOrderValue: 0,
    expiryDate: ''
});

const fetchCoupons = async () => {
    isLoading.value = true;
    try {
        const response = await couponService.getAllCoupons();
        coupons.value = response.data;
    } catch (error) {
        console.error('Error fetching coupons:', error);
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
            await importService.importCoupons(file!);
            alert('Import successful!');
            await fetchCoupons();
        } catch (error) {
            console.error('Import failed', error);
            alert('Import failed');
        } finally {
            if (fileInput.value) fileInput.value.value = '';
        }
    }
};

const handleSubmit = async () => {
    if (!formData.value.code) return;

    try {
        const payload = {
            code: formData.value.code,
            discountAmount: formData.value.discountAmount,
            minOrderValue: formData.value.minOrderValue,
            expiryDate: formData.value.expiryDate ? new Date(formData.value.expiryDate).toISOString() : new Date().toISOString()
        };

        if (editingCoupon.value) {
            await couponService.updateCoupon(editingCoupon.value.id, { ...editingCoupon.value, ...payload });
        } else {
            await couponService.createCoupon(payload);
        }
        await fetchCoupons();
        closeForm();
    } catch (error) {
        console.error('Error saving coupon:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this coupon?')) return;

    try {
        await couponService.deleteCoupon(id);
        await fetchCoupons();
    } catch (error) {
        console.error('Error deleting coupon:', error);
    }
};

const openForm = (coupon?: CouponDTO) => {
    if (coupon) {
        editingCoupon.value = coupon;
        formData.value = {
            code: coupon.code,
            discountAmount: coupon.discountAmount,
            minOrderValue: coupon.minOrderValue,
            expiryDate: coupon.expiryDate ? coupon.expiryDate.slice(0, 16) : '' // format for datetime-local
        };
    } else {
        editingCoupon.value = null;
        formData.value = {
            code: '',
            discountAmount: 0,
            minOrderValue: 0,
            expiryDate: ''
        };
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingCoupon.value = null;
    formData.value = {
        code: '',
        discountAmount: 0,
        minOrderValue: 0,
        expiryDate: ''
    };
};

onMounted(() => {
    fetchCoupons();
});
</script>

<template>
    <div class="coupon-view">
        <div class="header">
            <h2>Coupons</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search coupons..." class="search-input" />
                <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none"
                    accept=".xlsx, .xls" />
                <button class="btn btn-secondary" @click="importService.downloadTemplate('coupons')"
                    style="margin-right: 0.5rem">Download Template</button>
                <button class="btn btn-secondary" @click="triggerFileInput" style="margin-right: 0.5rem">Import
                    Excel</button>
                <button class="btn btn-primary" @click="openForm()">Add Coupon</button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Loading...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Code</th>
                        <th>Discount</th>
                        <th>Min Order</th>
                        <th>Expiry</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="coupon in filteredCoupons" :key="coupon.id">
                        <td>{{ coupon.id }}</td>
                        <td>{{ coupon.code }}</td>
                        <td>{{ coupon.discountAmount }}</td>
                        <td>{{ coupon.minOrderValue }}</td>
                        <td>{{ new Date(coupon.expiryDate).toLocaleDateString() }}</td>
                        <td>
                            <button class="btn-text" @click="openForm(coupon)">Edit</button>
                            <button class="btn-text text-danger" @click="handleDelete(coupon.id)">Delete</button>
                        </td>
                    </tr>
                    <tr v-if="filteredCoupons.length === 0">
                        <td colspan="6" class="text-center">No coupons found.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingCoupon ? 'Edit Coupon' : 'Add Coupon' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="code">Code</label>
                        <input id="code" v-model="formData.code" type="text" required />
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="discountAmount">Discount Amount</label>
                            <input id="discountAmount" v-model="formData.discountAmount" type="number" step="0.01"
                                required />
                        </div>
                        <div class="form-group">
                            <label for="minOrderValue">Min Order Value</label>
                            <input id="minOrderValue" v-model="formData.minOrderValue" type="number" required />
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="expiryDate">Expiry Date</label>
                        <input id="expiryDate" v-model="formData.expiryDate" type="datetime-local" required />
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
