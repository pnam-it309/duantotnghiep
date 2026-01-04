<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import productVariantService, { type ProductVariantDTO } from '../../services/productVariantService';
import sizeService, { type SizeDTO } from '../../services/sizeService';
import colorService, { type ColorDTO } from '../../services/colorService';
import importService from '../../services/importService';
import QRCode from 'qrcode';

const route = useRoute();
const productId = Number(route.params.productId); // Expect route /product/:productId/variants

const variants = ref<ProductVariantDTO[]>([]);
const sizes = ref<SizeDTO[]>([]);
const colors = ref<ColorDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingVariant = ref<ProductVariantDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

// Filters
const searchQuery = ref('');
const selectedSizeId = ref(0);
const selectedColorId = ref(0);

const filteredVariants = computed(() => {
    return variants.value.filter(variant => {
        const query = searchQuery.value.toLowerCase();
        const matchesSearch = !query ||
            variant.sku.toLowerCase().includes(query) ||
            variant.id.toString().includes(query);

        const matchesSize = selectedSizeId.value === 0 || variant.sizeId === selectedSizeId.value;
        const matchesColor = selectedColorId.value === 0 || variant.colorId === selectedColorId.value;

        return matchesSearch && matchesSize && matchesColor;
    });
});

// QR Code State
const showQRModal = ref(false);
const qrCodeUrl = ref('');
const selectedVariantQR = ref<ProductVariantDTO | null>(null);

const formData = ref({
    sizeId: 0,
    colorId: 0,
    price: 0,
    stockQuantity: 0,
    sku: ''
});

