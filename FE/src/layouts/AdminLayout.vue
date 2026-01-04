<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo">
        <h2>BanHang Admin</h2>
      </div>
      <nav class="nav-links">
        <RouterLink to="/admin/dashboard" class="nav-item">Dashboard</RouterLink>
        <RouterLink to="/admin/brands" class="nav-item">Brands</RouterLink>
        <RouterLink to="/admin/categories" class="nav-item">Categories</RouterLink>
        <RouterLink to="/admin/colors" class="nav-item">Colors</RouterLink>
        <RouterLink to="/admin/sizes" class="nav-item">Sizes</RouterLink>
        <RouterLink to="/admin/products" class="nav-item">Products</RouterLink>
        <RouterLink to="/admin/suppliers" class="nav-item">Suppliers</RouterLink>
        <div class="nav-separator"></div>
        <RouterLink to="/admin/coupons" class="nav-item">Coupons</RouterLink>
        <RouterLink to="/admin/discounts" class="nav-item">Discounts</RouterLink>
        <RouterLink to="/admin/pos" class="nav-item">🛒 POS (New Sale)</RouterLink>
        <RouterLink to="/admin/goods-receipts" class="nav-item">📦 Inventory Import</RouterLink>
        <RouterLink to="/admin/orders" class="nav-item">Orders</RouterLink>
        <RouterLink to="/admin/returns" class="nav-item">Returns</RouterLink>
        <RouterLink to="/admin/users" class="nav-item">Users</RouterLink>
      </nav>
    </aside>

    <main class="main-content">
      <header class="top-bar">
        <h3>Dashboard</h3>
      </header>
      <div class="content-wrapper">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 250px;
  background-color: var(--color-surface);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.logo h2 {
  font-size: 1.25rem;
  color: var(--color-primary);
}

.nav-links {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  color: var(--color-text-muted);
  font-weight: 500;
  transition: all 0.2s;
}

.nav-item:hover,
.nav-item.router-link-active {
  background-color: #EEF2FF;
  /* Light indigo */
  color: var(--color-primary);
}

.nav-separator {
  height: 1px;
  background-color: var(--border-color);
  margin: 0.5rem 0;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.top-bar {
  height: 64px;
  background-color: var(--color-surface);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 2rem;
}

.content-wrapper {
  padding: 2rem;
  flex: 1;
  overflow-y: auto;
}
</style>
