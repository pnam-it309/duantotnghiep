<template>
    <button @click="toggleWishlist" class="wishlist-btn" :class="{ active: isInWishlist }" :disabled="isLoading">
        <i :class="isInWishlist ? 'fas fa-heart' : 'far fa-heart'"></i>
    </button>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import wishlistService from '../services/wishlistService';
import { useAuthStore } from '../stores/authStore';

const props = defineProps<{
    productId: number;
}>();

const authStore = useAuthStore();
const isInWishlist = ref(false);
const isLoading = ref(false);

const checkWishlistStatus = async () => {
    if (!authStore.isLoggedIn) return;

    try {
        isInWishlist.value = await wishlistService.checkInWishlist(props.productId);
    } catch (error) {
        console.error('Failed to check wishlist status', error);
    }
};

const toggleWishlist = async () => {
    if (!authStore.isLoggedIn) {
        alert('Vui lòng đăng nhập để sử dụng Wishlist');
        return;
    }

    isLoading.value = true;

    try {
        if (isInWishlist.value) {
            await wishlistService.remove(props.productId);
            isInWishlist.value = false;
        } else {
            await wishlistService.add(props.productId);
            isInWishlist.value = true;
        }
    } catch (error: any) {
        console.error('Wishlist operation failed', error);
        alert(error.response?.data?.message || 'Có lỗi xảy ra');
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => {
    checkWishlistStatus();
});
</script>

<style scoped>
.wishlist-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1.5rem;
    color: #666;
    transition: all 0.3s ease;
    padding: 0.5rem;
}

.wishlist-btn:hover {
    color: #ff4757;
    transform: scale(1.1);
}

.wishlist-btn.active {
    color: #ff4757;
}

.wishlist-btn.active i {
    animation: heartbeat 0.3s;
}

.wishlist-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

@keyframes heartbeat {
    0% {
        transform: scale(1);
    }

    50% {
        transform: scale(1.3);
    }

    100% {
        transform: scale(1);
    }
}
</style>
