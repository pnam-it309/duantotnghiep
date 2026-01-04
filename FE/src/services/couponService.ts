import apiClient from './api';

export interface CouponDTO {
  id: number;
  code: string;
  discountAmount: number;
  minOrderValue: number;
  expiryDate: string; // ISO DateTime string
}

export default {
  getAllCoupons() {
    return apiClient.get<CouponDTO[]>('/coupons');
  },
  getCouponById(id: number) {
    return apiClient.get<CouponDTO>(`/coupons/${id}`);
  },
  getCouponByCode(code: string) {
    return apiClient.get<CouponDTO>(`/coupons/code/${code}`);
  },
  createCoupon(coupon: Omit<CouponDTO, 'id'>) {
    return apiClient.post<CouponDTO>('/coupons', coupon);
  },
  updateCoupon(id: number, coupon: CouponDTO) {
    return apiClient.put<CouponDTO>(`/coupons/${id}`, coupon);
  },
  deleteCoupon(id: number) {
    return apiClient.delete(`/coupons/${id}`);
  },
};
