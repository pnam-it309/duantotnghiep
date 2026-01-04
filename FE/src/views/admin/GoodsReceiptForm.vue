<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import supplierService, { type SupplierDTO } from '../../services/supplierService';
import productService, { type ProductDTO } from '../../services/productService';
import productVariantService, { type ProductVariantDTO } from '../../services/productVariantService';
import goodsReceiptService, { type GoodsReceiptDTO, type GoodsReceiptDetailDTO } from '../../services/goodsReceiptService';

const router = useRouter();

// State
const suppliers = ref<SupplierDTO[]>([]);
const products = ref<ProductDTO[]>([]);
const selectedProduct = ref<ProductDTO | null>(null);
const variants = ref<ProductVariantDTO[]>([]);
const form = ref<Partial<GoodsReceiptDTO>>({
    supplierId: undefined,
    userId: 1, // Hardcoded Admin for now
    notes: '',
    details: []
});

// Item Input State
const currentItem = ref({
    productId: 0,
    variantId: 0,
    quantity: 1,
    importPrice: 0
});

const isLoading = ref(false);

// Computed
const totalAmount = computed(() => {
    return form.value.details?.reduce((sum, item) => sum + (item.quantity * item.importPrice), 0) || 0;
});

const loadInitialData = async () => {
    try {
        const [supRes, prodRes] = await Promise.all([
            supplierService.getAllSuppliers(),
            productService.getAllProducts()
        ]);
        suppliers.value = supRes.data;
        products.value = prodRes.data.filter(p => p.active);
    } catch (e) {
        console.error("Error loading data", e);
    }
};

const handleProductChange = async () => {
    if (!currentItem.value.productId) {
        variants.value = [];
        return;
    }
    const prod = products.value.find(p => p.id === currentItem.value.productId);
    selectedProduct.value = prod || null;

    // Load variants
    try {
        const res = await productVariantService.getVariantsByProduct(currentItem.value.productId);
        variants.value = res.data;
        currentItem.value.variantId = 0; // reset
    } catch (e) {
        console.error("Error loading variants", e);
    }
};

const addItem = () => {
    if (!currentItem.value.variantId || currentItem.value.quantity <= 0 || currentItem.value.importPrice < 0) {
        alert("Please select valid variant, quantity and price");
        return;
    }

    const variant = variants.value.find(v => v.id === currentItem.value.variantId);
    if (!variant) return;

    // Check if exists
    const existing = form.value.details?.find(d => d.productVariantId === variant.id);
    if (existing) {
        existing.quantity += currentItem.value.quantity;
        // Average price or just update? Let's just update
        existing.importPrice = currentItem.value.importPrice;
    } else {
        const detail: GoodsReceiptDetailDTO = {
            productVariantId: variant.id,
            sku: variant.sku,
            productVariantName: `${selectedProduct.value?.name} (${variant.colorName} - ${variant.sizeValue})`,
            quantity: currentItem.value.quantity,
            importPrice: currentItem.value.importPrice
        };
        form.value.details?.push(detail);
    }

    // Reset item input but keep product/variant for faster entry if needed?
    // Let's reset quantity
    currentItem.value.quantity = 1;
};

const removeItem = (index: number) => {
    form.value.details?.splice(index, 1);
};

const submitForm = async () => {
    if (!form.value.supplierId) {
        alert("Please select a supplier");
        return;
    }
    if (!form.value.details || form.value.details.length === 0) {
        alert("Please add at least one item");
        return;
    }

    isLoading.value = true;
    try {
        await goodsReceiptService.createReceipt(form.value as GoodsReceiptDTO);
        alert("Import successful!");
        router.push('/admin/goods-receipts');
    } catch (e) {
        console.error("Error creating receipt", e);
        alert("Failed to create receipt");
    } finally {
        isLoading.value = false;
    }
};

onMounted(loadInitialData);
</script>

