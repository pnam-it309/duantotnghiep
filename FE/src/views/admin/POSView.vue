<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import productService, { type ProductDTO } from '../../services/productService';
import productVariantService, { type ProductVariantDTO } from '../../services/productVariantService';
import userService, { type UserDTO } from '../../services/userService';
import couponService, { type CouponDTO } from '../../services/couponService';
import orderService, { type OrderDTO } from '../../services/orderService';
import { useRouter } from 'vue-router';
import { Html5QrcodeScanner } from 'html5-qrcode';

const router = useRouter();

// --- State ---
const products = ref<ProductDTO[]>([]);
const users = ref<UserDTO[]>([]);
const pointsToUse = ref(0);
const isLoadingProducts = ref(false);

const searchQuery = ref('');

const filteredProducts = computed(() => {
    if (!searchQuery.value) return products.value;
    const query = searchQuery.value.toLowerCase();
    return products.value.filter(p =>
        p.name.toLowerCase().includes(query) ||
        p.categoryName?.toLowerCase().includes(query) ||
        p.brandName?.toLowerCase().includes(query)
    );
});

// Cart
interface CartItem {
    variantId: number;
    productName: string;
    sku: string;
    size: string;
    color: string;
    price: number;
    quantity: number;
}
const cart = ref<CartItem[]>([]);
const selectedUser = ref<number | null>(null);
const couponCode = ref('');
const appliedCoupon = ref<CouponDTO | null>(null);

// Modal for Variant Selection
const showVariantModal = ref(false);
const selectedProduct = ref<ProductDTO | null>(null);
const currentVariants = ref<ProductVariantDTO[]>([]);
const isLoadingVariants = ref(false);

// QR Scanner
const showScanner = ref(false);
let scanner: Html5QrcodeScanner | null = null;

// --- Computed ---
const subtotal = computed(() => {
    return cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0);
});

const discountAmount = computed(() => {
    if (!appliedCoupon.value) return 0;
    // Simple logic: if specific amount, use it. If dependent on min order, check it.
    if (appliedCoupon.value.minOrderValue && subtotal.value < appliedCoupon.value.minOrderValue) {
        return 0;
    }
    return appliedCoupon.value.discountAmount;
    // If percentage, logic would differ. Assuming fixed amount based on DTO viewing previously.
});

const pointsDiscountAmount = computed(() => {
    return pointsToUse.value * 1000; // 1 Point = 1000 VND
});

const finalTotal = computed(() => {
    const total = subtotal.value - discountAmount.value - pointsDiscountAmount.value;
    return total > 0 ? total : 0;
});

const selectedCustomerInfo = computed(() => {
    if (!selectedUser.value) return null;
    return users.value.find(u => u.id === selectedUser.value);
});

// --- Methods ---

const loadInitialData = async () => {
    isLoadingProducts.value = true;
    try {
        const [prodRes, userRes] = await Promise.all([
            productService.getAllProducts(),
            userService.getAllUsers()
        ]);
        // Filter only active products - Handle paginated or array
        const productList = Array.isArray(prodRes.data) ? prodRes.data : (prodRes.data as any).content;
        products.value = productList.filter((p: any) => p.active);
        
        users.value = Array.isArray(userRes.data) ? userRes.data : (userRes.data as any).content;
    } catch (e) {
        console.error("Error loading POS data", e);
    } finally {
        isLoadingProducts.value = false;
    }
};

const openProductModal = async (product: ProductDTO) => {
    selectedProduct.value = product;
    showVariantModal.value = true;
    isLoadingVariants.value = true;
    currentVariants.value = [];
    try {
        const res = await productVariantService.getVariantsByProduct(product.id);
        currentVariants.value = res.data;
    } catch (e) {
        console.error("Error loading variants", e);
    } finally {
        isLoadingVariants.value = false;
    }
};

