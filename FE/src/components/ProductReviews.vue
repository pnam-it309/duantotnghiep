<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import reviewService, { type ReviewDTO } from '../services/reviewService';
import api from '../services/api';
import { useAuthStore } from '../stores/authStore';

const props = defineProps<{
    productId: number;
}>();

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const reviews = ref<ReviewDTO[]>([]);
const newReviewContent = ref('');
const newReviewRating = ref(5);
const isSubmittingReview = ref(false);
const uploadedImages = ref<string[]>([]);
const fileInput = ref<HTMLInputElement | null>(null);

const loadReviews = async () => {
    if (!props.productId) return;
    try {
        const res = await reviewService.getReviewsByProduct(props.productId);
        reviews.value = res.data;
    } catch (e) {
        console.error("Failed to load reviews", e);
    }
};

const handleImageUpload = async (event: Event) => {
    const files = (event.target as HTMLInputElement).files;
    if (!files) return;

    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        if (file.size > 5 * 1024 * 1024) {
            alert(`File ${file.name} too large (max 5MB)`);
            continue;
        }

        const formData = new FormData();
        formData.append('file', file);

        try {
            const res = await api.post('/uploads', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            uploadedImages.value.push(res.data); // URL returned
        } catch (e) {
            console.error("Upload failed", e);
            alert("Failed to upload image");
        }
    }
    // Reset input
    if (fileInput.value) fileInput.value.value = '';
};

const removeImage = (index: number) => {
    uploadedImages.value.splice(index, 1);
};

const submitReview = async () => {
    if (!authStore.isAuthenticated) {
        if (confirm('You must be logged in to review. Go to login?')) {
            router.push({ name: 'shop-login', query: { redirect: route.fullPath } });
        }
        return;
    }

    if (!newReviewContent.value.trim()) {
        alert('Please write a comment');
        return;
    }

    isSubmittingReview.value = true;
    try {
        const review: ReviewDTO = {
            productId: props.productId,
            userId: authStore.user?.id || 0,
            content: newReviewContent.value,
            rating: newReviewRating.value,
            imageUrls: uploadedImages.value
        };
        const res = await reviewService.createReview(review);
        reviews.value.unshift(res.data);

        // Reset form
        newReviewContent.value = '';
        newReviewRating.value = 5;
        uploadedImages.value = [];
        alert('Review submitted!');
    } catch (e) {
        console.error("Failed to submit review", e);
        alert('Failed to submit review');
    } finally {
        isSubmittingReview.value = false;
    }
};

const formatDate = (dateStr?: string) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString();
};

const getImageUrl = (url: string) => {
    if (url.startsWith('http')) return url;
    return `http://localhost:8080${url}`; // Adjust backend URL
};

watch(() => props.productId, loadReviews);
onMounted(loadReviews);
</script>

