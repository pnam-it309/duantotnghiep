import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import { useAuthStore } from '../stores/authStore'

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
          component: () => import('../views/public/HomeView.vue'),
        },
        {
          path: 'products',
          name: 'shop-products',
          component: () => import('../views/public/ProductCatalogView.vue'),
        },
        {
          path: 'products/:id',
          name: 'shop-product-detail',
          component: () => import('../views/public/ProductDetailView.vue'),
        },
        {
          path: 'cart',
          name: 'shop-cart',
          component: () => import('../views/public/CartView.vue'),
        },
        {
          path: 'login',
          name: 'shop-login',
          component: () => import('../views/public/LoginView.vue'),
        },
        {
          path: 'register',
          name: 'shop-register',
          component: () => import('../views/public/RegisterView.vue'),
        },
        {
          path: 'checkout',
          name: 'shop-checkout',
          component: () => import('../views/public/CheckoutView.vue'),
        },
        {
          path: 'profile',
          name: 'shop-profile',
          component: () => import('../views/public/UserProfileView.vue'),
        },
        {
          path: 'orders',
          name: 'shop-orders',
          component: () => import('../views/public/OrderHistoryView.vue'),
        },
      ],
    },
    {
      path: '/payment-result',
      name: 'payment-result',
      component: () => import('../views/public/PaymentResultView.vue'),
      meta: { layout: 'public' },
    },
    {
      path: '/compare',
      name: 'compare',
      component: () => import('../views/public/ComparisonView.vue'),
    },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, role: 'ADMIN' },
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('../views/admin/DashboardView.vue'),
        },
        {
          path: 'brands',
          name: 'admin-brands',
          component: () => import('../views/admin/BrandView.vue'),
        },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('../views/admin/CategoryView.vue'),
        },
        {
          path: 'colors',
          name: 'admin-colors',
          component: () => import('../views/admin/ColorView.vue'),
        },
        {
          path: 'sizes',
          name: 'admin-sizes',
          component: () => import('../views/admin/SizeView.vue'),
        },
        {
          path: 'coupons',
          name: 'admin-coupons',
          component: () => import('../views/admin/CouponView.vue'),
        },
        {
          path: 'discounts',
          name: 'admin-discounts',
          component: () => import('../views/admin/DiscountView.vue'),
        },
        {
          path: 'users',
          name: 'admin-users',
          component: () => import('../views/admin/UserView.vue'),
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('../views/admin/OrderView.vue'),
        },
        {
          path: 'orders/:id',
          name: 'admin-order-detail',
          component: () => import('../views/admin/OrderDetailView.vue'),
        },
        {
          path: 'returns',
          name: 'admin-returns',
          component: () => import('../views/admin/ReturnRequestsView.vue'),
        },
        {
          path: 'pos',
          name: 'admin-pos',
          component: () => import('../views/admin/POSView.vue'),
        },
        {
          path: 'products',
          name: 'admin-products',
          component: () => import('../views/admin/ProductView.vue'),
        },
        {
          path: 'products/:productId/variants',
          name: 'admin-product-variants',
          component: () => import('../views/admin/ProductVariantView.vue'),
        },
        {
          path: 'suppliers',
          name: 'admin-suppliers',
          component: () => import('../views/admin/SupplierView.vue'),
        },
        {
          path: 'goods-receipts',
          name: 'admin-goods-receipts',
          component: () => import('../views/admin/GoodsReceiptView.vue'),
        },
        {
          path: 'goods-receipts/new',
          name: 'admin-goods-receipts-form',
          component: () => import('../views/admin/GoodsReceiptForm.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.meta.role && authStore.user?.role !== to.meta.role) {
    // If user is logged in but doesn't have the role
    next('/')
  } else {
    next()
  }
})

export default router
