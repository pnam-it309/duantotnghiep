<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
} from 'chart.js';
import { Line, Pie } from 'vue-chartjs';
import * as XLSX from 'xlsx';
import dashboardService, { type DashboardDTO } from '../../services/dashboardService';

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    BarElement,
    Title,
    Tooltip,
    Legend,
    ArcElement
);

const stats = ref<DashboardDTO | null>(null);
const isLoading = ref(false);

const revenueData = computed(() => {
    if (!stats.value?.revenueLast7Days) return { labels: [], datasets: [] };

    // Sort keys (dates)
    const sortedDates = Object.keys(stats.value.revenueLast7Days).sort();
    const dataPoints = sortedDates.map(date => stats.value?.revenueLast7Days[date] || 0);

    return {
        labels: sortedDates,
        datasets: [{
            label: 'Doanh thu theo ngày',
            backgroundColor: 'var(--color-primary)',
            borderColor: 'var(--color-primary)',
            data: dataPoints,
            tension: 0.3
        }]
    }
});

const statusData = computed(() => {
    if (!stats.value?.orderStatusCounts) return { labels: [], datasets: [] };

    const labels = Object.keys(stats.value.orderStatusCounts);
    const data = Object.values(stats.value.orderStatusCounts);
    const backgroundColors = [
        '#f59e0b', // Pending (yellow)
        '#10b981', // Delivered (green)
        '#ef4444', // Cancelled (red)
        '#3b82f6', // Shipping (blue)
        '#6366f1', // Confirmed (indigo)
    ];

    return {
        labels: labels,
        datasets: [{
            backgroundColor: backgroundColors.slice(0, labels.length),
            data: data
        }]
    }
});

const chartOptions = {
    responsive: true,
    maintainAspectRatio: false
};

const loadStats = async () => {
    isLoading.value = true;
    try {
        const res = await dashboardService.getStats();
        stats.value = res.data;
    } catch (e) {
        console.error("Error loading dashboard stats", e);
    } finally {
        isLoading.value = false;
    }
};

const exportToExcel = () => {
    if (!stats.value) return;

    // 1. Summary Sheet
    const summaryData = [
        ['Chỉ số', 'Giá trị'],
        ['Tổng doanh thu', stats.value.totalRevenue],
        ['Tổng đơn hàng', stats.value.totalOrders],
        ['Tổng sản phẩm', stats.value.totalProducts],
        ['Tổng người dùng', stats.value.totalUsers],
    ];
    const wb = XLSX.utils.book_new();
    const wsSummary = XLSX.utils.aoa_to_sheet(summaryData);
    XLSX.utils.book_append_sheet(wb, wsSummary, "Tổng quan");

    // 2. Revenue Breakdown
    const revenueRows = [['Ngày', 'Doanh thu']];
    if (stats.value.revenueLast7Days) {
        Object.entries(stats.value.revenueLast7Days).forEach(([date, val]) => {
            revenueRows.push([date, val.toString()]);
        });
    }
    const wsRevenue = XLSX.utils.aoa_to_sheet(revenueRows);
    XLSX.utils.book_append_sheet(wb, wsRevenue, "Doanh thu 7 ngày qua");

    // 3. Order Status
    const statusRows = [['Trạng thái', 'Số lượng']];
    if (stats.value.orderStatusCounts) {
        Object.entries(stats.value.orderStatusCounts).forEach(([status, count]) => {
            statusRows.push([status, count.toString()]);
        });
    }
    const wsStatus = XLSX.utils.aoa_to_sheet(statusRows);
    XLSX.utils.book_append_sheet(wb, wsStatus, "Trạng thái đơn hàng");

    // Save
    XLSX.writeFile(wb, `Bao_cao_Dashboard_${new Date().toISOString().slice(0, 10)}.xlsx`);
};

