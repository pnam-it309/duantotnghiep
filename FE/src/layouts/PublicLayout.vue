<script setup lang="ts">
import { useCartStore } from '../stores/cartStore';
import { computed } from 'vue';

const cartStore = useCartStore();
</script>

<template>
    <div class="public-layout">
        <header class="navbar">
            <div class="container">
                <router-link to="/" class="brand">
                    🛍️ E-Shop
                </router-link>

                <nav class="nav-links">
                    <router-link to="/">Home</router-link>
                    <router-link to="/products">Shop</router-link>
                </nav>

                <div class="actions">
                    <router-link to="/cart" class="cart-btn">
                        🛒 <span class="badge" v-if="cartStore.itemCount > 0">{{ cartStore.itemCount }}</span>
                    </router-link>
                    <router-link to="/login" class="btn-login">Login</router-link>
                </div>
            </div>
        </header>

        <main class="main-content">
            <router-view></router-view>
        </main>

        <footer class="footer">
            <div class="container">
                <p>&copy; 2025 E-Shop Enterprise. All rights reserved.</p>
            </div>
        </footer>
    </div>
</template>

<style scoped>
.public-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    font-family: 'Inter', sans-serif;
}

.navbar {
    background: white;
    border-bottom: 1px solid #e5e7eb;
    padding: 1rem 0;
    position: sticky;
    top: 0;
    z-index: 100;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 1rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.brand {
    font-size: 1.5rem;
    font-weight: 800;
    color: #4f46e5;
    /* Primary */
    text-decoration: none;
}

.nav-links {
    display: flex;
    gap: 2rem;
}

.nav-links a {
    text-decoration: none;
    color: #374151;
    font-weight: 500;
    transition: color 0.2s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
    color: #4f46e5;
}

.actions {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.cart-btn {
    text-decoration: none;
    font-size: 1.2rem;
    position: relative;
}

.badge {
    position: absolute;
    top: -8px;
    right: -8px;
    background: #ef4444;
    color: white;
    font-size: 0.7rem;
    font-weight: bold;
    padding: 2px 6px;
    border-radius: 99px;
}

.btn-login {
    padding: 0.5rem 1rem;
    background: #4f46e5;
    color: white;
    text-decoration: none;
    border-radius: 6px;
    font-size: 0.9rem;
    transition: opacity 0.2s;
}

.btn-login:hover {
    opacity: 0.9;
}

.main-content {
    flex: 1;
    background: #f9fafb;
}

.footer {
    background: #1f2937;
    color: #9ca3af;
    padding: 2rem 0;
    text-align: center;
    font-size: 0.9rem;
}
</style>
