<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import productService, { type ProductDTO } from '../../services/productService';
import productVariantService, { type ProductVariantDTO } from '../../services/productVariantService';
import { useCartStore } from '../../stores/cartStore';
import ProductReviews from '../../components/ProductReviews.vue';

const route = useRoute();
const cartStore = useCartStore();

const product = ref<ProductDTO | null>(null);
const variants = ref<ProductVariantDTO[]>([]);
const isLoading = ref(true);

// Selection state
const selectedColorId = ref<number | null>(null);
const selectedSizeId = ref<number | null>(null);
const quantity = ref(1);

// Recommendations
const recommendations = ref<ProductDTO[]>([]);

const loadData = async () => {
    const id = Number(route.params.id);
    if (!id) return;

    isLoading.value = true;
    window.scrollTo(0, 0); // Scroll top on route change
    try {
        const [pRes, vRes, rRes] = await Promise.all([
            productService.getProductById(id),
            productVariantService.getVariantsByProduct(id),
            api.get('/products/recommendations', { params: { type: 'similar', productId: id } })
        ]);
        product.value = pRes.data;
        variants.value = vRes.data;
        recommendations.value = rRes.data;

        // Pre-select first available option
        if (variants.value.length > 0 && variants.value[0]) {
            const first = variants.value[0];
            selectedColorId.value = first.colorId;
            selectedSizeId.value = first.sizeId;
        }
    } catch (e) {
        console.error("Error loading product", e);
    } finally {
        isLoading.value = false;
    }
};

// Unique Colors
const colors = computed(() => {
    const map = new Map();
    variants.value.forEach(v => {
        if (!map.has(v.colorId)) {
            map.set(v.colorId, { id: v.colorId, name: v.colorName });
        }
    });
    return Array.from(map.values());
});

// Unique Sizes
const sizes = computed(() => {
    const map = new Map();
    variants.value.forEach(v => {
        if (!map.has(v.sizeId)) {
            map.set(v.sizeId, { id: v.sizeId, name: v.sizeValue });
        }
    });
    return Array.from(map.values());
});

// Find variant based on selection
const selectedVariant = computed(() => {
    return variants.value.find(v =>
        v.colorId === selectedColorId.value &&
        v.sizeId === selectedSizeId.value
    );
});

const canAddToCart = computed(() => {
    return selectedVariant.value && selectedVariant.value.stockQuantity > 0;
});

const stockLabel = computed(() => {
    if (!selectedVariant.value) return '';
    return selectedVariant.value.stockQuantity > 0
        ? `In Stock (${selectedVariant.value.stockQuantity})`
        : 'Out of Stock';
});

const addToCart = () => {
    if (!selectedVariant.value || !product.value) return;

    cartStore.addItem({
        variantId: selectedVariant.value.id,
        productId: product.value.id,
        productName: product.value.name,
        variantName: `${selectedVariant.value.colorName} - ${selectedVariant.value.sizeValue}`,
        sku: selectedVariant.value.sku,
        price: selectedVariant.value.price,
        quantity: quantity.value
    });
    alert('Added to cart');
};

watch(() => route.params.id, loadData);
onMounted(loadData);
</script>

<template>
    <div class="product-detail-view container" v-if="product">
        <div class="grid">
            <!-- Image -->
            <div class="product-image">
                <div class="placeholder-img">{{ product.name.charAt(0) }}</div>
            </div>

            <!-- Info -->
            <div class="product-info">
                <h1>{{ product.name }}</h1>
                <div class="meta">
                    <span class="badge">{{ product.brandName }}</span>
                    <span class="badge">{{ product.categoryName }}</span>
                </div>

                <div class="price-section" v-if="selectedVariant">
                    <span class="price">${{ selectedVariant.price.toLocaleString() }}</span>
                    <span class="stock" :class="{ 'in-stock': selectedVariant.stockQuantity > 0 }">
                        {{ stockLabel }}
                    </span>
                </div>
                <div class="price-section" v-else>
                    <span class="price-placeholder">Select options to see price</span>
                </div>

                <p class="description">{{ product.description }}</p>

                <!-- Selectors -->
                <div class="selectors">
                    <div class="selector-group">
                        <label>Color</label>
                        <div class="options">
                            <button v-for="c in colors" :key="c.id" class="option-btn"
                                :class="{ active: selectedColorId === c.id }" @click="selectedColorId = c.id">
                                {{ c.name }}
                            </button>
                        </div>
                    </div>

                    <div class="selector-group">
                        <label>Size</label>
                        <div class="options">
                            <button v-for="s in sizes" :key="s.id" class="option-btn"
                                :class="{ active: selectedSizeId === s.id }" @click="selectedSizeId = s.id">
                                {{ s.name }}
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Add to Cart -->
                <div class="actions">
                    <div class="qty-control">
                        <button @click="quantity > 1 && quantity--">-</button>
                        <input type="number" v-model="quantity" readonly />
                        <button @click="quantity++">+</button>
                    </div>
                    <button class="btn-add-cart" :disabled="!canAddToCart" @click="addToCart">
                        {{ canAddToCart ? 'Add to Cart' : 'Out of Stock' }}
                    </button>
                </div>
            </div>
        </div>

        <ProductReviews :productId="product.id" />

        <!-- AI Recommendations -->
        <div class="recommendations-section" v-if="recommendations.length > 0">
            <h2>You might also like (AI Recommended)</h2>
            <div class="rec-grid">
                <div v-for="rec in recommendations" :key="rec.id" class="rec-card"
                    @click="router.push(`/products/${rec.id}`)">
                    <div class="rec-img">{{ rec.name.charAt(0) }}</div>
                    <div class="rec-info">
                        <h3>{{ rec.name }}</h3>
                        <div class="rec-price">${{ rec.startPrice ? rec.startPrice.toLocaleString() : 'N/A' }}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div v-else class="loading container">
        Loading...
    </div>
