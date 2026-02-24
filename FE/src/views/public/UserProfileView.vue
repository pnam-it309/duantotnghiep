<script setup lang="ts">
import { ref, onMounted } from 'vue';
import userService, { type UserDTO } from '../../services/userService';
import api from '../../services/api';
import { useRouter } from 'vue-router';
import AddressForm from '../../components/AddressForm.vue';

const router = useRouter();
const user = ref<UserDTO | null>(null);
const addresses = ref<any[]>([]);
const isLoading = ref(true);
const isSaving = ref(false);
const message = ref('');

const showAddressForm = ref(false);
const editingAddress = ref<any>(null);

const loyalty = ref<any>(null);

const loadProfile = async () => {
    const userId = localStorage.getItem('userId');
    if (!userId) {
        router.push('/login');
        return;
    }

    isLoading.value = true;
    try {
        const [userRes, addrRes, loyRes] = await Promise.all([
            userService.getUserById(Number(userId)),
            api.get('/user/addresses'),
            api.get('/loyalty/my-points').catch(() => ({ data: null })) // Soft fail
        ]);
        user.value = userRes.data;
        addresses.value = addrRes.data;
        loyalty.value = loyRes.data;
    } catch (e) {
        console.error("Failed to load profile", e);
    } finally {
        isLoading.value = false;
    }
};

const saveProfile = async () => {
    if (!user.value) return;

    isSaving.value = true;
    message.value = '';
    try {
        await userService.updateProfile(user.value.id, {
            fullName: user.value.fullName,
            phoneNumber: user.value.phoneNumber,
            // address field deprecated, but keeping for compatibility if needed
        });
        message.value = 'Profile updated successfully!';
    } catch (e) {
        console.error("Failed to update profile", e);
        message.value = 'Failed to update profile.';
    } finally {
        isSaving.value = false;
    }
};

const handleAddAddress = () => {
    editingAddress.value = null;
    showAddressForm.value = true;
};

const handleEditAddress = (addr: any) => {
    editingAddress.value = { ...addr }; // Clone
    showAddressForm.value = true;
};

const handleDeleteAddress = async (id: number) => {
    if (!confirm('Are you sure you want to delete this address?')) return;
    try {
        await api.delete(`/user/addresses/${id}`);
        await loadAddresses();
    } catch (e) {
        alert('Failed to delete address');
    }
};

const handleSetDefault = async (id: number) => {
    try {
        await api.put(`/user/addresses/${id}/default`);
        await loadAddresses();
    } catch (e) {
        alert('Failed to set default address');
    }
};

const loadAddresses = async () => {
    try {
        const res = await api.get('/user/addresses');
        addresses.value = res.data;
    } catch (e) {
        console.error("Failed to reload addresses", e);
    }
};

const onAddressSaved = async () => {
    showAddressForm.value = false;
    await loadAddresses();
};

onMounted(loadProfile);
</script>

<template>
    <div class="profile-view container">
        <h1>My Profile</h1>

        <div v-if="isLoading" class="loading">Loading profile...</div>

        <div v-else-if="user" class="profile-layout">
            <!-- Left Column: Personal Info -->
            <div class="profile-card">
                <h2>Personal Information</h2>
                <div class="form-group">
                    <label>Username</label>
                    <input type="text" :value="user.username" disabled class="disabled-input" />
                </div>

                <div class="form-group">
                    <label>Email</label>
                    <input type="email" :value="user.email" disabled class="disabled-input" />
                </div>

                <div class="form-group">
                    <label>Full Name</label>
                    <input type="text" v-model="user.fullName" placeholder="Enter your full name" />
                </div>

                <div class="form-group">
                    <label>Phone Number</label>
                    <input type="tel" v-model="user.phoneNumber" placeholder="Enter your phone number" />
                </div>

                <div v-if="message" class="message" :class="{ 'error': message.includes('Failed') }">
                    {{ message }}
                </div>

                <button @click="saveProfile" :disabled="isSaving" class="btn-save">
                    {{ isSaving ? 'Saving...' : 'Save Personal Info' }}
                </button>
            </div>

            <div class="profile-column-right">
                <!-- Loyalty Card -->
                <div class="profile-card loyalty-card" v-if="loyalty">
                    <div class="loyalty-header">
                        <h2>Member Loyalty</h2>
                        <span class="tier-badge" :class="loyalty.tierLevel.toLowerCase()">{{ loyalty.tierLevel }}</span>
                    </div>
                    <div class="points-display">
                        <div class="points-value">{{ loyalty.points }}</div>
                        <div class="points-label">Points Available</div>
                    </div>
                    <div class="progress-bar">
                        <div class="fill" :style="{ width: Math.min((loyalty.points / 1000) * 100, 100) + '%' }"></div>
                    </div>
                    <p class="next-tier-text">Earn more points to reach next tier!</p>
                </div>

                <!-- Address Book -->
                <div class="address-book">
                    <div class="header">
                        <h2>Address Book</h2>
                        <button @click="handleAddAddress" class="btn-add">+ Add New</button>
                    </div>

                    <div v-if="addresses.length === 0" class="empty-state">
                        <p>No addresses saved yet.</p>
                    </div>

                    <div v-else class="address-list">
                        <div v-for="addr in addresses" :key="addr.id" class="address-item"
                            :class="{ 'default': addr.isDefault }">
                            <div class="addr-content">
                                <div class="addr-header">
                                    <span class="name">{{ addr.receiverName }}</span>
                                    <span class="phone">{{ addr.receiverPhone }}</span>
                                    <span v-if="addr.isDefault" class="badge-default">Default</span>
                                </div>
                                <p class="addr-text">
                                    {{ addr.specificAddress }}, {{ addr.wardName }}, {{ addr.districtName }}, {{
                                        addr.provinceName }}
                                </p>
                            </div>
                            <div class="addr-actions">
                                <button @click="handleEditAddress(addr)">Edit</button>
                                <button @click="handleDeleteAddress(addr.id)" class="text-red">Delete</button>
                                <button v-if="!addr.isDefault" @click="handleSetDefault(addr.id)">Set Default</button>
                            </div>
                        </div>
                    </div>

                    <!-- Address Form Modal -->
                    <div v-if="showAddressForm" class="modal-overlay">
                        <div class="modal-content">
                            <AddressForm :initialData="editingAddress" :isEditing="!!editingAddress"
                                @saved="onAddressSaved" @cancel="showAddressForm = false" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.profile-view {
    padding: 3rem 1rem;
    max-width: 1200px;
}

