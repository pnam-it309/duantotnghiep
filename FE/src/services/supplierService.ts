import api from './api';

export interface SupplierDTO {
    id: number;
    name: string;
    address?: string;
    phoneNumber?: string;
    email?: string;
    active: boolean;
}

const supplierService = {
    getAllSuppliers() {
        return api.get<SupplierDTO[]>('/suppliers');
    },

    getActiveSuppliers() {
        return api.get<SupplierDTO[]>('/suppliers/active');
    },

    createSupplier(data: Omit<SupplierDTO, 'id' | 'active'>) {
        return api.post<SupplierDTO>('/suppliers', data);
    },

    updateSupplier(id: number, data: Partial<SupplierDTO>) {
        return api.put<SupplierDTO>(`/suppliers/${id}`, data);
    },

    deleteSupplier(id: number) {
        return api.delete(`/suppliers/${id}`);
    }
};

export default supplierService;
