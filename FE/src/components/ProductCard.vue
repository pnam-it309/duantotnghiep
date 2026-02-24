<script setup lang="ts">
import { type ProductDTO } from '../services/productService';

defineProps<{ product: ProductDTO; }>();

const emit = defineEmits<{ (e: 'click', id: number): void; }>();
</script>

<template>
    <div class="product-card" @click="emit('click', product.id)">
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
</template>

<style scoped>
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
    color: var(--color-primary);
}
</style>
