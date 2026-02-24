<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import userService, { type UserDTO } from '../../services/userService';
import importService from '../../services/importService';
import Pagination from '../../components/Pagination.vue';
import ScanCCCDModal from '../../components/ScanCCCDModal.vue';

const users = ref<UserDTO[]>([]);
const isLoading = ref(false);
const showForm = ref(false);
const editingUser = ref<UserDTO | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const cccdModal = ref<any>(null);

// Pagination state
const currentPage = ref(0);
const totalPages = ref(0);
const totalElements = ref(0);
const pageSize = ref(5);

// Filters
const searchQuery = ref('');
const selectedRole = ref('all');

const formData = ref({ 
    username: '', 
    email: '', 
    password: '',
    fullName: '',
    phoneNumber: '',
    address: '',
    role: 'CUSTOMER'
});

const fetchUsers = async () => {
    isLoading.value = true;
    try {
        const params: any = {
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchQuery.value || undefined,
            role: selectedRole.value === 'all' ? undefined : selectedRole.value
        };
        const response = await userService.searchUsers(params);
        users.value = response.data.content;
        totalPages.value = response.data.totalPages;
        totalElements.value = response.data.totalElements;
    } catch (error) {
        console.error('Error fetching users:', error);
    } finally {
        isLoading.value = false;
    }
};

watch([searchQuery, selectedRole], () => {
    currentPage.value = 0;
    fetchUsers();
});

const handlePageChange = (page: number) => {
    currentPage.value = page;
    fetchUsers();
};

const handleCCCDScanned = (data: any) => {
    openForm();
    formData.value.fullName = data.fullName;
    formData.value.address = data.address;
    alert('Đã quét CCCD cho: ' + data.fullName);
};

const sendWelcomeSMS = async (user: UserDTO) => {
    try {
        await userService.sendSMS(user.id, `Xin chào ${user.fullName || user.username}, chào mừng bạn đến với cửa hàng!`);
        alert('Đã gửi tin nhắn SMS chào mừng đến ' + (user.phoneNumber || 'người dùng'));
    } catch (error) {
        alert('Không thể gửi SMS');
    }
};

const exportExcel = () => {
    window.open(`${import.meta.env.VITE_API_BASE_URL || '/api'}/export/users/excel`, '_blank');
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
            alert('Nhập Excel thành công!');
            await fetchUsers();
        } catch (error) {
            console.error('Import failed', error);
            alert('Nhập Excel thất bại');
        } finally {
            if (fileInput.value) fileInput.value.value = '';
        }
    }
};

const handleSubmit = async () => {
    if (!formData.value.username || !formData.value.email || !formData.value.fullName) {
        alert('Vui lòng điền đầy đủ Tên đăng nhập, Email và Họ tên.');
        return;
    }

    try {
        const payload: UserDTO = {
            username: formData.value.username,
            email: formData.value.email,
            fullName: formData.value.fullName,
            phoneNumber: formData.value.phoneNumber || undefined,
            address: formData.value.address,
            role: formData.value.role,
            password: formData.value.password || undefined
        } as any;

        if (editingUser.value) {
            // In real app, avoid sending password if not changed, or handle securely
            await userService.updateUser(editingUser.value.id, { ...editingUser.value, ...payload } as any);
        } else {
            await userService.createUser(payload as any);
        }
        await fetchUsers();
        closeForm();
    } catch (error: any) {
        console.error('Lỗi khi lưu người dùng:', error);
        let message = 'Đã xảy ra lỗi không xác định';
        if (error.response?.data) {
            if (typeof error.response.data === 'object') {
                message = Object.entries(error.response.data)
                    .map(([field, msg]) => `${field}: ${msg}`)
                    .join('\n');
            } else {
                message = error.response.data;
            }
        } else {
            message = error.message;
        }
        alert('Lỗi khi lưu người dùng:\n' + message);
    }
};

const handleDelete = async (id: number) => {
    if (!confirm('Bạn có chắc chắn muốn xóa người dùng này?')) return;

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
            password: '', // Don't fill password on edit
            fullName: user.fullName || '',
            phoneNumber: user.phoneNumber || '',
            address: user.address || '',
            role: user.role || 'CUSTOMER'
        };
    } else {
        editingUser.value = null;
        formData.value = {
            username: '',
            email: '',
            password: '',
            fullName: '',
            phoneNumber: '',
            address: '',
            role: 'CUSTOMER'
        };
    }
    showForm.value = true;
};

