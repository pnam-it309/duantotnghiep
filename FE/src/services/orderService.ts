import apiClient from './api';

export interface OrderItemDTO {
  id?: number;
  productVariantId?: number; // Simplified for requests
  productName?: string; // For display
  variantSku?: string; // For display
  quantity: number;
  price: number;
}

export interface OrderDTO {
  id: number;
  userId: number;
  username?: string;
  couponId?: number;
  couponCode?: string;
  subtotal: number;
  discountTotal: number;
  finalTotal: number;
  status: string;
  items?: OrderItemDTO[];
  createdAt?: string;
  carrierName?: string;
  trackingCode?: string;
  pointsUsed?: number;
  pointsDiscount?: number;
}

export interface OrderStatusHistoryDTO {
    id: number;
    status: string;
    changedAt: string;
    note: string;
}

export default {
  getAllOrders() {
    return apiClient.get<OrderDTO[]>('/orders');
  },
  getOrdersByUser(userId: number) {
    return apiClient.get<OrderDTO[]>(`/orders/user/${userId}`);
  },
  getOrderById(id: number) {
    return apiClient.get<OrderDTO>(`/orders/${id}`);
  },
  getOrderItems(id: number) {
    return apiClient.get<OrderItemDTO[]>(`/orders/${id}/items`);
  },
  createOrder(order: any) { 
    return apiClient.post<OrderDTO>('/orders', order);
  },
  updateOrder(id: number, order: OrderDTO) {
    return apiClient.put<OrderDTO>(`/orders/${id}`, order);
  },
  deleteOrder(id: number) {
    return apiClient.delete(`/orders/${id}`);
  },
  // New Methods
  getOrderHistory(id: number) {
      return apiClient.get<OrderStatusHistoryDTO[]>(`/orders/${id}/history`);
  },
  shipOrder(id: number, carrierId: string) {
      return apiClient.post<OrderDTO>(`/orders/${id}/ship?carrierId=${carrierId}`);
  }
};
