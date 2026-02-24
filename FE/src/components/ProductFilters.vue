<script setup lang="ts">
import { type BrandDTO } from '../services/brandService';
import { type CategoryDTO } from '../services/categoryService';

interface FilterState {
    categoryId: number | undefined;
    brandId: number | undefined;
    minPrice: number | undefined;
    maxPrice: number | undefined;
}

const props = defineProps<{
    categories: CategoryDTO[];
    brands: BrandDTO[];
    filters: FilterState;
}>();

const emit = defineEmits<{
    (e: 'update:categoryId', value: number | undefined): void;
    (e: 'update:brandId', value: number | undefined): void;
    (e: 'update:minPrice', value: number | undefined): void;
    (e: 'update:maxPrice', value: number | undefined): void;
    (e: 'apply'): void;
    (e: 'reset'): void;
}>();

</script>

<template>
    <aside class="filters-sidebar">
        <div class="filter-group">
            <h3>Categories</h3>
            <div class="filter-options">
                <label class="radio-label">
                    <input type="radio" :value="undefined" :checked="filters.categoryId === undefined"
                        @change="emit('update:categoryId', undefined)" />
                    All
                </label>
                <label v-for="c in categories" :key="c.id" class="radio-label">
                    <input type="radio" :value="c.id" :checked="filters.categoryId === c.id"
                        @change="emit('update:categoryId', c.id)" />
                    {{ c.name }}
                </label>
            </div>
        </div>

        <div class="filter-group">
            <h3>Brands</h3>
            <div class="filter-options">
                <label class="radio-label">
                    <input type="radio" :value="undefined" :checked="filters.brandId === undefined"
                        @change="emit('update:brandId', undefined)" />
                    All
                </label>
                <label v-for="b in brands" :key="b.id" class="radio-label">
                    <input type="radio" :value="b.id" :checked="filters.brandId === b.id"
                        @change="emit('update:brandId', b.id)" />
                    {{ b.name }}
                </label>
            </div>
        </div>

        <div class="filter-group">
            <h3>Price Range</h3>
            <div class="price-inputs">
                <input type="number" :value="filters.minPrice"
                    @input="emit('update:minPrice', Number(($event.target as HTMLInputElement).value) || undefined)"
                    placeholder="Min" />
                <span>-</span>
                <input type="number" :value="filters.maxPrice"
                    @input="emit('update:maxPrice', Number(($event.target as HTMLInputElement).value) || undefined)"
                    placeholder="Max" />
            </div>
            <button class="btn-apply" @click="emit('apply')">Apply</button>
        </div>

        <button class="btn-reset" @click="emit('reset')">Reset Filters</button>
    </aside>
</template>

<style scoped>
.filters-sidebar {
    width: 250px;
    flex-shrink: 0;
    padding-right: 1rem;
    border-right: 1px solid #f3f4f6;
}

.filter-group {
    margin-bottom: 2rem;
}

.filter-group h3 {
    margin-bottom: 1rem;
    font-size: 1.1rem;
    color: #111827;
}

.filter-options {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.radio-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    color: #4b5563;
}

.price-inputs {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
}

.price-inputs input {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #d1d5db;
    border-radius: 4px;
}

.btn-apply {
    width: 100%;
    padding: 0.5rem;
    background: var(--color-primary);
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.btn-reset {
    width: 100%;
    padding: 0.5rem;
    background: white;
    border: 1px solid #d1d5db;
    color: #4b5563;
    border-radius: 4px;
    cursor: pointer;
}

.btn-reset:hover {
    background: #f9fafb;
}

@media (max-width: 768px) {
    .filters-sidebar {
        width: 100%;
        border-right: none;
        border-bottom: 1px solid #f3f4f6;
        padding-bottom: 2rem;
    }
}
</style>
