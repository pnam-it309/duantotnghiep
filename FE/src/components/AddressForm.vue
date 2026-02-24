<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import locationService, { type Province, type District, type Ward } from '../services/locationService';
import api from '../services/api';

const props = defineProps<{
    initialData?: any;
    isEditing?: boolean;
}>();

const emit = defineEmits(['saved', 'cancel']);

const provinces = ref<Province[]>([]);
const districts = ref<District[]>([]);
const wards = ref<Ward[]>([]);

const form = ref({
    receiverName: '',
    receiverPhone: '',
    provinceId: '',
    provinceName: '',
    districtId: '',
    districtName: '',
    wardCode: '',
    wardName: '',
    specificAddress: '',
    isDefault: false
});

const isLoading = ref(false);

const loadProvinces = async () => {
    provinces.value = await locationService.getProvinces();
};

const loadDistricts = async () => {
    if (form.value.provinceId) {
        districts.value = await locationService.getDistricts(form.value.provinceId);
        // Find name
        const p = provinces.value.find(x => x.id === form.value.provinceId);
        if (p) form.value.provinceName = p.name;
    } else {
        districts.value = [];
        wards.value = [];
    }
};

const loadWards = async () => {
    if (form.value.districtId) {
        wards.value = await locationService.getWards(form.value.districtId);
        // Find name
        const d = districts.value.find(x => x.id === form.value.districtId);
        if (d) form.value.districtName = d.name;
    } else {
        wards.value = [];
    }
};

const setWardName = () => {
    const w = wards.value.find(x => x.id === form.value.wardCode);
    if (w) form.value.wardName = w.name;
};

const handleSubmit = async () => {
    isLoading.value = true;
    try {
        const payload = {
            ...form.value,
            provinceId: parseInt(form.value.provinceId),
            districtId: parseInt(form.value.districtId)
        };

        if (props.isEditing && props.initialData?.id) {
            await api.put(`/user/addresses/${props.initialData.id}`, payload);
        } else {
            await api.post('/user/addresses', payload);
        }
        emit('saved');
    } catch (error) {
        console.error("Error saving address", error);
        alert("Failed to save address");
    } finally {
        isLoading.value = false;
    }
};

watch(() => form.value.provinceId, () => {
    form.value.districtId = '';
    form.value.wardCode = '';
    loadDistricts();
});

watch(() => form.value.districtId, () => {
    form.value.wardCode = '';
    loadWards();
});

watch(() => form.value.wardCode, () => {
    setWardName();
});

onMounted(async () => {
    await loadProvinces();
    if (props.initialData) {
        const data = props.initialData;
        form.value = {
            ...data,
            provinceId: data.provinceId?.toString() || '',
            districtId: data.districtId?.toString() || '',
            wardCode: data.wardCode || ''
        };
        // Trigger loads manually or wait for watcher? 
        // Watcher might clear data if we are not careful.
        // Better to manually load specific levels
        if (form.value.provinceId) {
            districts.value = await locationService.getDistricts(form.value.provinceId);
        }
        if (form.value.districtId) {
            wards.value = await locationService.getWards(form.value.districtId);
        }
    }
});
</script>

<template>
    <div class="address-form">
        <h3>{{ isEditing ? 'Edit Address' : 'Add New Address' }}</h3>
        <form @submit.prevent="handleSubmit">
            <div class="form-group">
                <label>Receiver Name</label>
                <input v-model="form.receiverName" required placeholder="John Doe" />
            </div>

            <div class="form-group">
                <label>Phone Number</label>
                <input v-model="form.receiverPhone" required placeholder="0901234567" />
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Province/City</label>
                    <select v-model="form.provinceId" required>
                        <option value="">Select Province</option>
                        <option v-for="p in provinces" :key="p.id" :value="p.id">{{ p.name }}</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>District</label>
                    <select v-model="form.districtId" required :disabled="!form.provinceId">
                        <option value="">Select District</option>
                        <option v-for="d in districts" :key="d.id" :value="d.id">{{ d.name }}</option>
                    </select>
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Ward</label>
                    <select v-model="form.wardCode" required :disabled="!form.districtId">
                        <option value="">Select Ward</option>
                        <option v-for="w in wards" :key="w.id" :value="w.id">{{ w.name }}</option>
                    </select>
                </div>
                <div class="form-group">
                    <label>Specific Address</label>
                    <input v-model="form.specificAddress" required placeholder="123 Street Name" />
                </div>
            </div>

            <div class="form-group checkbox">
                <label>
                    <input type="checkbox" v-model="form.isDefault" /> Set as default address
                </label>
            </div>

            <div class="actions">
                <button type="button" @click="$emit('cancel')" class="btn-cancel">Cancel</button>
                <button type="submit" class="btn-save" :disabled="isLoading">
                    {{ isLoading ? 'Saving...' : 'Save Address' }}
                </button>
            </div>
        </form>
    </div>
</template>

<style scoped>
.address-form {
    background: #f9fafb;
    padding: 1.5rem;
    border-radius: 8px;
    border: 1px solid #e5e7eb;
}

h3 {
    margin-top: 0;
    margin-bottom: 1rem;
}

.form-group {
    margin-bottom: 1rem;
    display: flex;
    flex-direction: column;
}

.form-row {
    display: flex;
    gap: 1rem;
}

.form-row .form-group {
    flex: 1;
}

label {
    font-size: 0.9rem;
    color: #374151;
    margin-bottom: 0.3rem;
    font-weight: 500;
}

input,
select {
    padding: 0.6rem;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    font-size: 0.95rem;
}

.checkbox {
    flex-direction: row;
    align-items: center;
    gap: 0.5rem;
}

.actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
    margin-top: 1rem;
}

.btn-cancel {
    background: white;
    border: 1px solid #d1d5db;
    padding: 0.6rem 1.2rem;
    border-radius: 6px;
    cursor: pointer;
}

.btn-save {
    background: var(--color-primary);
    color: white;
    border: none;
    padding: 0.6rem 1.2rem;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
}

.btn-save:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}
</style>
