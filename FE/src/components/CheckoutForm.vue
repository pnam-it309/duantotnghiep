<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '../services/api';

defineProps<{
    phoneNumber: string;
    shippingAddress: string;
    paymentMethod: string;
    isLoadingProfile: boolean;
}>();

const emit = defineEmits<{
    (e: 'update:phoneNumber', value: string): void;
    (e: 'update:shippingAddress', value: string): void;
    (e: 'update:paymentMethod', value: string): void;
}>();

const addresses = ref<any[]>([]);

const loadAddresses = async () => {
    try {
        const res = await api.get('/user/addresses');
        addresses.value = res.data;
    } catch (e) {
        console.error("Failed to load addresses", e);
    }
};

const selectAddress = (addr: any) => {
    const fullAddr = `${addr.specificAddress}, ${addr.wardName}, ${addr.districtName}, ${addr.provinceName}`;
    emit('update:shippingAddress', fullAddr);
    emit('update:phoneNumber', addr.receiverPhone);
};

onMounted(() => {
    loadAddresses();
});
</script>

<template>
    <div class="form-section">
        <h2>Shipping & Payment</h2>

        <div v-if="isLoadingProfile" class="loading">Loading details...</div>

        <div v-else class="form-content">
            <!-- Saved Addresses -->
            <div v-if="addresses.length > 0" class="form-group saved-addresses">
                <label>Saved Addresses</label>
                <div class="address-grid">
                    <div v-for="addr in addresses" :key="addr.id" class="address-card" @click="selectAddress(addr)">
                        <div class="card-header">
                            <span class="name">{{ addr.receiverName }}</span>
                            <span class="phone">{{ addr.receiverPhone }}</span>
                        </div>
                        <p class="addr-text">
                            {{ addr.specificAddress }}, {{ addr.wardName }}, {{ addr.districtName }}, {{
                            addr.provinceName }}
                        </p>
                        <button class="btn-select" type="button">Use This</button>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label>Phone Number <span class="required">*</span></label>
                <input type="tel" :value="phoneNumber"
                    @input="emit('update:phoneNumber', ($event.target as HTMLInputElement).value)"
                    placeholder="Contact number" />
            </div>

            <div class="form-group">
                <label>Shipping Address <span class="required">*</span></label>
                <textarea :value="shippingAddress"
                    @input="emit('update:shippingAddress', ($event.target as HTMLTextAreaElement).value)" rows="3"
                    placeholder="Delivery address"></textarea>
            </div>

            <div class="form-group">
                <label>Payment Method</label>
                <div class="payment-options">
                    <label class="radio-label">
                        <input type="radio" value="COD" :checked="paymentMethod === 'COD'"
                            @change="emit('update:paymentMethod', 'COD')" />
                        Cash on Delivery (COD)
                    </label>
                    <label class="radio-label">
                        <input type="radio" value="BANK_TRANSFER" :checked="paymentMethod === 'BANK_TRANSFER'"
                            @change="emit('update:paymentMethod', 'BANK_TRANSFER')" />
                        Bank Transfer
                    </label>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.form-section {
    background: white;
    padding: 2rem;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
}

.form-group {
    margin-bottom: 1.5rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.required {
    color: #ef4444;
}

.form-group input,
.form-group textarea {
    width: 100%;
    padding: 0.8rem;
    border: 1px solid #d1d5db;
    border-radius: 6px;
}

.payment-options {
    display: flex;
    flex-direction: column;
}

.radio-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
    cursor: pointer;
}

/* Address Cards */
.saved-addresses {
    border-bottom: 1px solid #e5e7eb;
    padding-bottom: 1.5rem;
    margin-bottom: 1.5rem;
}

.address-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
}

.address-card {
    border: 1px solid #d1d5db;
    padding: 1rem;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    background: #f9fafb;
}

.address-card:hover {
    border-color: var(--color-primary);
    background: #eef2ff;
}

.card-header {
    display: flex;
    justify-content: space-between;
    font-size: 0.9rem;
    margin-bottom: 0.5rem;
    font-weight: bold;
}

.addr-text {
    font-size: 0.85rem;
    color: #4b5563;
    margin-bottom: 0.8rem;
    line-height: 1.4;
}

.btn-select {
    width: 100%;
    padding: 0.4rem;
    background: white;
    border: 1px solid var(--color-primary);
    color: var(--color-primary);
    border-radius: 4px;
    font-size: 0.8rem;
    cursor: pointer;
}

.address-card:hover .btn-select {
    background: var(--color-primary);
    color: white;
}
</style>