const exportToWord = () => {
    if (!stats.value) return;

    // Create a simple HTML document for Word
    const content = `
        <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
        <head><meta charset='utf-8'><title>Báo cáo Dashboard</title></head>
        <body>
            <h1>Báo cáo Dashboard</h1>
            <p>Ngày: ${new Date().toLocaleDateString()}</p>
            
            <h2>Tổng quan</h2>
            <table border="1" style="border-collapse: collapse; width: 100%;">
                <tr><td>Tổng doanh thu</td><td>${stats.value.totalRevenue.toLocaleString()} VNĐ</td></tr>
                <tr><td>Tổng đơn hàng</td><td>${stats.value.totalOrders}</td></tr>
                <tr><td>Tổng sản phẩm</td><td>${stats.value.totalProducts}</td></tr>
                <tr><td>Tổng khách hàng</td><td>${stats.value.totalUsers}</td></tr>
            </table>

            <h2>Doanh thu (7 ngày qua)</h2>
            <ul>
                ${Object.entries(stats.value.revenueLast7Days || {}).map(([d, v]) => `<li>${d}: ${v.toLocaleString()} VNĐ</li>`).join('')}
            </ul>

            <h2>Phân bố trạng thái đơn hàng</h2>
             <ul>
                ${Object.entries(stats.value.orderStatusCounts || {}).map(([s, c]) => `<li>${s}: ${c}</li>`).join('')}
            </ul>
        </body>
        </html>
    `;

    const blob = new Blob(['\ufeff', content], {
        type: 'application/msword'
    });

    // Trigger download
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `Bao_cao_Dashboard_${new Date().toISOString().slice(0, 10)}.doc`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
};

onMounted(loadStats);
</script>

<template>
    <div class="dashboard-view">
        <div class="page-header">
            <div class="page-title">
                <h2>Bảng điều khiển</h2>
                <p class="page-subtitle">Chào mừng bạn trở lại, đây là những gì đang diễn ra với cửa hàng của bạn hôm nay.</p>
            </div>
            <div class="export-buttons" v-if="stats">
                <button class="btn btn-outline" @click="exportToExcel">
                    <i class="pi pi-file-excel"></i>
                    <span>Xuất Excel</span>
                </button>
                <button class="btn btn-outline" @click="exportToWord">
                    <i class="pi pi-file-word"></i>
                    <span>Xuất Word</span>
                </button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">
            <div class="loader"></div>
            <span>Đang tải dữ liệu...</span>
        </div>

        <div v-else-if="stats" class="dashboard-content">
            <!-- Stats Cards -->
            <div class="stats-grid">
                <div class="stat-card revenue">
                    <div class="stat-icon">
                        <i class="pi pi-money-bill"></i>
                    </div>
                    <div class="stat-info">
                        <h3>Doanh thu</h3>
                        <div class="value">{{ stats.totalRevenue.toLocaleString() }}đ</div>
                    </div>
                </div>
                <div class="stat-card orders">
                    <div class="stat-icon">
                        <i class="pi pi-shopping-bag"></i>
                    </div>
                    <div class="stat-info">
                        <h3>Đơn hàng</h3>
                        <div class="value">{{ stats.totalOrders }}</div>
                    </div>
                </div>
                <div class="stat-card products">
                    <div class="stat-icon">
                        <i class="pi pi-box"></i>
                    </div>
                    <div class="stat-info">
                        <h3>Sản phẩm</h3>
                        <div class="value">{{ stats.totalProducts }}</div>
                    </div>
                </div>
                <div class="stat-card users">
                    <div class="stat-icon">
                        <i class="pi pi-users"></i>
                    </div>
                    <div class="stat-info">
                        <h3>Khách hàng</h3>
                        <div class="value">{{ stats.totalUsers }}</div>
                    </div>
                </div>
            </div>

            <div class="alert-banner" v-if="stats.lowStockCount > 0">
                <i class="pi pi-exclamation-triangle"></i>
                <span>Cảnh báo: Có <strong>{{ stats.lowStockCount }}</strong> sản phẩm sắp hết hàng. <router-link to="/admin/products">Kiểm tra ngay</router-link></span>
            </div>

            <!-- Charts Section -->
            <div class="charts-grid">
                <div class="chart-card card main-chart">
                    <div class="card-header">
                        <h3>Doanh thu (7 ngày qua)</h3>
                        <div class="card-actions">
                            <span class="badge badge-success">+12% so với tuần trước</span>
                        </div>
                    </div>
                    <div class="chart-container">
                        <Line :data="revenueData" :options="chartOptions" />
                    </div>
                </div>
                <div class="chart-card card pie-chart">
                    <div class="card-header">
                        <h3>Trạng thái đơn hàng</h3>
                    </div>
                    <div class="chart-container">
                        <Pie :data="statusData" :options="chartOptions" />
                    </div>
                </div>
            </div>

            <div class="quick-links-section">
                <h3>Thao tác nhanh</h3>
                <div class="links-grid">
                    <router-link to="/admin/pos" class="quick-link-card pos">
                        <i class="pi pi-desktop"></i>
                        <span>Bán hàng (POS)</span>
                    </router-link>
                    <router-link to="/admin/goods-receipts/new" class="quick-link-card import">
                        <i class="pi pi-plus-circle"></i>
                        <span>Nhập hàng mới</span>
                    </router-link>
                    <router-link to="/admin/products" class="quick-link-card products">
                        <i class="pi pi-list"></i>
                        <span>Quản lý sản phẩm</span>
                    </router-link>
                    <router-link to="/admin/orders" class="quick-link-card orders">
                        <i class="pi pi-shopping-cart"></i>
                        <span>Đơn hàng mới</span>
                    </router-link>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.dashboard-view {
    animation: fade-in 0.5s ease-out;
}

