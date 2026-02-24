<template>
    <div class="wishlist-view container">
        <h1>Danh Sách Yêu Thích</h1>

        <div v-if="loading" class="loading">
            <p>Đang tải...</p>
        </div>

        <div v-else-if="products.length === 0" class="empty-state">
            <i class="far fa-heart"></i>
            <h2>Chưa có sản phẩm yêu thích</h2>
            <p>Hãy thêm những sản phẩm bạn quan tâm vào danh sách!</p>
            <router-link to="/products" class="btn-primary">Khám phá sản phẩm</router-link>
        </div>

        <div v-else class="wishlist-grid">
            <ProductCard v-for="product in products" :key="product.id" :product="product" @removed="loadWishlist" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import wishlistService from '../../services/wishlistService';
import ProductCard from '../../components/ProductCard.vue';

const products = ref<any[]>([]);
const loading = ref(true);

const loadWishlist = async () => {
    loading.value = true;
    try {
        products.value = await wishlistService.getAll();
    } catch (error) {
        console.error('Failed to load wishlist', error);
        alert('Không thể tải danh sách yêu thích');
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    loadWishlist();
});
</script>

<style scoped>
.wishlist-view {
    padding: 3rem 1rem;
    min-height: 70vh;
}

h1 {
    margin-bottom: 2rem;
    color: #2c3e50;
}

.loading {
    text-align: center;
    padding: 4rem;
    color: #666;
}

.empty-state {
    text-align: center;
    padding: 4rem 2rem;
    color: #666;
}

.empty-state i {
    font-size: 5rem;
    color: #e0e0e0;
    margin-bottom: 1rem;
}

.empty-state h2 {
    color: #2c3e50;
    margin-bottom: 1rem;
}

.btn-primary {
    display: inline-block;
    background: var(--color-primary);
    color: white;
    padding: 0.8rem 1.5rem;
    border-radius: 6px;
    text-decoration: none;
    margin-top: 1rem;
    transition: background 0.3s;
}

.btn-primary:hover {
    background: var(--color-primary-hover);
}

.wishlist-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 2rem;
    margin-top: 2rem;
}

@media (max-width: 768px) {
    .wishlist-grid {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 1rem;
    }
}
</style>