const closeForm = () => {
    showForm.value = false;
    editingUser.value = null;
    formData.value = { username: '', email: '', password: '', fullName: '', phoneNumber: '', address: '', role: 'CUSTOMER' };
};

onMounted(() => {
    fetchUsers();
});
</script>

<template>
    <div class="user-view">
        <div class="page-header">
            <div class="page-title">
                <h2>Người dùng</h2>
            </div>
            <button class="btn btn-primary btn-add" @click="openForm()">
                <i class="pi pi-plus"></i>
                <span>Thêm người dùng</span>
            </button>
        </div>

        <div class="action-bar card">
            <div class="filter-group">
                <div class="search-wrapper">
                    <i class="pi pi-search search-icon"></i>
                    <input type="text" v-model="searchQuery" placeholder="Tìm tên, email, số điện thoại..." class="search-input" />
                </div>

                <div class="select-group">
                  <div class="select-wrapper">
                    <select v-model="selectedRole" class="filter-select">
                      <option value="all">Tất cả vai trò</option>
                      <option value="CUSTOMER">Khách hàng</option>
                      <option value="EMPLOYEE">Nhân viên</option>
                      <option value="ADMIN">Quản trị viên</option>
                    </select>
                    <i class="pi pi-chevron-down select-icon"></i>
                  </div>
                </div>

                <button class="btn btn-outline" @click="cccdModal.open()">
                  <i class="pi pi-id-card"></i>
                  <span>Quét CCCD</span>
                </button>
            </div>

            <div class="button-group">
                <div class="import-export">
                    <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none" accept=".xlsx, .xls" />
                    <button class="btn btn-outline" @click="exportExcel()" title="Xuất Excel">
                        <i class="pi pi-file-export"></i>
                        <span>Xuất Excel</span>
                    </button>
                    <button class="btn btn-outline" @click="triggerFileInput()" title="Nhập Excel">
                        <i class="pi pi-file-import"></i>
                        <span>Nhập Excel</span>
                    </button>
                </div>
                <button class="btn btn-outline" @click="importService.downloadTemplate('users')" title="Tải mẫu">
                    <i class="pi pi-download"></i>
                    <span>Tải mẫu</span>
                </button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Đang tải...</div>

        <div v-else class="card table-container">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Thông tin người dùng</th>
                        <th>Vai trò</th>
                        <th>Hạng thành viên</th>
                        <th>Điểm thưởng</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="user in users" :key="user.id">
                        <td>{{ user.id }}</td>
                        <td>
                          <div><strong>{{ user.fullName || user.username }}</strong></div>
                          <div class="text-xs text-muted">{{ user.email }}</div>
                          <div class="text-xs text-muted">{{ user.phoneNumber }}</div>
                        </td>
                        <td>
                          <span class="badge" :class="user.role === 'ADMIN' ? 'badge-danger' : (user.role === 'EMPLOYEE' ? 'badge-warning' : 'badge-success')">
                            {{ user.role }}
                          </span>
                        </td>
                        <td>
                            <span class="tier-badge" :class="user.membershipTier?.toLowerCase() || 'silver'">
                                {{ user.membershipTier === 'SILVER' ? 'BẠC' : (user.membershipTier === 'GOLD' ? 'VÀNG' : (user.membershipTier === 'DIAMOND' ? 'KIM CƯƠNG' : 'BẠC')) }}
                            </span>
                        </td>
                        <td>{{ user.rewardPoints }} điểm</td>
                        <td>
                            <button class="btn-text" @click="openForm(user)">Sửa</button>
                            <button class="btn-text" @click="sendWelcomeSMS(user)">SMS</button>
                            <button class="btn-text text-danger" @click="handleDelete(user.id)">Xóa</button>
                        </td>
                    </tr>
                    <tr v-if="users.length === 0">
                        <td colspan="6" class="text-center">Không tìm thấy người dùng nào.</td>
                    </tr>
                </tbody>
            </table>

            <Pagination 
                :current-page="currentPage" 
                :total-pages="totalPages" 
                :total-elements="totalElements"
                :page-size="pageSize"
                @page-change="handlePageChange"
            />
        </div>

        <!-- Scan Modal -->
        <ScanCCCDModal ref="cccdModal" @scanned="handleCCCDScanned" />

        <!-- Modal -->
        <div v-if="showForm" class="modal-overlay">
            <div class="modal card">
                <h3>{{ editingUser ? 'Chỉnh sửa' : 'Thêm mới' }}</h3>
                <form @submit.prevent="handleSubmit" class="form">
                    <div class="form-row">
                      <div class="form-group">
                          <label for="username">Tên đăng nhập</label>
                          <input id="username" v-model="formData.username" type="text" required />
                      </div>
                      <div class="form-group">
                          <label for="fullName">Họ tên</label>
                          <input id="fullName" v-model="formData.fullName" type="text" required />
                      </div>
                    </div>
                    
                    <div class="form-row">
                      <div class="form-group">
                          <label for="email">Email</label>
                          <input id="email" v-model="formData.email" type="email" required />
                      </div>
                      <div class="form-group">
                          <label for="phoneNumber">Số điện thoại</label>
                          <input id="phoneNumber" v-model="formData.phoneNumber" type="text" />
                      </div>
                    </div>

                    <div class="form-group">
                        <label for="address">Địa chỉ</label>
                        <textarea id="address" v-model="formData.address" rows="2"></textarea>
                    </div>

                    <div class="form-row">
                      <div class="form-group">
                          <label for="role">Vai trò</label>
                          <select id="role" v-model="formData.role">
                            <option value="CUSTOMER">Khách hàng</option>
                            <option value="EMPLOYEE">Nhân viên</option>
                            <option value="ADMIN">Quản trị viên</option>
                          </select>
                      </div>
                      <div class="form-group">
                          <label for="password">Mật khẩu {{ editingUser ? '(Để trống nếu không đổi)' : '' }}</label>
                          <input id="password" v-model="formData.password" type="password" :required="!editingUser" />
                      </div>
                    </div>

                    <div class="form-actions">
                        <button type="button" class="btn" @click="closeForm">Hủy</button>
                        <button type="submit" class="btn btn-primary">Lưu</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<style scoped>
