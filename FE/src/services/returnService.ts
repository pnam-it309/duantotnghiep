import apiClient from './api';

export interface ReturnRequestDTO {
    id: number;
    orderId: number;
    reason: string;
    status: string;
    refundAmount: number;
    createdAt: string;
}

const returnService = {
    getAllRequests() {
        return apiClient.get<ReturnRequestDTO[]>('/returns');
    },
    createRequest(orderId: number, reason: string) {
        return apiClient.post<ReturnRequestDTO>('/returns', { orderId, reason });
    },
    updateStatus(id: number, status: string) {
        return apiClient.put<ReturnRequestDTO>(`/returns/${id}/status?status=${status}`);
    }
};

export default returnService;