<template>
    <div class="reviews-section">
        <h2>Customer Reviews ({{ reviews.length }})</h2>

        <!-- Write Review -->
        <div class="write-review">
            <h3>Write a Review</h3>
            <div class="rating-input">
                <label>Rating:</label>
                <select v-model="newReviewRating">
                    <option :value="5">⭐⭐⭐⭐⭐ (Excellent)</option>
                    <option :value="4">⭐⭐⭐⭐ (Good)</option>
                    <option :value="3">⭐⭐⭐ (Average)</option>
                    <option :value="2">⭐⭐ (Poor)</option>
                    <option :value="1">⭐ (Terrible)</option>
                </select>
            </div>

            <textarea v-model="newReviewContent" placeholder="Share your thoughts about this product..."></textarea>

            <!-- Image Upload -->
            <div class="image-upload-section">
                <div class="upload-btn-wrapper">
                    <button class="btn-upload"><i class="fas fa-camera"></i> Add Photos</button>
                    <input type="file" ref="fileInput" multiple accept="image/*" @change="handleImageUpload" />
                </div>

                <div class="preview-grid" v-if="uploadedImages.length > 0">
                    <div v-for="(img, idx) in uploadedImages" :key="idx" class="preview-item">
                        <img :src="getImageUrl(img)" alt="Preview" />
                        <button type="button" @click="removeImage(idx)" class="btn-remove-img">×</button>
                    </div>
                </div>
            </div>

            <button class="btn-submit" @click="submitReview" :disabled="isSubmittingReview">
                {{ isSubmittingReview ? 'Submitting...' : 'Submit Review' }}
            </button>
        </div>

        <!-- List Reviews -->
        <div class="review-list">
            <div v-if="reviews.length === 0" class="no-reviews">
                No reviews yet. Be the first to review!
            </div>
            <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                    <div>
                        <span class="reviewer">{{ review.username || 'Anonymous' }}</span>
                        <span v-if="review.isVerifiedPurchase" class="verified-badge">
                            <i class="fas fa-check-circle"></i> Verified Purchase
                        </span>
                    </div>
                    <span class="date">{{ formatDate(review.createdAt) }}</span>
                </div>
                <div class="rating-display">
                    <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= review.rating }">★</span>
                </div>
                <p class="review-content">{{ review.content }}</p>

                <!-- Review Images -->
                <div v-if="review.imageUrls && review.imageUrls.length > 0" class="review-images">
                    <img v-for="(img, idx) in review.imageUrls" :key="idx" :src="getImageUrl(img)"
                        class="review-img-thumb" alt="Review Image" />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.reviews-section {
    margin-top: 4rem;
    padding-top: 2rem;
    border-top: 1px solid #e5e7eb;
}

.write-review {
    background: #f9fafb;
    padding: 1.5rem;
    border-radius: 12px;
    margin-bottom: 2rem;
}

.rating-input {
    margin-bottom: 1rem;
}

.rating-input select {
    padding: 0.5rem;
    border-radius: 4px;
    border: 1px solid #d1d5db;
    margin-left: 0.5rem;
}

.write-review textarea {
    width: 100%;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    padding: 0.8rem;
    margin: 1rem 0;
    font-family: inherit;
}

/* Image Upload Styles */
.image-upload-section {
    margin-bottom: 1rem;
}

.upload-btn-wrapper {
    position: relative;
    overflow: hidden;
    display: inline-block;
}

.btn-upload {
    border: 1px dashed var(--color-primary);
    color: var(--color-primary);
    background-color: white;
    padding: 8px 20px;
    border-radius: 8px;
    font-weight: bold;
    cursor: pointer;
}

.upload-btn-wrapper input[type=file] {
    font-size: 100px;
    position: absolute;
    left: 0;
    top: 0;
    opacity: 0;
    cursor: pointer;
}

.preview-grid {
    display: flex;
    gap: 10px;
    overflow-x: auto;
    margin-top: 10px;
    padding-bottom: 5px;
}

.preview-item {
    position: relative;
    width: 80px;
    height: 80px;
}

.preview-item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 6px;
}

.btn-remove-img {
    position: absolute;
    top: -5px;
    right: -5px;
    background: red;
    color: white;
    border: none;
    border-radius: 50%;
    width: 20px;
    height: 20px;
    font-size: 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
}

.btn-submit {
    padding: 0.8rem 1.5rem;
    background: var(--color-primary);
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: bold;
}

.btn-submit:disabled {
    background: #9ca3af;
}

.review-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.review-item {
    padding-bottom: 1.5rem;
    border-bottom: 1px solid #f3f4f6;
}

.review-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.5rem;
    font-size: 0.9rem;
    color: #6b7280;
}

.reviewer {
    font-weight: bold;
    color: #111827;
}

.verified-badge {
    color: #10b981;
    font-size: 0.8rem;
    margin-left: 8px;
}

.star {
    color: #d1d5db;
    font-size: 1.2rem;
}

.star.filled {
    color: #fbbf24;
}

.review-images {
    display: flex;
    gap: 10px;
    margin-top: 10px;
}

.review-img-thumb {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border-radius: 6px;
    cursor: pointer;
}
</style>
