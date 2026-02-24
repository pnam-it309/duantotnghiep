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
    if (!confirm('Bạn có chắc chắn muốn xóa phiên bản này?')) return;
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
        <div class="page-header">
            <div class="page-title">
                <h2>Các phiên bản (Sản phẩm #{{ productId }})</h2>
            </div>
            <button class="btn btn-primary btn-add" @click="openForm()">
                <i class="pi pi-plus"></i>
                <span>Thêm phiên bản</span>
            </button>
        </div>

        <div class="action-bar card">
            <div class="filter-group">
                <div class="search-wrapper">
                    <i class="pi pi-search search-icon"></i>
                    <input type="text" v-model="searchQuery" placeholder="Tìm SKU..." class="search-input" />
                </div>

                <div class="select-group">
                  <div class="select-wrapper">
                    <select v-model="selectedSizeId" class="filter-select">
                        <option :value="0">Tất cả kích thước</option>
                        <option v-for="s in sizes" :key="s.id" :value="s.id">{{ s.sizeValue }}</option>
                    </select>
                    <i class="pi pi-chevron-down select-icon"></i>
                  </div>
                </div>

                <div class="select-group">
                  <div class="select-wrapper">
                    <select v-model="selectedColorId" class="filter-select">
                        <option :value="0">Tất cả màu sắc</option>
                        <option v-for="c in colors" :key="c.id" :value="c.id">{{ c.colorName }}</option>
                    </select>
                    <i class="pi pi-chevron-down select-icon"></i>
                  </div>
                </div>
            </div>

            <div class="button-group">
                <div class="import-export">
                    <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none" accept=".xlsx, .xls" />
                    <button class="btn btn-outline" @click="triggerFileInput" title="Nhập Excel">
                        <i class="pi pi-file-import"></i>
                        <span>Nhập Excel</span>
                    </button>
                </div>
                <button class="btn btn-outline" @click="importService.downloadTemplate('product_variants')" title="Tải mẫu">
                    <i class="pi pi-download"></i>
                    <span>Tải mẫu</span>
                </button>
            </div>
        </div>

        <div v-if="!productId" class="alert-error">ID sản phẩm không hợp lệ</div>

        <div v-else-if="isLoading" class="loading">Đang tải...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>SKU</th>
                        <th>Kích thước</th>
                        <th>Màu sắc</th>
                        <th>Giá</th>
                        <th>Kho</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="variant in filteredVariants" :key="variant.id">
                        <td>{{ variant.id }}</td>
                        <td><span class="sku-badge">{{ variant.sku }}</span></td>
                        <td>{{ variant.sizeValue }}</td>
                        <td>
                          <div class="color-cell">
                            {{ variant.colorName }}
                          </div>
                        </td>
                        <td><span class="price-val">{{ variant.price.toLocaleString() }}đ</span></td>
                        <td>
                          <span class="badge" :class="variant.stockQuantity > 0 ? 'badge-success' : 'badge-danger'">
                            {{ variant.stockQuantity }}
                          </span>
                        </td>
                        <td>
                            <button class="btn-text" @click="generateQR(variant)">QR</button>
                            <button class="btn-text" @click="openForm(variant)">Sửa</button>
                            <button class="btn-text text-danger" @click="handleDelete(variant.id)">Xóa</button>
                        </td>
                    </tr>
                    <tr v-if="filteredVariants.length === 0">
                        <td colspan="7" class="text-center">Không tìm thấy phiên bản nào.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingVariant ? 'Chỉnh sửa' : 'Thêm mới' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="sku">SKU</label>
                        <input id="sku" v-model="formData.sku" type="text" placeholder="Nhập SKU biến thể" required />
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="size">Kích thước</label>
                            <select id="size" v-model="formData.sizeId" required>
                                <option v-for="s in sizes" :key="s.id" :value="s.id">{{ s.sizeValue }}</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="color">Màu sắc</label>
                            <select id="color" v-model="formData.colorId" required>
                                <option v-for="c in colors" :key="c.id" :value="c.id">{{ c.colorName }}</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="price">Giá bán</label>
                            <input id="price" v-model="formData.price" type="number" step="0.01" required />
                        </div>
                        <div class="form-group">
                            <label for="stock">Số lượng kho</label>
                            <input id="stock" v-model="formData.stockQuantity" type="number" required />
                        </div>
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn" @click="closeForm">Hủy</button>
                        <button type="submit" class="btn btn-primary">Lưu</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- QR Modal -->
        <div v-if="showQRModal" class="modal-overlay" @click.self="showQRModal = false">
            <div class="modal card qr-modal">
                <div class="modal-header">
                  <h3>Mã QR: {{ selectedVariantQR?.sku }}</h3>
                  <p>{{ selectedVariantQR?.colorName }} - {{ selectedVariantQR?.sizeValue }}</p>
                </div>
                <div class="qr-container">
                    <img :src="qrCodeUrl" alt="Mã QR sản phẩm" />
                </div>
                <div class="form-actions qr-actions">
                    <button class="btn" @click="showQRModal = false">Đóng</button>
                    <a :href="qrCodeUrl" download="qrcode.png" class="btn btn-primary">
                      <i class="pi pi-download"></i>
                      <span>Tải về</span>
                    </a>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.loading {
    text-align: center;
    padding: 3rem;
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
    font-size: 0.875rem;
}

.btn-text:hover {
    text-decoration: underline;
}

.text-danger {
    color: var(--color-danger);
}

.sku-badge {
    background: #f1f5f9;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-family: monospace;
    font-weight: 600;
    font-size: 0.75rem;
    color: #475569;
}

.color-cell {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.color-dot {
    width: 14px;
    height: 14px;
    border-radius: 50%;
    border: 1px solid var(--border-color);
}

.price-val {
    font-weight: 600;
    color: var(--color-text-main);
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(4px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal {
    width: 100%;
    max-width: 500px;
    animation: modal-in 0.3s ease-out;
}

@keyframes modal-in {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

.qr-modal {
    max-width: 360px;
}

.qr-container {
    padding: 1.5rem;
    background: #f8fafc;
    border-radius: 12px;
    margin: 1.5rem 0;
    display: flex;
    justify-content: center;
}

.qr-container img {
    width: 200px;
    height: 200px;
    mix-blend-mode: multiply;
}

.qr-actions {
  justify-content: center;
  width: 100%;
}

.form {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
    margin-top: 1.5rem;
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
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.025em;
}

input, select {
  padding: 0.625rem 0.875rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 0.9375rem;
  transition: all 0.2s;
}

input:focus, select:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(234, 179, 8, 0.1);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1rem;
}

.alert-error {
    background-color: #fef2f2;
    color: #991b1b;
    padding: 1rem;
    border-radius: 8px;
    text-align: center;
    margin: 2rem;
    border: 1px solid #fecaca;
}

/* Table Enhancements */
.table-container {
    border: 1px solid var(--border-color);
    box-shadow: var(--shadow-sm);
}

table th {
    background-color: #f9fafb;
    padding: 1rem;
}

table td {
    padding: 1rem;
    font-size: 0.875rem;
    color: var(--color-text-main);
}
</style>