.loading {
    text-align: center;
    padding: 3rem;
    color: var(--color-text-muted);
}

.text-center {
    text-align: center;
}

.btn-text {
    background: none;
    border: none;
    color: var(--color-primary);
    font-weight: 500;
    padding: 0 0.5rem;
    font-size: 0.875rem;
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
    background-color: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(4px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal {
    width: 100%;
    max-width: 500px;
    animation: modal-in 0.3s ease-out;
}

@keyframes modal-in {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

.form {
    display: flex;
    flex-direction: column;
    gap: 1.25rem;
    margin-top: 1.5rem;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

label {
    font-size: 0.8125rem;
    font-weight: 600;
    color: var(--color-text-muted);
    text-transform: uppercase;
    letter-spacing: 0.025em;
}

input, select, textarea {
    padding: 0.625rem 0.875rem;
    border: 1px solid var(--border-color);
    border-radius: 8px;
    font-size: 0.9375rem;
    transition: all 0.2s;
}

input:focus, select:focus, textarea:focus {
    outline: none;
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(234, 179, 8, 0.1);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-top: 1rem;
}

.form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 1rem;
}

.text-xs {
  font-size: 0.75rem;
}

.text-muted {
  color: var(--color-text-muted);
}

.tier-badge {
    padding: 0.25rem 0.625rem;
    border-radius: 999px;
    font-size: 0.75rem;
    font-weight: bold;
    color: white;
}

.tier-badge.silver {
    background: #94a3b8;
}

.tier-badge.gold {
    background: #fbbf24;
    color: #451a03;
}

.tier-badge.diamond {
    background: #8b5cf6;
}

/* Table Enhancements */
.table-container {
    border: 1px solid var(--border-color);
    box-shadow: var(--shadow-sm);
}

table th {
    background-color: #f9fafb;
    padding: 1rem;
}

table td {
    padding: 1rem;
    font-size: 0.875rem;
    color: var(--color-text-main);
}
</style>