const loadData = async () => {
    if (!productId) return;
    isLoading.value = true;
    try {
        const [variantsRes, sizesRes, colorsRes] = await Promise.all([
            productVariantService.getVariantsByProduct(productId),
            sizeService.getAllSizes(),
            colorService.getAllColors()
        ]);
        variants.value = variantsRes.data;
        sizes.value = sizesRes.data;
        colors.value = colorsRes.data;
    } catch (error) {
        console.error('Error loading variants data:', error);
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
            await importService.importProductVariants(file!);
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
    if (!formData.value.sizeId || !formData.value.colorId) return;

    try {
        const payload = {
            productId,
            sizeId: formData.value.sizeId,
            colorId: formData.value.colorId,
            price: formData.value.price,
            stockQuantity: formData.value.stockQuantity,
            sku: formData.value.sku
        };

        if (editingVariant.value) {
            if (editingVariant.value.id !== undefined) {
                await productVariantService.updateVariant(editingVariant.value.id, { ...editingVariant.value, ...payload });
            }
        } else {
            await productVariantService.createVariant(payload);
        }
        await loadData();
        closeForm();
    } catch (error) {
        console.error('Error saving variant:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this variant?')) return;
    try {
        await productVariantService.deleteVariant(id);
        await loadData();
    } catch (error) {
        console.error('Error deleting variant:', error);
    }
};

const generateQR = async (variant: ProductVariantDTO) => {
    selectedVariantQR.value = variant;
    try {
        // We encode the Variant ID as a JSON string for reliability
        const data = JSON.stringify({ id: variant.id, sku: variant.sku });
        qrCodeUrl.value = await QRCode.toDataURL(data);
        showQRModal.value = true;
    } catch (err) {
        console.error("Error generating QR", err);
    }
};

const openForm = (variant?: ProductVariantDTO) => {
    if (variant) {
        editingVariant.value = variant;
        formData.value = {
            sizeId: variant.sizeId,
            colorId: variant.colorId,
            price: variant.price,
            stockQuantity: variant.stockQuantity,
            sku: variant.sku
        };
    } else {
        editingVariant.value = null;
        formData.value = {
            sizeId: sizes.value.length > 0 ? sizes.value[0]?.id || 0 : 0,
            colorId: colors.value.length > 0 ? colors.value[0]?.id || 0 : 0,
            price: 0,
            stockQuantity: 0,
            sku: ''
        };
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingVariant.value = null;
};

onMounted(() => {
    loadData();
});
</script>

<template>
    <div class="variant-view">
        <div class="header">
            <h2>Product Variants (Product #{{ productId }})</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search SKU..." class="search-input" />

                <select v-model="selectedSizeId" class="filter-select">
                    <option :value="0">All Sizes</option>
                    <option v-for="s in sizes" :key="s.id" :value="s.id">{{ s.sizeValue }}</option>
                </select>

                <select v-model="selectedColorId" class="filter-select">
                    <option :value="0">All Colors</option>
                    <option v-for="c in colors" :key="c.id" :value="c.id">{{ c.colorName }}</option>
                </select>

                <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none"
                    accept=".xlsx, .xls" />
                <button class="btn btn-secondary" @click="importService.downloadTemplate('product_variants')"
                    style="margin-right: 0.5rem">Download Template</button>
                <button class="btn btn-secondary" @click="triggerFileInput" style="margin-right: 0.5rem">Import
                    Excel</button>
                <button class="btn btn-primary" @click="openForm()">Add Variant</button>
            </div>
        </div>

        <div v-if="!productId" class="alert-error">Invalid Product ID</div>

        <div v-else-if="isLoading" class="loading">Loading...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>SKU</th>
                        <th>Size</th>
                        <th>Color</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="variant in filteredVariants" :key="variant.id">
                        <td>{{ variant.id }}</td>
                        <td>{{ variant.sku }}</td>
                        <td>{{ variant.sizeValue }}</td>
                        <td>{{ variant.colorName }}</td>
                        <td>${{ variant.price }}</td>
                        <td>{{ variant.stockQuantity }}</td>
                        <td>
                            <button class="btn-text" @click="generateQR(variant)">QR</button>
                            <button class="btn-text" @click="openForm(variant)">Edit</button>
                            <button class="btn-text text-danger" @click="handleDelete(variant.id)">Delete</button>
                        </td>
                    </tr>
                    <tr v-if="filteredVariants.length === 0">
                        <td colspan="7" class="text-center">No variants found.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingVariant ? 'Edit Variant' : 'Add Variant' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="sku">SKU</label>
                        <input id="sku" v-model="formData.sku" type="text" required />
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="size">Size</label>
                            <select id="size" v-model="formData.sizeId" required>
                                <option v-for="s in sizes" :key="s.id" :value="s.id">{{ s.sizeValue }}</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="color">Color</label>
                            <select id="color" v-model="formData.colorId" required>
                                <option v-for="c in colors" :key="c.id" :value="c.id">{{ c.colorName }}</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="price">Price</label>
                            <input id="price" v-model="formData.price" type="number" step="0.01" required />
                        </div>
                        <div class="form-group">
                            <label for="stock">Stock</label>
                            <input id="stock" v-model="formData.stockQuantity" type="number" required />
                        </div>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn" @click="closeForm">Cancel</button>
                        <button type="submit" class="btn btn-primary">Save</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- QR Modal -->
        <div v-if="showQRModal" class="modal-overlay" @click.self="showQRModal = false">
            <div class="modal card qr-modal">
                <h3>QR Code: {{ selectedVariantQR?.sku }}</h3>
                <p>{{ selectedVariantQR?.colorName }} - {{ selectedVariantQR?.sizeValue }}</p>
                <div class="qr-container">
                    <img :src="qrCodeUrl" alt="Product QR Code" />
                </div>
                <div class="form-actions">
                    <button class="btn" @click="showQRModal = false">Close</button>
                    <a :href="qrCodeUrl" download="qrcode.png" class="btn btn-primary">Download</a>
                </div>
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

.qr-modal {
    text-align: center;
}

.qr-container img {
    width: 200px;
    height: 200px;
    margin: 1rem 0;
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

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1rem;
}

.alert-error {
    color: var(--color-danger);
    text-align: center;
    margin: 2rem;
}
</style>
