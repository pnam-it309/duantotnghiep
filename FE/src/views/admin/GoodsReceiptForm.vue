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
        products.value = prodRes.data.content.filter((p: ProductDTO) => p.active);
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
        alert("Vui lòng chọn phiên bản, số lượng và giá nhập hợp lệ");
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
        alert("Vui lòng chọn nhà cung cấp");
        return;
    }
    if (!form.value.details || form.value.details.length === 0) {
        alert("Vui lòng thêm ít nhất một sản phẩm");
        return;
    }

    isLoading.value = true;
    try {
        await goodsReceiptService.createReceipt(form.value as GoodsReceiptDTO);
        alert("Nhập hàng thành công!");
        router.push('/admin/goods-receipts');
    } catch (e) {
        console.error("Error creating receipt", e);
        alert("Lỗi khi tạo phiếu nhập");
    } finally {
        isLoading.value = false;
    }
};

onMounted(loadInitialData);
</script>

<template>
    <div class="receipt-form-view">
        <div class="page-header">
            <div class="page-title">
                <h2>Phiếu nhập hàng mới</h2>
            </div>
            <button class="btn btn-outline" @click="router.back()">
                <i class="pi pi-arrow-left"></i>
                <span>Quay lại</span>
            </button>
        </div>

        <div class="form-grid">
            <div class="main-column">
                <div class="card form-section shadow-glow">
                    <div class="section-header">
                        <i class="pi pi-plus-circle"></i>
                        <h3>Thêm sản phẩm vào phiếu</h3>
                    </div>
                    <div class="entry-row">
                        <div class="form-group product-select">
                            <label>Sản phẩm</label>
                            <div class="select-wrapper">
                              <select v-model="currentItem.productId" @change="handleProductChange" class="form-control">
                                  <option :value="0">Chọn sản phẩm...</option>
                                  <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
                              </select>
                              <i class="pi pi-chevron-down select-icon"></i>
                            </div>
                        </div>
                        <div class="form-group variant-select">
                            <label>Phiên bản</label>
                            <div class="select-wrapper">
                              <select v-model="currentItem.variantId" class="form-control" :disabled="!currentItem.productId">
                                  <option :value="0">Chọn phiên bản...</option>
                                  <option v-for="v in variants" :key="v.id" :value="v.id">
                                      {{ v.sku }} - {{ v.colorName }} / {{ v.sizeValue }}
                                  </option>
                              </select>
                              <i class="pi pi-chevron-down select-icon"></i>
                            </div>
                        </div>
                        <div class="form-group qty-input">
                            <label>SL</label>
                            <input type="number" v-model="currentItem.quantity" min="1" class="form-control" />
                        </div>
                        <div class="form-group price-input">
                            <label>Giá nhập</label>
                            <input type="number" v-model="currentItem.importPrice" min="0" class="form-control" />
                        </div>
                        <div class="form-group action-btn">
                            <label>&nbsp;</label>
                            <button class="btn btn-primary" @click="addItem">Thêm</button>
                        </div>
                    </div>
                </div>

                <div class="card table-container">
                    <div class="section-header">
                        <i class="pi pi-list"></i>
                        <h3>Danh sách sản phẩm nhập</h3>
                    </div>
                    <table class="table">
                        <thead>
                            <tr>
                                <th>SKU</th>
                                <th>Thông tin sản phẩm</th>
                                <th>SL</th>
                                <th>Giá nhập</th>
                                <th>Thành tiền</th>
                                <th class="text-right">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in form.details" :key="index">
                                <td><span class="sku-badge">{{ item.sku }}</span></td>
                                <td><strong>{{ item.productVariantName }}</strong></td>
                                <td>{{ item.quantity }}</td>
                                <td>{{ item.importPrice.toLocaleString() }}đ</td>
                                <td><span class="row-total">{{ (item.quantity * item.importPrice).toLocaleString() }}đ</span></td>
                                <td class="text-right">
                                    <button class="btn-text text-danger" @click="removeItem(index)">Xóa</button>
                                </td>
                            </tr>
                            <tr v-if="!form.details?.length">
                                <td colspan="6" class="text-center empty-row">Chưa có sản phẩm nào được chọn.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="side-column">
                <div class="card summary-card sticky-card">
                    <div class="section-header">
                        <i class="pi pi-info-circle"></i>
                        <h3>Thông tin chung</h3>
                    </div>
                    <div class="form-group mb-4">
                        <label>Nhà cung cấp</label>
                        <div class="select-wrapper">
                          <select v-model="form.supplierId" class="form-control">
                              <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
                          </select>
                          <i class="pi pi-chevron-down select-icon"></i>
                        </div>
                    </div>
                    <div class="form-group mb-4">
                        <label>Ghi chú phiếu nhập</label>
                        <textarea v-model="form.notes" class="form-control" rows="3" placeholder="Nhập ghi chú..."></textarea>
                    </div>

                    <div class="summary-details">
                        <div class="summary-line">
                            <span>Tổng số mặt hàng:</span>
                            <span>{{ form.details?.length || 0 }}</span>
                        </div>
                        <div class="summary-line total">
                            <span>TỔNG CỘNG:</span>
                            <span class="total-price">{{ totalAmount.toLocaleString() }}đ</span>
                        </div>
                    </div>

                    <button class="btn btn-primary btn-block btn-lg mt-6" @click="submitForm" :disabled="isLoading">
                        <i class="pi pi-save"></i>
                        <span>{{ isLoading ? 'Đang lưu...' : 'Hoàn tất nhập hàng' }}</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.receipt-form-view {
    padding-bottom: 3rem;
    animation: fade-in 0.4s ease-out;
}

