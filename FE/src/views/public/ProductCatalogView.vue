<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import productService, { type ProductDTO } from '../../services/productService';

const router = useRouter();
const products = ref<ProductDTO[]>([]);
const isLoading = ref(true);
const searchQuery = ref('');

const loadProducts = async () => {
    isLoading.value = true;
    try {
        const res = await productService.getAllProducts();
        // Only show active products
        products.value = res.data.filter(p => p.active);
    } catch (e) {
        console.error("Failed to load products", e);
    } finally {
        isLoading.value = false;
    }
};

const filteredProducts = computed(() => {
    if (!searchQuery.value) return products.value;
    const q = searchQuery.value.toLowerCase();
    return products.value.filter(p =>
        p.name.toLowerCase().includes(q) ||
        p.categoryName?.toLowerCase().includes(q) ||
        p.brandName?.toLowerCase().includes(q)
    );
});

const viewProduct = (id: number) => {
    router.push({ name: 'shop-product-detail', params: { id } });
};

onMounted(loadProducts);
</script>

<template>
    <div class="catalog-view container">
        <div class="catalog-header">
            <h1>Shop All Products</h1>
            <input type="text" v-model="searchQuery" placeholder="Search products..." class="search-input" />
        </div>

        <div v-if="isLoading" class="loading">Loading products...</div>

        <div v-else class="product-grid">
            <div v-for="product in filteredProducts" :key="product.id" class="product-card"
                @click="viewProduct(product.id)">
                <div class="product-image">
                    <div class="placeholder-img">{{ product.name.charAt(0) }}</div>
                </div>
                <div class="product-info">
                    <div class="meta">
                        <span class="category">{{ product.categoryName }}</span>
                        <span class="brand">{{ product.brandName }}</span>
                    </div>
                    <h3>{{ product.name }}</h3>
                    <div class="price">View Details</div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.catalog-view {
    padding: 2rem 1rem;
}

.catalog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
}

.search-input {
    padding: 0.8rem;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    width: 300px;
    font-size: 1rem;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 2rem;
}

.product-card {
    background: white;
    border: 1px solid #f3f4f6;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.product-image {
    height: 200px;
    background: #f9fafb;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 4rem;
    color: #d1d5db;
}

.product-info {
    padding: 1.5rem;
}

.meta {
    display: flex;
    gap: 0.5rem;
    font-size: 0.8rem;
    color: #6b7280;
    margin-bottom: 0.5rem;
}

.meta span {
    background: #f3f4f6;
    padding: 2px 8px;
    border-radius: 4px;
}

h3 {
    margin: 0 0 1rem 0;
    font-size: 1.1rem;
    color: #111827;
    line-height: 1.4;
}

.price {
    font-weight: bold;
    color: #4f46e5;
}
</style>
