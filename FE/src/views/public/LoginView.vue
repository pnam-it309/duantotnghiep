<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '../../stores/authStore';

const authStore = useAuthStore();
const username = ref('');
const password = ref('');
const isLoading = ref(false);

const handleLogin = async () => {
    if (!username.value || !password.value) return;

    isLoading.value = true;
    try {
        await authStore.login({
            username: username.value,
            password: password.value
        });
    } catch (e) {
        // Error handled in store
    } finally {
        isLoading.value = false;
    }
};
</script>

<template>
    <div class="login-view container">
        <div class="login-card">
            <h1>Welcome Back</h1>
            <p>Sign in to your account</p>

            <form @submit.prevent="handleLogin">
                <input type="text" v-model="username" placeholder="Username" required />
                <input type="password" v-model="password" placeholder="Password" required />

                <div v-if="authStore.error" class="error-msg">{{ authStore.error }}</div>

                <button type="submit" :disabled="isLoading">
                    {{ isLoading ? 'Signing in...' : 'Login' }}
                </button>
            </form>

            <p class="hint">
                Don't have an account? <router-link to="/register">Register here</router-link>
            </p>
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
    background: var(--color-primary);
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 1.1rem;
    font-weight: bold;
    cursor: pointer;
}

button:hover {
    background: var(--color-primary-hover);
}

.hint {
    margin-top: 1rem;
    color: #6b7280;
    font-size: 0.9rem;
}
</style>