@keyframes fade-in {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.form-grid {
    display: grid;
    grid-template-columns: 1fr 340px;
    gap: 1.5rem;
    align-items: start;
}

.section-header {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 1.5rem;
}

.section-header i {
    color: var(--color-primary);
    font-size: 1.25rem;
}

.section-header h3 {
    margin: 0;
    font-size: 1.125rem;
    font-weight: 700;
}

.form-section {
    margin-bottom: 1.5rem;
}

.entry-row {
    display: grid;
    grid-template-columns: 2fr 2fr 80px 1fr auto;
    gap: 1rem;
    align-items: flex-end;
}

.qty-input input, .price-input input {
    text-align: right;
}

.sku-badge {
    background: #f1f5f9;
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    font-family: monospace;
    font-weight: 600;
    font-size: 0.8125rem;
}

.row-total {
    font-weight: 700;
    color: var(--color-text-main);
}

.text-right {
    text-align: right;
}

.empty-row {
    padding: 3rem 0 !important;
    color: var(--color-text-muted);
    font-style: italic;
}

.summary-card {
    padding: 1.5rem;
}

.sticky-card {
    position: sticky;
    top: 1rem;
}

.summary-details {
    background: #f8fafc;
    padding: 1rem;
    border-radius: 12px;
    margin-top: 2rem;
}

.summary-line {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
    font-size: 0.875rem;
}

.summary-line.total {
    border-top: 1px solid #e2e8f0;
    padding-top: 0.75rem;
    margin-top: 0.75rem;
    font-weight: 800;
    color: var(--color-text-main);
}

.total-price {
    font-size: 1.25rem;
    color: var(--color-primary);
}

label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.025em;
  margin-bottom: 0.5rem;
  display: block;
}

.btn-block {
    width: 100%;
    height: 3.5rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
}

.shadow-glow {
    box-shadow: 0 0 20px rgba(234, 179, 8, 0.05);
}

.mb-4 { margin-bottom: 1rem; }
.mt-6 { margin-top: 1.5rem; }

@media (max-width: 1024px) {
    .form-grid { grid-template-columns: 1fr; }
    .sticky-card { position: static; }
}
</style>
