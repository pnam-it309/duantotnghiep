<script setup lang="ts">
import { ref, onMounted, watch, reactive } from 'vue';
import { useRouter } from 'vue-router';
import productService, { type ProductDTO } from '../../services/productService';
import brandService, { type BrandDTO } from '../../services/brandService';
import categoryService, { type CategoryDTO } from '../../services/categoryService';
import ProductCard from '../../components/ProductCard.vue';
import ProductFilters from '../../components/ProductFilters.vue';

const router = useRouter();
const products = ref<ProductDTO[]>([]);
const brands = ref<BrandDTO[]>([]);
const categories = ref<CategoryDTO[]>([]);
const isLoading = ref(true);

// Filters State
const searchQuery = ref('');
const filters = reactive({
    categoryId: undefined as number | undefined,
    brandId: undefined as number | undefined,
    minPrice: undefined as number | undefined,
    maxPrice: undefined as number | undefined
});

const loadFilters = async () => {
    try {
        const [bRes, cRes] = await Promise.all([
            brandService.getAllBrands(),
            categoryService.getAllCategories()
        ]);
        brands.value = bRes.data;
        categories.value = cRes.data;
    } catch (e) {
        console.error("Failed to load filters", e);
    }
};

const loadProducts = async () => {
    isLoading.value = true;
    try {
        const res = await productService.searchProducts({
            keyword: searchQuery.value,
            brandId: filters.brandId,
            categoryId: filters.categoryId,
            minPrice: filters.minPrice,
            maxPrice: filters.maxPrice
        });
        products.value = res.data;
    } catch (e) {
        console.error("Failed to load products", e);
    } finally {
        isLoading.value = false;
    }
};

let debounceTimeout: any;
const debouncedLoad = () => {
    clearTimeout(debounceTimeout);
    debounceTimeout = setTimeout(loadProducts, 300);
};

// Watchers
watch([searchQuery, () => filters.brandId, () => filters.categoryId], debouncedLoad);

const applyPriceFilter = () => {
    loadProducts();
};

const resetFilters = () => {
    searchQuery.value = '';
    filters.categoryId = undefined;
    filters.brandId = undefined;
    filters.minPrice = undefined;
    filters.maxPrice = undefined;
    loadProducts();
};

const viewProduct = (id: number) => {
    router.push({ name: 'shop-product-detail', params: { id } });
};

onMounted(() => {
    loadFilters();
    loadProducts();
});
</script>

<template>
    <div class="catalog-view container">
        <div class="catalog-header">
            <h1>Shop All Products</h1>
            <div class="search-bar">
                <input type="text" v-model="searchQuery" placeholder="Search products..." class="search-input" />
            </div>
        </div>

        <div class="catalog-layout">
            <!-- Sidebar -->
            <ProductFilters :categories="categories" :brands="brands" :filters="filters"
                @update:categoryId="filters.categoryId = $event" @update:brandId="filters.brandId = $event"
                @update:minPrice="filters.minPrice = $event" @update:maxPrice="filters.maxPrice = $event"
                @apply="applyPriceFilter" @reset="resetFilters" />

            <!-- Grid -->
            <main class="product-content">
                <div v-if="isLoading" class="loading">Loading products...</div>

                <div v-else-if="products.length === 0" class="no-results">
                    No products found matching your criteria.
                </div>

                <div v-else class="product-grid">
                    <ProductCard v-for="product in products" :key="product.id" :product="product"
                        @click="viewProduct" />
                </div>
            </main>
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

.catalog-layout {
    display: flex;
    gap: 2rem;
}

.product-content {
    flex: 1;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 2rem;
}

.no-results {
    text-align: center;
    padding: 4rem;
    color: #6b7280;
    font-size: 1.1rem;
}

@media (max-width: 768px) {
    .catalog-layout {
        flex-direction: column;
    }
}
</style>
