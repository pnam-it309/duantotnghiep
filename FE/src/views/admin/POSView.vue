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
        // Filter only active products
        products.value = prodRes.data.filter(p => p.active);
        users.value = userRes.data;
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
                alert("Coupon expired");
                return;
            }
            appliedCoupon.value = res.data;
        } else {
            alert("Coupon not found");
        }
    } catch (e) {
        console.error("Invalid coupon", e);
        alert("Invalid coupon code");
    }
};

const checkout = async () => {
    if (cart.value.length === 0) {
        alert("Cart is empty");
        return;
    }
    if (!selectedUser.value) {
        alert("Please select a customer");
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
        alert("Order created successfully!");
        // Reset
        cart.value = [];
        appliedCoupon.value = null;
        couponCode.value = '';
        pointsToUse.value = 0;
        selectedUser.value = null;
    } catch (e) {
        console.error("Checkout failed", e);
        alert("Checkout failed. See console.");
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
                alert(`Added ${prod?.name || 'Item'} to cart!`);
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
            <div class="section-header">
                <h2>Products</h2>
                <div class="header-actions">
                    <input type="text" v-model="searchQuery" placeholder="Search products..." class="search-input" />
                    <button class="btn btn-secondary" @click="openScanner">📷 Scan QR</button>
                </div>
            </div>

            <div v-if="isLoadingProducts" class="loading">Loading Products...</div>

            <div v-else class="products-grid">
                <div v-for="product in filteredProducts" :key="product.id" class="product-card card"
                    @click="openProductModal(product)">
                    <div class="product-icon">📦</div>
                    <div class="product-info">
                        <h3>{{ product.name }}</h3>
                        <p>{{ product.categoryName }}</p>
                        <p>{{ product.brandName }}</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- Right: Cart & Checkout -->
        <div class="cart-section card">
            <h2>Current Order</h2>

            <div class="customer-select">
                <label>Customer</label>
                <select v-model="selectedUser">
                    <option :value="null">Select Customer...</option>
                    <option v-for="u in users" :key="u.id" :value="u.id">{{ u.username }} ({{ u.email }})</option>
                </select>
                <div v-if="selectedCustomerInfo" class="customer-loyalty-info">
                    <span class="badge" :class="selectedCustomerInfo.membershipTier?.toLowerCase() || 'silver'">
                        {{ selectedCustomerInfo.membershipTier || 'SILVER' }}
                    </span>
                    <span class="points">💎 {{ selectedCustomerInfo.rewardPoints || 0 }} pts</span>
                </div>
            </div>

            <div class="cart-items">
                <div v-if="cart.length === 0" class="empty-cart">Cart is empty</div>
                <div v-for="item in cart" :key="item.variantId" class="cart-item">
                    <div class="item-details">
                        <strong>{{ item.productName }}</strong>
                        <small>{{ item.color }} / {{ item.size }}</small>
                        <span>${{ item.price }}</span>
                    </div>
                    <div class="item-actions">
                        <button class="btn-sm" @click="adjustQuantity(item, -1)">-</button>
                        <span>{{ item.quantity }}</span>
                        <button class="btn-sm" @click="adjustQuantity(item, 1)">+</button>
                    </div>
                    <div class="item-total">
                        ${{ item.price * item.quantity }}
                    </div>
                </div>
            </div>

            <div class="cart-footer">
                <div class="coupon-area">
                    <input v-model="couponCode" placeholder="Coupon Code" :disabled="!!appliedCoupon" />
                    <button v-if="!appliedCoupon" @click="applyCoupon" class="btn">Apply</button>
                    <button v-else @click="removeCoupon" class="btn text-danger">Remove</button>
                </div>

                <div class="points-area" v-if="selectedCustomerInfo && selectedCustomerInfo.rewardPoints > 0">
                    <div class="points-input-group">
                        <label>Use Points (Max: {{ selectedCustomerInfo.rewardPoints }})</label>
                        <input type="number" v-model.number="pointsToUse" min="0"
                            :max="selectedCustomerInfo.rewardPoints" class="points-input" />
                        <span class="points-value">-{{ pointsToUse * 1000 }} VND</span>
                    </div>
                </div>

                <div class="totals">
                    <div class="row"><span>Subtotal:</span> <span>${{ subtotal }}</span></div>
                    <div class="row text-success"><span>Discount:</span> <span>-${{ discountAmount }}</span></div>
                    <div class="row total"><span>Total:</span> <span>${{ finalTotal }}</span></div>
                </div>

                <button class="btn btn-primary btn-block pay-btn" @click="checkout" :disabled="cart.length === 0">
                    PAY ${{ finalTotal }}
                </button>
            </div>
        </div>

        <!-- Variant Selection Modal -->
        <div v-if="showVariantModal" class="modal-overlay" @click.self="showVariantModal = false">
            <div class="modal card">
                <h3>Select Variant: {{ selectedProduct?.name }}</h3>

                <div v-if="isLoadingVariants">Loading Options...</div>
                <div v-else-if="currentVariants.length === 0">No variants available for this product.</div>

                <div v-else class="variants-list">
                    <div v-for="variant in currentVariants" :key="variant.id" class="variant-option"
                        @click="addToCart(variant)">
                        <span class="v-info">{{ variant.colorName }} - {{ variant.sizeValue }}</span>
                        <span class="v-price">${{ variant.price }}</span>
                        <span class="v-stock">Stock: {{ variant.stockQuantity }}</span>
                        <span class="v-sku">SKU: {{ variant.sku }}</span>
                    </div>
                </div>

                <div class="form-actions">
                    <button class="btn" @click="showVariantModal = false">Cancel</button>
                </div>
            </div>
        </div>

        <!-- Scanner Modal -->
        <div v-if="showScanner" class="modal-overlay" @click.self="closeScanner">
            <div class="modal card scanner-modal">
                <h3>Scan Product QR</h3>
                <div id="qr-reader" style="width: 100%;"></div>
                <div class="form-actions">
                    <button class="btn" @click="closeScanner">Close</button>
                </div>
            </div>
        </div>

    </div>
</template>

<style scoped>
.pos-layout {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 1.5rem;
    height: calc(100vh - 100px);
    overflow: hidden;
}

.products-section {
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.section-header {
    margin-bottom: 1rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.5rem;
}

.header-actions {
    display: flex;
    gap: 0.5rem;
    align-items: center;
}

.search-input {
    padding: 0.5rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    min-width: 200px;
}

.products-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 1rem;
    overflow-y: auto;
    padding-bottom: 2rem;
}

.product-card {
    cursor: pointer;
    transition: transform 0.1s, box-shadow 0.1s;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 1rem;
    height: 100%;
}

.product-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
    border-color: var(--color-primary);
}

.product-icon {
    font-size: 2.5rem;
    margin-bottom: 0.5rem;
}

.product-info h3 {
    font-size: 1rem;
    margin: 0 0 0.5rem 0;
    color: var(--color-heading);
}

.product-info p {
    margin: 0;
    font-size: 0.8rem;
    color: var(--color-text-muted);
}

.cart-section {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.customer-select {
    margin-bottom: 1rem;
}

.customer-select select {
    width: 100%;
    padding: 0.5rem;
    margin-top: 0.25rem;
    border-radius: var(--radius-md);
    border: 1px solid var(--border-color);
}

.customer-loyalty-info {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-top: 0.5rem;
    font-size: 0.9rem;
}

.badge {
    padding: 0.2rem 0.5rem;
    border-radius: 99px;
    font-weight: bold;
    color: white;
    font-size: 0.75rem;
}

.badge.silver {
    background: #6c757d;
}

.badge.gold {
    background: #ffc107;
    color: black;
}

.badge.diamond {
    background: #6f42c1;
}

.points {
    color: var(--color-primary);
    font-weight: 600;
}

.cart-items {
    flex: 1;
    overflow-y: auto;
    border-top: 1px solid var(--border-color);
    border-bottom: 1px solid var(--border-color);
    padding: 1rem 0;
}

.empty-cart {
    text-align: center;
    color: var(--color-text-muted);
    margin-top: 2rem;
}

.cart-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.75rem 0;
    border-bottom: 1px dashed var(--border-color);
}

.item-details {
    display: flex;
    flex-direction: column;
}

.item-details strong {
    font-size: 0.9rem;
}

.item-details small {
    font-size: 0.8rem;
    color: var(--color-text-muted);
}

.item-actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.btn-sm {
    width: 24px;
    height: 24px;
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid var(--border-color);
    background: var(--color-surface);
    border-radius: 4px;
    cursor: pointer;
}

.btn-sm:hover {
    background: var(--color-background);
}

.item-total {
    font-weight: 600;
    min-width: 60px;
    text-align: right;
}

.cart-footer {
    padding-top: 1rem;
}

.coupon-area {
    display: flex;
    gap: 0.5rem;
    margin-bottom: 1rem;
}

.coupon-area input {
    flex: 1;
    padding: 0.5rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
}

.points-area {
    margin-bottom: 1rem;
    padding: 0.5rem;
    background-color: #f8f9fa;
    border-radius: var(--radius-md);
    border: 1px dashed var(--color-primary);
}

.points-input-group {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    justify-content: space-between;
}

.points-input-group label {
    font-size: 0.85rem;
    font-weight: 600;
}

.points-input {
    width: 60px;
    padding: 0.25rem;
    border: 1px solid var(--border-color);
    border-radius: 4px;
    text-align: right;
}

.points-value {
    color: var(--color-danger);
    font-weight: bold;
    font-size: 0.9rem;
}

.totals .row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
}