.profile-layout {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
}

@media (max-width: 768px) {
    .profile-layout {
        grid-template-columns: 1fr;
    }
}

.profile-card,
.address-book {
    background: white;
    padding: 2rem;
    border-radius: 12px;
    border: 1px solid #e5e7eb;
}

h2 {
    margin-top: 0;
    margin-bottom: 1.5rem;
    font-size: 1.3rem;
    border-bottom: 2px solid #f3f4f6;
    padding-bottom: 0.5rem;
}

.form-group {
    margin-bottom: 1.2rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.4rem;
    font-weight: 500;
    color: #374151;
}

.form-group input {
    width: 100%;
    padding: 0.8rem;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 1rem;
}

.disabled-input {
    background: #f3f4f6;
    color: #6b7280;
}

.btn-save {
    width: 100%;
    padding: 1rem;
    background: var(--color-primary);
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 1rem;
    font-weight: bold;
    cursor: pointer;
}

/* Address Book Styles */
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.btn-add {
    background: #10b981;
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
}

.address-list {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.address-item {
    border: 1px solid #e5e7eb;
    padding: 1rem;
    border-radius: 8px;
    background: #f9fafb;
}

.address-item.default {
    border-color: var(--color-primary);
    background: #eef2ff;
}

.addr-header {
    display: flex;
    gap: 1rem;
    align-items: center;
    margin-bottom: 0.5rem;
}

.name {
    font-weight: bold;
}

.phone {
    color: #6b7280;
    font-size: 0.9rem;
}

.badge-default {
    background: var(--color-primary);
    color: white;
    font-size: 0.7rem;
    padding: 2px 6px;
    border-radius: 99px;
}

.addr-text {
    margin: 0;
    color: #4b5563;
    font-size: 0.95rem;
}

.addr-actions {
    margin-top: 0.8rem;
    display: flex;
    gap: 1rem;
    font-size: 0.9rem;
}

.addr-actions button {
    background: none;
    border: none;
    cursor: pointer;
    color: var(--color-primary);
    padding: 0;
}

.addr-actions button.text-red {
    color: #ef4444;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 100;
}

.modal-content {
    background: white;
    border-radius: 12px;
    width: 90%;
    max-width: 600px;
    max-height: 90vh;
    overflow-y: auto;
}

.profile-column-right {
    display: flex;
    flex-direction: column;
    gap: 2rem;
}

.loyalty-card {
    background: linear-gradient(135deg, var(--color-primary) 0%, #7c3aed 100%);
    color: white;
    border: none;
}

.loyalty-card h2 {
    color: white;
    border-bottom-color: rgba(255, 255, 255, 0.2);
}

.loyalty-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.tier-badge {
    background: rgba(255, 255, 255, 0.2);
    padding: 0.25rem 0.75rem;
    border-radius: 99px;
    font-weight: bold;
    text-transform: uppercase;
    font-size: 0.8rem;
}

.points-display {
    text-align: center;
    margin: 1.5rem 0;
}

.points-value {
    font-size: 3rem;
    font-weight: 800;
}

.points-label {
    opacity: 0.9;
}

.progress-bar {
    height: 6px;
    background: rgba(0, 0, 0, 0.2);
    border-radius: 3px;
    overflow: hidden;
    margin-bottom: 0.5rem;
}

.fill {
    height: 100%;
    background: #fbbf24;
}

.next-tier-text {
    font-size: 0.85rem;
    opacity: 0.8;
    text-align: center;
}
</style>