const addToCart = (variant: ProductVariantDTO, product?: ProductDTO) => {
    const existing = cart.value.find(i => i.variantId === variant.id);
    if (existing) {
        existing.quantity++;
    } else {
        // If product is not passed, find it
        let prodName = product?.name || selectedProduct.value?.name;
        if (!prodName && variant.productId) {
            const p = products.value.find(pr => pr.id === variant.productId);
            if (p) prodName = p.name;
        }

        cart.value.push({
            variantId: variant.id,
            productName: prodName || 'Unknown Product',
            sku: variant.sku,
            size: variant.sizeValue || '',
            color: variant.colorName || '',
            price: variant.price,
            quantity: 1
        });
    }
    showVariantModal.value = false;
};

const adjustQuantity = (item: CartItem, delta: number) => {
    item.quantity += delta;
    if (item.quantity <= 0) {
        cart.value = cart.value.filter(i => i !== item);
    }
};

const removeCoupon = () => {
    appliedCoupon.value = null;
    couponCode.value = '';
}

const applyCoupon = async () => {
    if (!couponCode.value) return;
    try {
        const res = await couponService.getCouponByCode(couponCode.value);
        if (res.data) {
            // Validate expiry
            if (res.data.expiryDate && new Date(res.data.expiryDate) < new Date()) {
                alert("Mã giảm giá đã hết hạn");
                return;
            }
            appliedCoupon.value = res.data;
        } else {
            alert("Không tìm thấy mã giảm giá");
        }
    } catch (e) {
        console.error("Invalid coupon", e);
        alert("Mã giảm giá không hợp lệ");
    }
};

const checkout = async () => {
    if (cart.value.length === 0) {
        alert("Giỏ hàng đang trống");
        return;
    }
    if (!selectedUser.value) {
        alert("Vui lòng chọn khách hàng");
        return;
    }

    const orderPayload = {
        user: { id: selectedUser.value },
        coupon: appliedCoupon.value ? { id: appliedCoupon.value.id } : null,
        subtotal: subtotal.value,
        discountTotal: discountAmount.value,
        finalTotal: finalTotal.value,
        pointsUsed: pointsToUse.value,
        pointsDiscount: pointsDiscountAmount.value,
        status: 'COMPLETED'
    };

    const itemsPayload = cart.value.map(item => ({
        quantity: item.quantity,
        price: item.price,
        productVariant: { id: item.variantId }
    }));

    const payload = {
        order: orderPayload,
        items: itemsPayload
    };

    try {
        await orderService.createOrder(payload);
        alert("Đơn hàng đã được tạo thành công!");
        // Reset
        cart.value = [];
        appliedCoupon.value = null;
        couponCode.value = '';
        pointsToUse.value = 0;
        selectedUser.value = null;
    } catch (e) {
        console.error("Checkout failed", e);
        alert("Thanh toán thất bại. Vui lòng kiểm tra lại.");
    }
};

// --- QR Scanner Logic ---
const openScanner = () => {
    showScanner.value = true;
    setTimeout(() => {
        if (!document.getElementById('qr-reader')) return;

        scanner = new Html5QrcodeScanner(
            "qr-reader",
            { fps: 10, qrbox: { width: 250, height: 250 } },
            /* verbose= */ false
        );
        scanner.render(onScanSuccess, (error) => { });
    }, 100);
};

const onScanSuccess = async (decodedText: string) => {
    try {
        const data = JSON.parse(decodedText);
        if (data.id) {
            scanner?.pause();

            const res = await productVariantService.getVariantById(data.id);
            if (res.data) {
                let prod = products.value.find(p => p.id === res.data.productId);

                addToCart(res.data, prod);
                alert(`Đã thêm ${prod?.name || 'sản phẩm'} vào giỏ hàng!`);
                closeScanner();
            }
        }
    } catch (e) {
        // console.error("Invalid QR", e);
    }
};

const closeScanner = () => {
    if (scanner) {
        scanner.clear().catch(error => console.error("Failed to clear scanner", error));
        scanner = null;
    }
    showScanner.value = false;
};

onMounted(loadInitialData);

onBeforeUnmount(() => {
    if (scanner) {
        scanner.clear();
    }
});
</script>

