<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '../../stores/authStore';

const authStore = useAuthStore();
const username = ref('');
const password = ref('');
const email = ref('');
const fullName = ref('');
const isLoading = ref(false);

const handleRegister = async () => {
    isLoading.value = true;
    try {
        await authStore.register({
            username: username.value,
            password: password.value,
            email: email.value,
            fullName: fullName.value
        });
        alert('Registration successful! Please login.');
    } catch (e) {
        // Error handled in store
    } finally {
        isLoading.value = false;
    }
};
</script>

<template>
    <div class="register-view container">
        <div class="login-card">
            <h1>Create Account</h1>
            <p>Join us today</p>

            <form @submit.prevent="handleRegister">
                <input type="text" v-model="fullName" placeholder="Full Name" required />
                <input type="email" v-model="email" placeholder="Email" required />
                <input type="text" v-model="username" placeholder="Username" required />
                <input type="password" v-model="password" placeholder="Password" required />

                <div v-if="authStore.error" class="error-msg">{{ authStore.error }}</div>

                <button type="submit" :disabled="isLoading">
                    {{ isLoading ? 'Creating Account...' : 'Register' }}
                </button>
            </form>

            <p class="hint">
                Already have an account? <router-link to="/login">Login here</router-link>
            </p>
        </div>
    </div>
</template>

<style scoped>
.register-view {
    display: flex;
    justify-content: center;
    padding-top: 5rem;
    padding-bottom: 5rem;
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
    margin: 0.5rem 0;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 1rem;
}

button {
    width: 100%;
    padding: 1rem;
    margin-top: 1rem;
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

.error-msg {
    color: #ef4444;
    margin: 0.5rem 0;
    font-size: 0.9rem;
}
</style>
