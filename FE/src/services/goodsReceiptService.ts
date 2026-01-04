import api from './api';

export interface GoodsReceiptDetailDTO {
    id?: number;
    productVariantId: number;
    productVariantName?: string;
    sku?: string;
    quantity: number;
    importPrice: number;
}

export interface GoodsReceiptDTO {
    id?: number;
    supplierId: number;
    supplierName?: string;
    userId?: number;
    username?: string;
    importDate?: string;
    totalAmount?: number;
    notes?: string;
    details: GoodsReceiptDetailDTO[];
}

const goodsReceiptService = {
    getAllReceipts() {
        return api.get<GoodsReceiptDTO[]>('/goods-receipts');
    },

    createReceipt(data: GoodsReceiptDTO) {
        return api.post<GoodsReceiptDTO>('/goods-receipts', data);
    }
};

export default goodsReceiptService;