<template>
    <div class="pos-layout">
        <!-- Left: Product Grid -->
        <div class="products-section">
            <div class="page-header pos-header">
                <div class="page-title">
                    <h2>Danh mục sản phẩm</h2>
                </div>
                <div class="header-actions">
                    <div class="search-wrapper">
                        <i class="pi pi-search search-icon"></i>
                        <input type="text" v-model="searchQuery" placeholder="Tìm tên, danh mục, thương hiệu..." class="search-input" />
                    </div>
                    <button class="btn btn-outline" @click="openScanner">
                        <i class="pi pi-qrcode"></i>
                        <span>Quét mã</span>
                    </button>
                </div>
            </div>

            <div v-if="isLoadingProducts" class="loading">
                <div class="loader"></div>
                <span>Đang tải sản phẩm...</span>
            </div>

            <div v-else class="products-grid custom-scrollbar">
                <div v-for="product in filteredProducts" :key="product.id" class="product-card card"
                    @click="openProductModal(product)">
                    <div class="product-image">
                        <i class="pi pi-package"></i>
                    </div>
                    <div class="product-info">
                        <h3>{{ product.name }}</h3>
                        <div class="product-meta">
                            <span class="category-tag">{{ product.categoryName }}</span>
                            <span class="brand-tag">{{ product.brandName }}</span>
                        </div>
                    </div>
                    <div class="product-overlay">
                        <i class="pi pi-plus"></i>
                        <span>Chọn mẫu</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right: Cart & Checkout -->
        <div class="cart-section card">
            <div class="cart-header">
                <h3>Đơn hàng hiện tại</h3>
                <span class="item-count">{{ cart.length }} món</span>
            </div>

            <div class="customer-selection">
                <div class="form-group">
                    <label>Khách hàng thành viên</label>
                    <div class="select-wrapper">
                      <select v-model="selectedUser" class="filter-select">
                          <option :value="null">Khách lẻ / Chọn thành viên...</option>
                          <option v-for="u in users" :key="u.id" :value="u.id">{{ u.username }} ({{ u.email }})</option>
                      </select>
                      <i class="pi pi-chevron-down select-icon"></i>
                    </div>
                </div>
                
                <div v-if="selectedCustomerInfo" class="customer-profile">
                    <div class="profile-avatar">
                        {{ selectedCustomerInfo.username?.charAt(0).toUpperCase() }}
                    </div>
                    <div class="profile-info">
                        <div class="name-row">
                            <strong>{{ selectedCustomerInfo.username }}</strong>
                            <span class="badge" :class="selectedCustomerInfo.membershipTier?.toLowerCase() || 'silver'">
                                {{ selectedCustomerInfo.membershipTier || 'SILVER' }}
                            </span>
                        </div>
                        <span class="points"><i class="pi pi-star-fill"></i> {{ selectedCustomerInfo.rewardPoints || 0 }} điểm tích lũy</span>
                    </div>
                </div>
            </div>

            <div class="cart-items custom-scrollbar">
                <div v-if="cart.length === 0" class="empty-cart">
                    <i class="pi pi-shopping-cart"></i>
                    <p>Giỏ hàng đang trống</p>
                </div>
                <div v-for="item in cart" :key="item.variantId" class="cart-item">
                    <div class="item-main">
                        <div class="item-info">
                            <strong>{{ item.productName }}</strong>
                            <div class="item-spec">
                                <span class="spec-label">{{ item.color }}</span>
                                <span class="spec-divider">|</span>
                                <span class="spec-label">{{ item.size }}</span>
                            </div>
                        </div>
                        <div class="item-pricing">
                            <span class="unit-price">{{ item.price.toLocaleString() }}đ</span>
                            <div class="quantity-controls">
                                <button class="btn-qty" @click="adjustQuantity(item, -1)"><i class="pi pi-minus"></i></button>
                                <span class="qty-value">{{ item.quantity }}</span>
                                <button class="btn-qty" @click="adjustQuantity(item, 1)"><i class="pi pi-plus"></i></button>
                            </div>
                        </div>
                    </div>
                    <div class="item-total-price">
                        {{ (item.price * item.quantity).toLocaleString() }}đ
                    </div>
                </div>
            </div>

            <div class="cart-summary">
                <div class="coupon-section">
                    <div class="coupon-input">
                      <input v-model="couponCode" placeholder="Nhập mã khuyến mãi" :disabled="!!appliedCoupon" />
                      <button v-if="!appliedCoupon" @click="applyCoupon" class="btn btn-outline btn-sm">Áp dụng</button>
                      <button v-else @click="removeCoupon" class="btn btn-text text-danger btn-sm">Gỡ</button>
                    </div>
                </div>

                <div class="points-redemption" v-if="selectedCustomerInfo && selectedCustomerInfo.rewardPoints > 0">
                    <div class="redemption-control">
                        <div class="redemption-info">
                            <label>Dùng điểm tích lũy</label>
                            <small>Sử dụng tối đa {{ selectedCustomerInfo.rewardPoints }}đ</small>
                        </div>
                        <input type="number" v-model.number="pointsToUse" min="0"
                            :max="selectedCustomerInfo.rewardPoints" class="points-input" />
                    </div>
                </div>

                <div class="bill-details">
                    <div class="bill-row">
                        <span>Tạm tính</span>
                        <span>{{ subtotal.toLocaleString() }}đ</span>
                    </div>
                    <div class="bill-row promo" v-if="discountAmount > 0">
                        <span>Khuyến mãi</span>
                        <span>-{{ discountAmount.toLocaleString() }}đ</span>
                    </div>
                    <div class="bill-row points" v-if="pointsDiscountAmount > 0">
                        <span>Dùng điểm</span>
                        <span>-{{ pointsDiscountAmount.toLocaleString() }}đ</span>
                    </div>
                    <div class="bill-total">
                        <span>TỔNG CỘNG</span>
                        <span>{{ finalTotal.toLocaleString() }}đ</span>
                    </div>
                </div>

                <button class="btn btn-primary btn-block checkout-btn" @click="checkout" :disabled="cart.length === 0">
                    <i class="pi pi-check-circle"></i>
                    <span>THANH TOÁN</span>
                </button>
            </div>
        </div>

        <!-- Variant Selection Modal -->
        <div v-if="showVariantModal" class="modal-overlay">
            <div class="modal card variant-modal">
                <div class="modal-header">
                    <h3>{{ selectedProduct?.name }}</h3>
                    <p>Vui lòng chọn phiên bản để thêm vào giỏ hàng</p>
                </div>

                <div v-if="isLoadingVariants" class="modal-loading">
                   <div class="loader sm"></div>
                </div>
                
                <div v-else-if="currentVariants.length === 0" class="modal-empty">
                    Không có phiên bản nào cho sản phẩm này.
                </div>

                <div v-else class="variants-selection-list custom-scrollbar">
                    <div v-for="variant in currentVariants" :key="variant.id" class="variant-card"
                        :class="{ 'out-of-stock': variant.stockQuantity <= 0 }"
                        @click="variant.stockQuantity > 0 && addToCart(variant)">
                        <div class="variant-primary">
                            <span class="variant-name">{{ variant.colorName }} / {{ variant.sizeValue }}</span>
                            <span class="variant-sku">{{ variant.sku }}</span>
                        </div>
                        <div class="variant-secondary">
                            <span class="variant-price">{{ variant.price.toLocaleString() }}đ</span>
                            <span class="variant-stock" :class="{ 'warning': variant.stockQuantity < 10 }">
                                Kho: {{ variant.stockQuantity }}
                            </span>
                        </div>
                    </div>
                </div>

                <div class="form-actions">
                    <button class="btn" @click="showVariantModal = false">Đóng</button>
                </div>
            </div>
        </div>

        <!-- Scanner Modal -->
        <div v-if="showScanner" class="modal-overlay">
            <div class="modal card scanner-modal">
                <div class="modal-header">
                    <h3>Quét mã QR sản phẩm</h3>
                    <p>Đặt mã QR vào khung hình để tự động thêm sản phẩm</p>
                </div>
                <div id="qr-reader-container">
                    <div id="qr-reader"></div>
                </div>
                <div class="form-actions">
                    <button class="btn btn-primary" @click="closeScanner">Hoàn tất</button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.pos-layout {
    display: grid;
    grid-template-columns: 1fr 400px;
    gap: 1.5rem;
    height: calc(100vh - 40px);
    overflow: hidden;
    padding: 0.5rem;
}

