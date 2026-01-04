import apiClient from './api';

export default {
    importBrands(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.post('/import/brands', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    importCategories(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.post('/import/categories', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    importColors(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.post('/import/colors', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    importSizes(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.post('/import/sizes', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    importUsers(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.post('/import/users', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    importProducts(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.post('/import/products', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    importProductVariants(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.post('/import/product-variants', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    importCoupons(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.post('/import/coupons', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    importDiscounts(file: File) {
        const formData = new FormData();
        formData.append('file', file);
        return apiClient.post('/import/discounts', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    },
    async downloadTemplate(entityType: string) {
        try {
            const response = await apiClient.get(`/templates/${entityType}`, {
                responseType: 'blob'
            });
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `${entityType}_template.xlsx`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error(`Error downloading template for ${entityType}`, error);
            alert('Failed to download template.');
        }
    }
};