@keyframes fade-in {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.page-subtitle {
    color: var(--color-text-muted);
    font-size: 0.875rem;
    margin-top: 0.25rem;
}

.export-buttons {
    display: flex;
    gap: 0.75rem;
}

.loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 10rem 0;
    gap: 1.5rem;
    color: var(--color-text-muted);
}

.loader {
    width: 48px;
    height: 48px;
    border: 4px solid #f3f3f3;
    border-top: 4px solid var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* Stats Grid */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.stat-card {
    background: white;
    padding: 1.5rem;
    border-radius: 16px;
    border: 1px solid var(--border-color);
    box-shadow: var(--shadow-sm);
    display: flex;
    align-items: center;
    gap: 1.25rem;
    transition: all 0.3s ease;
}

.stat-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
}

.revenue .stat-icon { background: #eef2ff; color: #4f46e5; }
.orders .stat-icon { background: #ecfdf5; color: #059669; }
.products .stat-icon { background: #fff7ed; color: #d97706; }
.users .stat-icon { background: #fdf2f8; color: #db2777; }

.stat-info h3 {
    font-size: 0.8125rem;
    color: var(--color-text-muted);
    text-transform: uppercase;
    letter-spacing: 0.05em;
    margin-bottom: 0.25rem;
}

.stat-info .value {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--color-text-main);
}

/* Alert Banner */
.alert-banner {
    background: #fffbeb;
    border: 1px solid #fef3c7;
    border-radius: 12px;
    padding: 1rem 1.5rem;
    margin-bottom: 2rem;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    color: #92400e;
    font-size: 0.9375rem;
}

.alert-banner i {
    font-size: 1.25rem;
}

.alert-banner strong {
    font-weight: 700;
}

.alert-banner a {
    color: #b45309;
    font-weight: 600;
    text-decoration: underline;
}

/* Charts Grid */
.charts-grid {
    display: grid;
    grid-template-columns: 1.6fr 1fr;
    gap: 1.5rem;
    margin-bottom: 2.5rem;
}

.chart-card {
    padding: 1.5rem;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
}

.card-header h3 {
    font-size: 1.125rem;
    font-weight: 700;
}

.chart-container {
    height: 350px;
    position: relative;
}

/* Quick Links */
.quick-links-section h3 {
    font-size: 1.125rem;
    margin-bottom: 1.25rem;
    font-weight: 700;
}

.links-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1.25rem;
}

.quick-link-card {
    background: white;
    padding: 1.5rem;
    border-radius: 16px;
    border: 1px solid var(--border-color);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
    text-decoration: none;
    color: var(--color-text-main);
    font-weight: 600;
    transition: all 0.2s;
}

.quick-link-card i {
    font-size: 1.5rem;
    color: var(--color-primary);
}

.quick-link-card:hover {
    background: #fff;
    border-color: var(--color-primary);
    box-shadow: 0 4px 6px -1px rgba(220, 38, 38, 0.1);
    transform: translateY(-2px);
}

@media (max-width: 1200px) {
    .stats-grid { grid-template-columns: repeat(2, 1fr); }
    .links-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 900px) {
    .charts-grid { grid-template-columns: 1fr; }
    .stats-grid { grid-template-columns: 1fr; }
}
</style>