.products-section {
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.pos-header {
    margin-bottom: 1.5rem;
}

.header-actions {
    display: flex;
    gap: 1rem;
    align-items: center;
}

.products-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 1.25rem;
    overflow-y: auto;
    padding-right: 0.5rem;
    padding-bottom: 2rem;
}

.product-card {
    cursor: pointer;
    position: relative;
    padding: 0;
    overflow: hidden;
    height: 220px;
    display: flex;
    flex-direction: column;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover {
    transform: translateY(-4px);
    border-color: var(--color-primary);
    box-shadow: 0 12px 20px -10px rgba(0, 0, 0, 0.1);
}

.product-image {
    height: 120px;
    background: #f8fafc;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 2.5rem;
    color: #cbd5e1;
    border-bottom: 1px solid var(--border-color);
}

.product-info {
    padding: 1rem;
    flex: 1;
}

.product-info h3 {
    font-size: 0.9375rem;
    font-weight: 600;
    margin-bottom: 0.5rem;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.product-meta {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.category-tag, .brand-tag {
    font-size: 0.75rem;
    padding: 0.125rem 0.375rem;
    background: #f1f5f9;
    color: #64748b;
    border-radius: 4px;
}

.product-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(220, 38, 38, 0.9);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: white;
    opacity: 0;
    transition: opacity 0.2s ease;
    gap: 0.5rem;
}

.product-card:hover .product-overlay {
    opacity: 1;
}

.product-overlay i {
    font-size: 1.5rem;
}

.product-overlay span {
    font-weight: 600;
    font-size: 0.875rem;
}

/* Cart Section */
.cart-section {
    display: flex;
    flex-direction: column;
    height: 100%;
    padding: 0;
    overflow: hidden;
}

.cart-header {
    padding: 1.25rem;
    border-bottom: 1px solid var(--border-color);
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.cart-header h3 {
    font-size: 1.125rem;
    font-weight: 700;
}

.item-count {
    font-size: 0.8125rem;
    padding: 0.25rem 0.625rem;
    background: #f1f5f9;
    border-radius: 12px;
    color: #64748b;
    font-weight: 600;
}

.customer-selection {
    padding: 1.25rem;
    background: #fcfcfc;
    border-bottom: 1px solid var(--border-color);
}

.customer-profile {
    margin-top: 1rem;
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 0.75rem;
    background: white;
    border: 1px solid var(--border-color);
    border-radius: 12px;
}

.profile-avatar {
    width: 40px;
    height: 40px;
    background: var(--color-primary);
    color: white;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 1.125rem;
}

.profile-info {
    flex: 1;
}

.name-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.125rem;
}

.name-row strong {
    font-size: 0.9375rem;
}

.points {
    font-size: 0.75rem;
    color: #dc2626;
    font-weight: 600;
}

.cart-items {
    flex: 1;
    overflow-y: auto;
    padding: 0.5rem 1.25rem;
}

.empty-cart {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 4rem 0;
    color: #cbd5e1;
}

.empty-cart i {
    font-size: 3rem;
    margin-bottom: 1rem;
}

.cart-item {
    padding: 1rem 0;
    border-bottom: 1px dashed var(--border-color);
}

.item-main {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 0.5rem;
}

.item-info {
    flex: 1;
}

.item-info strong {
    font-size: 0.875rem;
    display: block;
    margin-bottom: 0.25rem;
}

.item-spec {
    font-size: 0.75rem;
    color: var(--color-text-muted);
}

.spec-divider {
    margin: 0 0.375rem;
    opacity: 0.5;
}

.item-pricing {
    text-align: right;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 0.5rem;
}

.unit-price {
    font-size: 0.8125rem;
    color: var(--color-text-muted);
}

.quantity-controls {
    display: flex;
    align-items: center;
    background: #f1f5f9;
    border-radius: 6px;
    padding: 2px;
}

.btn-qty {
    width: 24px;
    height: 24px;
    border: none;
    background: white;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.75rem;
    cursor: pointer;
    box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.qty-value {
    width: 32px;
    text-align: center;
    font-size: 0.875rem;
    font-weight: 600;
}

.item-total-price {
    text-align: right;
    font-weight: 700;
    font-size: 0.9375rem;
}

.cart-summary {
    padding: 1.25rem;
    background: #f8fafc;
    border-top: 1px solid var(--border-color);
}

.coupon-section {
    margin-bottom: 1rem;
}

.coupon-input {
    display: flex;
    gap: 0.5rem;
}

.coupon-input input {
    flex: 1;
    padding: 0.5rem 0.75rem;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    font-size: 0.875rem;
}

.points-redemption {
    padding: 0.75rem;
    background: white;
    border: 1px solid var(--border-color);
    border-radius: 10px;
    margin-bottom: 1rem;
}

.redemption-control {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.redemption-info label {
    font-size: 0.8125rem;
    font-weight: 600;
    display: block;
}

.redemption-info small {
    font-size: 0.6875rem;
    color: var(--color-text-muted);
}

.points-input {
    width: 70px;
    padding: 0.375rem;
    border: 1px solid var(--border-color);
    border-radius: 6px;
    text-align: right;
    font-weight: 700;
}

.bill-details {
    margin-bottom: 1.25rem;
}

.bill-row {
    display: flex;
    justify-content: space-between;
    font-size: 0.875rem;
    margin-bottom: 0.5rem;
    color: var(--color-text-muted);
}

.bill-row.promo, .bill-row.points {
    color: #ef4444;
}

.bill-total {
    display: flex;
    justify-content: space-between;
    font-size: 1.125rem;
    font-weight: 800;
    color: var(--color-text-main);
    padding-top: 0.75rem;
    border-top: 2px solid #e2e8f0;
    margin-top: 0.75rem;
}

.checkout-btn {
    height: 52px;
    font-size: 1rem;
    font-weight: 700;
    letter-spacing: 0.05em;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.75rem;
}

/* Modals */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(15, 23, 42, 0.4);
    backdrop-filter: blur(8px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
}

.variant-modal {
    width: 90%;
    max-width: 600px;
    max-height: 80vh;
}

.modal-header {
    margin-bottom: 1.5rem;
}

.modal-header h3 {
    font-size: 1.25rem;
    margin-bottom: 0.25rem;
}

.modal-header p {
    color: var(--color-text-muted);
    font-size: 0.875rem;
}

.variants-selection-list {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0.75rem;
    overflow-y: auto;
    padding-right: 0.5rem;
    margin-bottom: 1.5rem;
}

.variant-card {
    padding: 1rem;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
}

.variant-card:hover:not(.out-of-stock) {
    background: white;
    border-color: var(--color-primary);
    box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}

.variant-card.out-of-stock {
    opacity: 0.5;
    cursor: not-allowed;
    background: #f1f5f9;
}

.variant-name {
    display: block;
    font-weight: 700;
    font-size: 0.9375rem;
    margin-bottom: 0.125rem;
}

.variant-sku {
    display: block;
    font-size: 0.75rem;
    color: #64748b;
    font-family: monospace;
}

.variant-secondary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 1rem;
}

.variant-price {
    font-weight: 800;
    color: var(--color-text-main);
}

.variant-stock {
    font-size: 0.75rem;
    font-weight: 600;
    color: #10b981;
}

.variant-stock.warning {
    color: #f59e0b;
}

/* Scanner */
.scanner-modal {
    max-width: 450px;
}

#qr-reader-container {
    background: #000;
    border-radius: 16px;
    overflow: hidden;
    margin: 1.5rem 0;
}

#qr-reader {
  border: none !important;
}

.loader {
    width: 32px;
    height: 32px;
    border: 3px solid #f3f3f3;
    border-top: 3px solid var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* Scrollbar styling */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

@media (max-width: 1024px) {
    .pos-layout { grid-template-columns: 1fr; overflow-y: auto; height: auto; }
    .cart-section { height: auto; }
    .products-grid { height: auto; overflow-y: visible; }
}
</style>
