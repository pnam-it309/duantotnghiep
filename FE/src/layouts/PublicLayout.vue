<script setup lang="ts">
import { useCartStore } from '../stores/cartStore';
import { useAuthStore } from '../stores/authStore';
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';

const cartStore = useCartStore();
const authStore = useAuthStore();
const router = useRouter();
const showDropdown = ref(false);

const checkLogin = () => {
    // authStore handles state
};

const logout = () => {
    authStore.logout();
    showDropdown.value = false;
};

const toggleDropdown = () => {
    showDropdown.value = !showDropdown.value;
};

onMounted(() => {
    // Maybe check token validity?
});
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

                    <div v-if="authStore.isAuthenticated" class="user-menu">
                        <button @click="toggleDropdown" class="btn-account">
                            {{ authStore.user?.username || 'My Account' }}
                        </button>
                        <div v-if="showDropdown" class="dropdown-menu">
                            <router-link v-if="authStore.isAdmin" to="/admin/dashboard" @click="showDropdown = false">Admin Dashboard</router-link>
                            <router-link to="/profile" @click="showDropdown = false">Profile</router-link>
                            <router-link to="/orders" @click="showDropdown = false">My Orders</router-link>
                            <a href="#" @click.prevent="logout">Logout</a>
                        </div>
                    </div>
                    <div v-else class="auth-buttons">
                        <router-link to="/login" class="btn-login">Login</router-link>
                        <router-link to="/register" class="btn-register">Register</router-link>
                    </div>
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
    color: var(--color-primary);
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
    color: var(--color-primary);
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
    margin-right: 0.5rem;
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

.btn-login,
.btn-register,
.btn-account {
    padding: 0.5rem 1rem;
    color: white;
    text-decoration: none;
    border-radius: 6px;
    font-size: 0.9rem;
    transition: opacity 0.2s;
    border: none;
    cursor: pointer;
}

.btn-login {
    background: transparent;
    color: var(--color-primary);
    border: 1px solid var(--color-primary);
    margin-right: 0.5rem;
}

.btn-login:hover {
    background: #eef2ff;
}

.btn-register,
.btn-account {
    background: var(--color-primary);
    color: white;
}

.btn-register:hover,
.btn-account:hover {
    opacity: 0.9;
}

/* Dropdown */
.user-menu {
    position: relative;
}

.dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    min-width: 150px;
    margin-top: 0.5rem;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.dropdown-menu a {
    padding: 0.8rem 1rem;
    text-decoration: none;
    color: #374151;
    font-size: 0.9rem;
    transition: background 0.2s;
}

.dropdown-menu a:hover {
    background: #f3f4f6;
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
