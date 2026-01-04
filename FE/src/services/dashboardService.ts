import api from './api';

export interface DashboardDTO {
    totalOrders: number;
    totalProducts: number;
    totalUsers: number;
    totalRevenue: number;
    lowStockCount: number;
    revenueLast7Days: Record<string, number>;
    orderStatusCounts: Record<string, number>;
}

const dashboardService = {
    getStats() {
        return api.get<DashboardDTO>('/dashboard/stats');
    }
};

export default dashboardService;