.totals .total {
    font-size: 1.25rem;
    font-weight: 700;
    color: var(--color-primary);
    border-top: 2px solid var(--border-color);
    padding-top: 0.5rem;
    margin-top: 0.5rem;
}

.pay-btn {
    margin-top: 1rem;
    height: 3rem;
    font-size: 1.1rem;
}

.btn-block {
    width: 100%;
}

.text-success {
    color: var(--color-secondary);
}

.text-danger {
    color: var(--color-danger);
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
    z-index: 2000;
}

.modal {
    width: 100%;
    max-width: 500px;
}

.scanner-modal {
    max-width: 400px;
}

.variants-list {
    display: grid;
    gap: 0.5rem;
    max-height: 400px;
    overflow-y: auto;
    margin-top: 1rem;
}

.variant-option {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1fr;
    gap: 0.5rem;
    padding: 0.75rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    cursor: pointer;
    align-items: center;
}

.variant-option:hover {
    background-color: #EEF2FF;
    border-color: var(--color-primary);
}

.v-price {
    font-weight: bold;
    color: var(--color-primary);
}

.v-stock {
    font-size: 0.85rem;
    color: var(--color-text-muted);
}

.v-sku {
    font-family: monospace;
    font-size: 0.85rem;
}

.btn-secondary {
    background-color: var(--color-surface);
    border: 1px solid var(--border-color);
    color: var(--color-primary);
}

.header-actions .btn {
    margin-left: 1rem;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1rem;
}
</style>
