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
            label: 'Daily Revenue',
            backgroundColor: '#4f46e5',
            borderColor: '#4f46e5',
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
        ['Metric', 'Value'],
        ['Total Revenue', stats.value.totalRevenue],
        ['Total Orders', stats.value.totalOrders],
        ['Total Products', stats.value.totalProducts],
        ['Total Users', stats.value.totalUsers],
    ];
    const wb = XLSX.utils.book_new();
    const wsSummary = XLSX.utils.aoa_to_sheet(summaryData);
    XLSX.utils.book_append_sheet(wb, wsSummary, "Summary");

    // 2. Revenue Breakdown
    const revenueRows = [['Date', 'Revenue']];
    if (stats.value.revenueLast7Days) {
        Object.entries(stats.value.revenueLast7Days).forEach(([date, val]) => {
            revenueRows.push([date, val.toString()]);
        });
    }
    const wsRevenue = XLSX.utils.aoa_to_sheet(revenueRows);
    XLSX.utils.book_append_sheet(wb, wsRevenue, "Revenue Last 7 Days");

    // 3. Order Status
    const statusRows = [['Status', 'Count']];
    if (stats.value.orderStatusCounts) {
        Object.entries(stats.value.orderStatusCounts).forEach(([status, count]) => {
            statusRows.push([status, count.toString()]);
        });
    }
    const wsStatus = XLSX.utils.aoa_to_sheet(statusRows);
    XLSX.utils.book_append_sheet(wb, wsStatus, "Order Stats");

    // Save
    XLSX.writeFile(wb, `Dashboard_Report_${new Date().toISOString().slice(0, 10)}.xlsx`);
};

const exportToWord = () => {
    if (!stats.value) return;

    // Create a simple HTML document for Word
    const content = `
        <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
        <head><meta charset='utf-8'><title>Dashboard Report</title></head>
        <body>
            <h1>Dashboard Report</h1>
            <p>Date: ${new Date().toLocaleDateString()}</p>
            
            <h2>Summary</h2>
            <table border="1" style="border-collapse: collapse; width: 100%;">
                <tr><td>Total Revenue</td><td>$${stats.value.totalRevenue.toLocaleString()}</td></tr>
                <tr><td>Total Orders</td><td>${stats.value.totalOrders}</td></tr>
                <tr><td>Total Products</td><td>${stats.value.totalProducts}</td></tr>
                <tr><td>Total Customers</td><td>${stats.value.totalUsers}</td></tr>
            </table>

            <h2>Revenue (Last 7 Days)</h2>
            <ul>
                ${Object.entries(stats.value.revenueLast7Days || {}).map(([d, v]) => `<li>${d}: $${v.toLocaleString()}</li>`).join('')}
            </ul>

            <h2>Order Status Distribution</h2>
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
    link.download = `Dashboard_Report_${new Date().toISOString().slice(0, 10)}.doc`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
};

onMounted(loadStats);
</script>

<template>
    <div class="dashboard-view">
        <div class="header-actions">
            <h2>Dashboard</h2>
            <div class="export-buttons" v-if="stats">
                <button class="btn btn-outline" @click="exportToExcel">Export Excel / Sheets</button>
                <button class="btn btn-outline" @click="exportToWord">Export Word</button>
            </div>
        </div>

        <div v-if="isLoading" class="loading">Loading stats...</div>

        <div v-else-if="stats">
            <!-- Stats Cards -->
            <div class="stats-grid">
                <div class="stat-card">
                    <h3>Revenue</h3>
                    <div class="value">${{ stats.totalRevenue.toLocaleString() }}</div>
                </div>
                <div class="stat-card">
                    <h3>Orders</h3>
                    <div class="value">{{ stats.totalOrders }}</div>
                </div>
                <div class="stat-card">
                    <h3>Products</h3>
                    <div class="value">{{ stats.totalProducts }}</div>
                </div>
                <div class="stat-card">
                    <h3>Customers</h3>
                    <div class="value">{{ stats.totalUsers }}</div>
                </div>
                <div class="stat-card alert-card" :class="{ 'has-alert': stats.lowStockCount > 0 }">
                    <h3>Low Stock</h3>
                    <div class="value">{{ stats.lowStockCount }}</div>
                </div>
            </div>

            <!-- Charts Section -->
            <div class="charts-grid">
                <div class="chart-card card">
                    <h3>Revenue (Last 7 Days)</h3>
                    <div class="chart-container">
                        <Line :data="revenueData" :options="chartOptions" />
                    </div>
                </div>
                <div class="chart-card card">
                    <h3>Order Status</h3>
                    <div class="chart-container">
                        <Pie :data="statusData" :options="chartOptions" />
                    </div>
                </div>
            </div>
        </div>

        <div class="quick-links card">
            <h3>Quick Actions</h3>
            <div class="links">
                <router-link to="/admin/pos" class="btn btn-primary">New Sale (POS)</router-link>
                <router-link to="/admin/goods-receipts/new" class="btn btn-secondary">Import Goods</router-link>
                <router-link to="/admin/products" class="btn btn-outline">Manage Products</router-link>
            </div>
        </div>
    </div>
</template>

<style scoped>
.dashboard-view {
    padding: 1rem;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.stat-card {
    background-color: var(--color-surface);
    padding: 1.5rem;
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-color);
    box-shadow: var(--shadow-sm);
    text-align: center;
}

.alert-card.has-alert {
    background-color: #fef2f2;
    border-color: #ef4444;
}

.alert-card.has-alert .value {
    color: #ef4444;
}

.stat-card h3 {
    color: var(--color-text-muted);
    font-size: 0.9rem;
    margin-bottom: 0.5rem;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.stat-card .value {
    font-size: 2rem;
    font-weight: 700;
    color: var(--color-primary);
}

.quick-links {
    margin-top: 2rem;
    padding: 1.5rem;
}

.links {
    display: flex;
    gap: 1rem;
    margin-top: 1rem;
}

.btn {
    padding: 0.75rem 1.5rem;
    border-radius: var(--radius-md);
    text-decoration: none;
    font-weight: 500;
    display: inline-block;
}

.btn-primary {
    background-color: var(--color-primary);
    color: white;
}

.btn-secondary {
    background-color: var(--color-secondary);
    /* Usually green or similar */
    color: white;
}

.btn-outline {
    border: 1px solid var(--border-color);
    color: var(--color-text);
}

.card {
    background-color: var(--color-surface);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border-color);
}

.header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
}

.export-buttons {
    display: flex;
    gap: 1rem;
}

.charts-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.chart-card {
    padding: 1.5rem;
    height: 400px;
    display: flex;
    flex-direction: column;
}

.chart-container {
    flex: 1;
    position: relative;
    min-height: 0;
    /* Important for Chart.js responsiveness */
}

@media (max-width: 1000px) {
    .charts-grid {
        grid-template-columns: 1fr;
    }
}
</style>
