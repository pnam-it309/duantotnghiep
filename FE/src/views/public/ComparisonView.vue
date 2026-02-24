<template>
    <div class="comparison-view container">
        <div class="header">
            <h1>So Sánh Sản Phẩm</h1>
            <button v-if="products.length > 0" @click="comparisonStore.clear" class="btn-clear">
                Xóa tất cả
            </button>
        </div>

        <div v-if="products.length === 0" class="empty-state">
            <i class="fas fa-balance-scale"></i>
            <h2>Chưa có sản phẩm nào để so sánh</h2>
            <p>Chọn tối đa 4 sản phẩm để so sánh chi tiết</p>
            <router-link to="/products" class="btn-primary">Chọn sản phẩm</router-link>
        </div>

        <div v-else class="comparison-table-wrapper">
            <table class="comparison-table">
                <thead>
                    <tr>
                        <th class="feature-col">Tính năng</th>
                        <th v-for="product in products" :key="product.id" class="product-col">
                            <div class="product-header">
                                <button @click="comparisonStore.removeProduct(product.id)" class="btn-remove">×</button>
                                <img :src="product.image || '/placeholder.png'" class="product-img" />
                                <h3>{{ product.name }}</h3>
                                <router-link :to="`/product/${product.id}`" class="link">Chi tiết</router-link>
                            </div>
                        </th>
                        <!-- Add placeholder cols if < 4 products -->
                        <th v-for="i in (4 - products.length)" :key="`placeholder-${i}`" class="placeholder-col">
                            <div class="add-box">
                                <router-link to="/products" class="btn-add">
                                    <i class="fas fa-plus"></i> Thêm SP
                                </router-link>
                            </div>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="feature-label">Giá</td>
                        <td v-for="product in products" :key="product.id" class="price">
                            {{ new Intl.NumberFormat('vi-VN', {
                                style: 'currency', currency: 'VND'
                            }).format(product.price) }}
                        </td>
                        <td v-for="i in (4 - products.length)" :key="`p-price-${i}`"></td>
                    </tr>
                    <tr>
                        <td class="feature-label">Thương hiệu</td>
                        <td v-for="product in products" :key="product.id">{{ product.brandName || '-' }}</td>
                        <td v-for="i in (4 - products.length)" :key="`p-brand-${i}`"></td>
                    </tr>
                    <tr>
                        <td class="feature-label">Danh mục</td>
                        <td v-for="product in products" :key="product.id">{{ product.categoryName || '-' }}</td>
                        <td v-for="i in (4 - products.length)" :key="`p-cat-${i}`"></td>
                    </tr>
                    <tr>
                        <td class="feature-label">Đánh giá</td>
                        <td v-for="product in products" :key="product.id">
                            <span class="stars">★ {{ product.rating }}</span>
                        </td>
                        <td v-for="i in (4 - products.length)" :key="`p-rate-${i}`"></td>
                    </tr>
                    <tr>
                        <td class="feature-label">Hành động</td>
                        <td v-for="product in products" :key="product.id">
                            <button class="btn-buy" @click="addToCart(product)">Thêm vào giỏ</button>
                        </td>
                        <td v-for="i in (4 - products.length)" :key="`p-act-${i}`"></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup lang="ts">
import { useComparisonStore } from '../stores/comparisonStore';
import { cartStore } from '../stores/cartStore'; // Check if it's named export or default

const comparisonStore = useComparisonStore();
const products = comparisonStore.products;

const addToCart = (product: any) => {
    // Assuming cartStore usage, might need to adjust based on store definition
    // For now simple alert or log if actual logic differs
    // cartStore.addToCart({ productId: product.id, quantity: 1, ... });
    alert('Feature coming soon: Quick add to cart from comparison');
};
</script>

<style scoped>
.comparison-view {
    padding: 3rem 1rem;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
}

.btn-clear {
    color: #ef4444;
    background: none;
    border: 1px solid #ef4444;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    cursor: pointer;
}

.empty-state {
    text-align: center;
    padding: 4rem;
    color: #6b7280;
}

.empty-state i {
    font-size: 4rem;
    margin-bottom: 1rem;
}

.comparison-table-wrapper {
    overflow-x: auto;
}

.comparison-table {
    width: 100%;
    border-collapse: collapse;
    min-width: 800px;
}

th,
td {
    padding: 1rem;
    border: 1px solid #e5e7eb;
    vertical-align: top;
}

.feature-col {
    width: 15%;
    background: #f9fafb;
    font-weight: bold;
}

.product-col,
.placeholder-col {
    width: 21%;
}

.product-header {
    position: relative;
    text-align: center;
}

.btn-remove {
    position: absolute;
    top: -10px;
    right: -10px;
    background: #9ca3af;
    color: white;
    border-radius: 50%;
    width: 24px;
    height: 24px;
    border: none;
    cursor: pointer;
}

.product-img {
    width: 120px;
    height: 120px;
    object-fit: cover;
    margin-bottom: 1rem;
}

.price {
    color: #ef4444;
    font-weight: bold;
    font-size: 1.1rem;
}

.add-box {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 200px;
    background: #f9fafb;
}

.btn-add {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #4b5563;
    text-decoration: none;
}

.btn-buy {
    background: var(--color-primary);
    color: white;
    border: none;
    width: 100%;
    padding: 0.5rem;
    border-radius: 4px;
    cursor: pointer;
}
</style>
