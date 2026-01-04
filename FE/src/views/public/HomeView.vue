<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
// We will eventually fetch featured products here.
// For now, let's just link to the catalog.

const router = useRouter();

const navigateToShop = () => {
    router.push('/products');
};
</script>

<template>
    <div class="home-view">
        <!-- Hero Section -->
        <section class="hero">
            <div class="container">
                <div class="hero-content">
                    <h1>Premium Electronics for Modern Life</h1>
                    <p>Discover the latest gadgets and accessories with exclusive deals.</p>
                    <button class="btn-primary" @click="navigateToShop">Shop Now</button>
                </div>
                <div class="hero-image">
                    <!-- Placeholder or generative image later -->
                    <div class="placeholder-img">📱💻🎧</div>
                </div>
            </div>
        </section>

        <!-- Categories / Features -->
        <section class="features container">
            <div class="feature-card">
                <h3>🚀 Fast Shipping</h3>
                <p>Delivery within 24 hours in main cities.</p>
            </div>
            <div class="feature-card">
                <h3>🛡️ Genuine Warranty</h3>
                <p>12 months official warranty for all items.</p>
            </div>
            <div class="feature-card">
                <h3>💎 Loyalty Rewards</h3>
                <p>Earn points for every purchase.</p>
            </div>
        </section>
    </div>
</template>

<style scoped>
.hero {
    background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
    color: white;
    padding: 4rem 0;
}

.hero .container {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.hero-content {
    max-width: 50%;
}

.hero h1 {
    font-size: 3rem;
    font-weight: 800;
    margin-bottom: 1rem;
    line-height: 1.2;
}

.hero p {
    font-size: 1.2rem;
    margin-bottom: 2rem;
    opacity: 0.9;
}

.btn-primary {
    background: white;
    color: #4f46e5;
    border: none;
    padding: 0.8rem 2rem;
    font-size: 1.1rem;
    font-weight: bold;
    border-radius: 99px;
    cursor: pointer;
    transition: transform 0.2s;
}

.btn-primary:hover {
    transform: scale(1.05);
}

.hero-image {
    font-size: 10rem;
    opacity: 0.8;
}

.features {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 2rem;
    margin-top: 4rem;
    margin-bottom: 4rem;
}

.feature-card {
    background: white;
    padding: 2rem;
    border-radius: 12px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    text-align: center;
}

.feature-card h3 {
    color: #1f2937;
    margin-bottom: 0.5rem;
}

.feature-card p {
    color: #6b7280;
}
</style>