<template>
    <div class="receipt-form-view">
        <div class="header">
            <h2>New Goods Receipt</h2>
            <button class="btn" @click="router.back()">Back</button>
        </div>

        <div class="form-container card">
            <!-- Master Info -->
            <div class="form-section">
                <h3>General Info</h3>
                <div class="form-row">
                    <div class="form-group">
                        <label>Supplier</label>
                        <select v-model="form.supplierId" class="form-control">
                            <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Notes</label>
                        <input type="text" v-model="form.notes" class="form-control" />
                    </div>
                </div>
            </div>

            <!-- Item Entry -->
            <div class="form-section item-entry">
                <h3>Add Items</h3>
                <div class="entry-row">
                    <div class="form-group product-select">
                        <label>Product</label>
                        <select v-model="currentItem.productId" @change="handleProductChange" class="form-control">
                            <option :value="0">Select Product...</option>
                            <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
                        </select>
                    </div>
                    <div class="form-group variant-select">
                        <label>Variant</label>
                        <select v-model="currentItem.variantId" class="form-control" :disabled="!currentItem.productId">
                            <option :value="0">Select Variant...</option>
                            <option v-for="v in variants" :key="v.id" :value="v.id">
                                {{ v.sku }} - {{ v.colorName }} / {{ v.sizeValue }}
                            </option>
                        </select>
                    </div>
                    <div class="form-group qty- input">
                        <label>Qty</label>
                        <input type="number" v-model="currentItem.quantity" min="1" class="form-control" />
                    </div>
                    <div class="form-group price-input">
                        <label>Import Price</label>
                        <input type="number" v-model="currentItem.importPrice" min="0" class="form-control" />
                    </div>
                    <div class="form-group action-btn">
                        <label>&nbsp;</label>
                        <button class="btn btn-secondary" @click="addItem">Add</button>
                    </div>
                </div>
            </div>

            <!-- Items Table -->
            <div class="form-section">
                <h3>Items List</h3>
                <table class="table">
                    <thead>
                        <tr>
                            <th>SKU</th>
                            <th>Product Info</th>
                            <th>Qty</th>
                            <th>Import Price</th>
                            <th>Subtotal</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in form.details" :key="index">
                            <td>{{ item.sku }}</td>
                            <td>{{ item.productVariantName }}</td>
                            <td>{{ item.quantity }}</td>
                            <td>{{ item.importPrice.toLocaleString() }}</td>
                            <td>{{ (item.quantity * item.importPrice).toLocaleString() }}</td>
                            <td>
                                <button class="text-danger btn-text" @click="removeItem(index)">Remove</button>
                            </td>
                        </tr>
                        <tr v-if="!form.details?.length">
                            <td colspan="6" class="text-center">No items added.</td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="4" style="text-align: right; font-weight: bold;">TOTAL:</td>
                            <td style="font-weight: bold; font-size: 1.1rem;">{{ totalAmount.toLocaleString() }}</td>
                            <td></td>
                        </tr>
                    </tfoot>
                </table>
            </div>

            <div class="form-actions">
                <button class="btn btn-primary btn-lg" @click="submitForm" :disabled="isLoading">
                    {{ isLoading ? 'Saving...' : 'Create Receipt' }}
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.receipt-form-view {
    max-width: 1000px;
    margin: 0 auto;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.card {
    background-color: var(--color-surface);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    padding: 2rem;
    border: 1px solid var(--border-color);
}

.form-section {
    margin-bottom: 2rem;
    padding-bottom: 1rem;
    border-bottom: 1px dashed var(--border-color);
}

.form-section h3 {
    margin-bottom: 1rem;
    color: var(--color-heading);
    font-size: 1.1rem;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

.entry-row {
    display: flex;
    gap: 1rem;
    align-items: flex-start;
    flex-wrap: wrap;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.product-select {
    flex: 2;
    min-width: 200px;
}

.variant-select {
    flex: 2;
    min-width: 200px;
}

.qty-input {
    flex: 1;
    min-width: 80px;
}

.price-input {
    flex: 1;
    min-width: 120px;
}

.action-btn {
    min-width: 80px;
}

.form-control {
    padding: 0.625rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    background-color: var(--color-background);
}

.table {
    width: 100%;
    border-collapse: collapse;
}

.table th,
.table td {
    padding: 0.75rem;
    border-bottom: 1px solid var(--border-color);
    text-align: left;
}

.text-center {
    text-align: center;
}

.text-danger {
    color: var(--color-danger);
}

.btn-text {
    background: none;
    border: none;
    cursor: pointer;
}

.btn-text:hover {
    text-decoration: underline;
}

.btn {
    padding: 0.625rem 1.25rem;
    border-radius: var(--radius-md);
    cursor: pointer;
    border: 1px solid var(--border-color);
    background: white;
}

.btn-secondary {
    background-color: var(--color-primary);
    color: white;
    border: none;
}

.btn-primary {
    background-color: var(--color-primary);
    color: white;
    border: none;
}

.btn-lg {
    padding: 0.75rem 2rem;
    font-size: 1rem;
    width: 100%;
}

.form-actions {
    margin-top: 1rem;
}
</style>
