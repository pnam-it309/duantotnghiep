<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
// import { useCartStore } from '../../stores/cartStore';

const router = useRouter();
const route = useRoute();
const userId = ref('');

const login = () => {
    if (!userId.value) return;
    localStorage.setItem('userId', userId.value);

    // Redirect
    const redirect = route.query.redirect as string || '/';
    router.push(redirect);
};
</script>

<template>
    <div class="login-view container">
        <div class="login-card">
            <h1>Welcome Back</h1>
            <p>Enter your User ID to login (Simulated for MVP)</p>

            <form @submit.prevent="login">
                <input type="number" v-model="userId" placeholder="User ID" required />
                <button type="submit">Login</button>
            </form>

            <p class="hint">Don't have an ID? Check Admin > Users to find one (e.g., 1).</p>
        </div>
    </div>
</template>

<style scoped>
.login-view {
    display: flex;
    justify-content: center;
    padding-top: 5rem;
}

.login-card {
    background: white;
    padding: 2rem;
    border-radius: 12px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 400px;
    text-align: center;
}

h1 {
    margin-bottom: 0.5rem;
}

input {
    width: 100%;
    padding: 1rem;
    margin: 1.5rem 0;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 1rem;
}

button {
    width: 100%;
    padding: 1rem;
    background: #4f46e5;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 1.1rem;
    font-weight: bold;
    cursor: pointer;
}

button:hover {
    background: #4338ca;
}

.hint {
    margin-top: 1rem;
    color: #6b7280;
    font-size: 0.9rem;
}
</style>
