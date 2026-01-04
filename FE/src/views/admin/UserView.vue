<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import userService, { type UserDTO } from '../../services/userService';
import importService from '../../services/importService';

const users = ref<UserDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingUser = ref<UserDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const searchQuery = ref('');

const filteredUsers = computed(() => {
    if (!searchQuery.value) return users.value;
    const lowerQuery = searchQuery.value.toLowerCase();
    return users.value.filter(user =>
        user.username.toLowerCase().includes(lowerQuery) ||
        user.email.toLowerCase().includes(lowerQuery) ||
        user.id.toString().includes(lowerQuery)
    );
});

const formData = ref({ username: '', email: '', password: '' });

const fetchUsers = async () => {
    isLoading.value = true;
    try {
        const response = await userService.getAllUsers();
        users.value = response.data;
    } catch (error) {
        console.error('Error fetching users:', error);
    } finally {
        isLoading.value = false;
    }
};

const triggerFileInput = () => {
    fileInput.value?.click();
};

const handleFileUpload = async (event: Event) => {
    const target = event.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
        const file = target.files[0];
        try {
            await importService.importUsers(file!);
            alert('Import successful!');
            await fetchUsers();
        } catch (error) {
            console.error('Import failed', error);
            alert('Import failed');
        } finally {
            if (fileInput.value) fileInput.value.value = '';
        }
    }
};

const handleSubmit = async () => {
    if (!formData.value.username || !formData.value.email) return;

    try {
        const payload: Partial<UserDTO> & { password?: string } = {
            username: formData.value.username,
            email: formData.value.email
        };
        if (formData.value.password) {
            payload.password = formData.value.password;
        }

        if (editingUser.value) {
            // In real app, avoid sending password if not changed, or handle securely
            await userService.updateUser(editingUser.value.id, { ...editingUser.value, ...payload });
        } else {
            await userService.createUser(payload as any);
        }
        await fetchUsers();
        closeForm();
    } catch (error) {
        console.error('Error saving user:', error);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this user?')) return;

    try {
        await userService.deleteUser(id);
        await fetchUsers();
    } catch (error) {
        console.error('Error deleting user:', error);
    }
};

const openForm = (user?: UserDTO) => {
    if (user) {
        editingUser.value = user;
        formData.value = {
            username: user.username,
            email: user.email,
            password: '' // Don't fill password on edit
        };
    } else {
        editingUser.value = null;
        formData.value = {
            username: '',
            email: '',
            password: ''
        };
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingUser.value = null;
    formData.value = { username: '', email: '', password: '' };
};

onMounted(() => {
    fetchUsers();
});
</script>

<template>
    <div class="user-view">
        <div class="header">
            <h2>Users</h2>
            <div class="actions">
                <input type="text" v-model="searchQuery" placeholder="Search user..." class="search-input" />
                <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none"
                    accept=".xlsx, .xls" />
                <button class="btn btn-secondary" @click="importService.downloadTemplate('users')"
                    style="margin-right: 0.5rem">Download Template</button>
                <button class="btn btn-secondary" @click="triggerFileInput" style="margin-right: 0.5rem">Import
                    Excel</button>
                <button class="btn btn-primary" @click="openForm()">Add User</button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Loading...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Tier</th>
                        <th>Points</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="user in filteredUsers" :key="user.id">
                        <td>{{ user.id }}</td>
                        <td>{{ user.username }}</td>
                        <td>{{ user.email }}</td>
                        <td>
                            <span class="tier-badge" :class="user.membershipTier?.toLowerCase() || 'silver'">
                                {{ user.membershipTier || 'SILVER' }}
                            </span>
                        </td>
                        <td>{{ user.rewardPoints }} pts</td>
                        <td>
                            <button class="btn-text" @click="openForm(user)">Edit</button>
                            <button class="btn-text text-danger" @click="handleDelete(user.id)">Delete</button>
                        </td>
                    </tr>
                    <tr v-if="filteredUsers.length === 0">
                        <td colspan="4" class="text-center">No users found.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingUser ? 'Edit User' : 'Add User' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-group">
                        <label for="username">Username</label>
                        <input id="username" v-model="formData.username" type="text" required />
                    </div>
                    <div class="form-group">
                        <label for="email">Email</label>
                        <input id="email" v-model="formData.email" type="email" required />
                    </div>
                    <div class="form-group">
                        <label for="password">Password {{ editingUser ? '(Leave blank to keep current)' : '' }}</label>
                        <input id="password" v-model="formData.password" type="password" :required="!editingUser" />
                    </div>
                    <div class="form-actions">
                        <button type="button" class="btn" @click="closeForm">Cancel</button>
                        <button type="submit" class="btn btn-primary">Save</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<style scoped>
.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    flex-wrap: wrap;
    gap: 1rem;
}

.actions {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.search-input {
    padding: 0.5rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-right: 0.5rem;
    min-width: 200px;
}

.loading {
    text-align: center;
    padding: 2rem;
    color: var(--color-text-muted);
}

.text-center {
    text-align: center;
}

.btn-text {
    background: none;
    border: none;
    color: var(--color-primary);
    padding: 0 0.5rem;
}

.btn-text:hover {
    text-decoration: underline;
}

.text-danger {
    color: var(--color-danger);
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal {
    width: 100%;
    max-width: 400px;
}

.form {
    margin-top: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

input {
    padding: 0.625rem;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1rem;
}

.tier-badge {
    padding: 0.25rem 0.5rem;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: bold;
    color: white;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.tier-badge.silver {
    background: #6c757d;
}

.tier-badge.gold {
    background: #ffc107;
    color: black;
    text-shadow: none;
}

.tier-badge.diamond {
    background: #6f42c1;
}
</style>