</template>

<style scoped>
.product-detail-view {
    padding: 3rem 1rem;
}

.grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 4rem;
}

.product-image {
    background: #f3f4f6;
    border-radius: 16px;
    height: 500px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 8rem;
    color: #d1d5db;
}

.product-info h1 {
    font-size: 2.5rem;
    margin-bottom: 1rem;
    line-height: 1.2;
}

.meta {
    display: flex;
    gap: 0.5rem;
    margin-bottom: 2rem;
}

.badge {
    background: #f3f4f6;
    padding: 0.25rem 0.75rem;
    border-radius: 99px;
    font-size: 0.9rem;
    color: #4b5563;
}

.price-section {
    margin-bottom: 2rem;
    display: flex;
    align-items: center;
    gap: 1rem;
}

.price {
    font-size: 2rem;
    font-weight: 700;
    color: var(--color-primary);
}

.stock {
    font-weight: 600;
    color: #ef4444;
}

.stock.in-stock {
    color: #10b981;
}

.description {
    color: #4b5563;
    line-height: 1.6;
    margin-bottom: 2rem;
}

.selector-group {
    margin-bottom: 1.5rem;
}

.selector-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
}

.options {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.option-btn {
    padding: 0.5rem 1rem;
    border: 1px solid #d1d5db;
    background: white;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
}

.option-btn:hover {
    border-color: var(--color-primary);
}

.option-btn.active {
    background: var(--color-primary);
    color: white;
    border-color: var(--color-primary);
}

.actions {
    display: flex;
    gap: 1rem;
    margin-top: 2rem;
}

.qty-control {
    display: flex;
    align-items: center;
    border: 1px solid #d1d5db;
    border-radius: 6px;
}

.qty-control button {
    width: 40px;
    height: 100%;
    border: none;
    background: none;
    cursor: pointer;
    font-size: 1.2rem;
}

.qty-control input {
    width: 50px;
    text-align: center;
    border: none;
    outline: none;
    font-weight: bold;
}

.btn-add-cart {
    flex: 1;
    background: var(--color-primary);
    color: white;
    border: none;
    padding: 1rem;
    font-size: 1.1rem;
    font-weight: bold;
    border-radius: 6px;
    cursor: pointer;
    transition: background 0.2s;
}

.btn-add-cart:disabled {
    background: #d1d5db;
    cursor: not-allowed;
}

.btn-add-cart:hover:not(:disabled) {
    background: var(--color-primary-hover);
}

@media (max-width: 768px) {
    .grid {
        grid-template-columns: 1fr;
    }
}

.recommendations-section {
    margin-top: 4rem;
    padding-top: 2rem;
    border-top: 1px solid #e5e7eb;
}

.rec-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1.5rem;
    margin-top: 1.5rem;
}

.rec-card {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    cursor: pointer;
    transition: transform 0.2s;
}

.rec-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.rec-img {
    height: 150px;
    background: #f3f4f6;
    border-radius: 8px 8px 0 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 3rem;
    color: #d1d5db;
}

.rec-info {
    padding: 1rem;
}

.rec-info h3 {
    font-size: 1rem;
    margin: 0 0 0.5rem 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.rec-price {
    font-weight: bold;
    color: var(--color-primary);
}
</style>
