import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('../layouts/PublicLayout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('../views/public/HomeView.vue')
        },
        {
          path: 'products',
          name: 'shop-products',
          component: () => import('../views/public/ProductCatalogView.vue')
        },
        {
          path: 'products/:id',
          name: 'shop-product-detail',
          component: () => import('../views/public/ProductDetailView.vue')
        },
        {
          path: 'cart',
          name: 'shop-cart',
          component: () => import('../views/public/CartView.vue')
        },
        {
            path: 'login',
            name: 'shop-login',
            component: () => import('../views/public/LoginView.vue')
        }
      ]
    },
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('../views/admin/DashboardView.vue')
        },
        {
          path: 'brands',
          name: 'admin-brands',
          component: () => import('../views/admin/BrandView.vue')
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('../views/admin/CategoryView.vue')
        },
        {
          path: 'colors',
          name: 'admin-colors',
          component: () => import('../views/admin/ColorView.vue')
        },
        {
          path: 'sizes',
          name: 'admin-sizes',
          component: () => import('../views/admin/SizeView.vue')
        },
        {
          path: 'coupons',
          name: 'admin-coupons',
          component: () => import('../views/admin/CouponView.vue')
        },
        {
          path: 'discounts',
          name: 'admin-discounts',
          component: () => import('../views/admin/DiscountView.vue')
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/admin/UserView.vue')
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('../views/admin/OrderView.vue')
        },
        {
          path: 'orders/:id',
          name: 'admin-order-detail',
          component: () => import('../views/admin/OrderDetailView.vue')
        },
        {
          path: 'returns',
          name: 'admin-returns',
          component: () => import('../views/admin/ReturnRequestsView.vue')
        },
        {
          path: 'pos',
          name: 'admin-pos',
          component: () => import('../views/admin/POSView.vue')
        },
        {
          path: 'products',
          name: 'admin-products',
          component: () => import('../views/admin/ProductView.vue')
        },
        {
          path: 'products/:productId/variants',
          name: 'admin-product-variants',
          component: () => import('../views/admin/ProductVariantView.vue')
        },
        {
          path: 'suppliers',
          name: 'admin-suppliers',
          component: () => import('../views/admin/SupplierView.vue')
        },
        {
          path: 'goods-receipts',
          name: 'admin-goods-receipts',
          component: () => import('../views/admin/GoodsReceiptView.vue')
        },
        {
          path: 'goods-receipts/new',
          name: 'admin-goods-receipts-form',
          component: () => import('../views/admin/GoodsReceiptForm.vue')
        }
      ]
    }
  ],
})

export default router
