import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService, { type LoginPayload, type RegisterPayload } from '../services/authService'
import router from '../router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<any | null>(JSON.parse(localStorage.getItem('user') || 'null'))
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(payload: LoginPayload) {
    error.value = null
    try {
      const response = await authService.login(payload)
      token.value = response.accessToken
      user.value = response.user

      localStorage.setItem('token', token.value!)
      localStorage.setItem('user', JSON.stringify(user.value))

      // Redirect based on role
      if (user.value.role === 'ADMIN') {
        router.push('/admin/dashboard')
      } else {
        router.push('/')
      }
    } catch (err: any) {
      console.error('Login failed', err)
      error.value = err.response?.data?.message || 'Login failed. Please check your credentials.'
      throw err
    }
  }

  async function register(payload: RegisterPayload) {
    error.value = null
    try {
      await authService.register(payload)
      // Auto login or redirect to login? Let's redirect to login for now
      router.push('/login')
    } catch (err: any) {
      console.error('Registration failed', err)
      error.value = err.response?.data?.message || 'Registration failed.'
      throw err
    }
  }

  function logout() {
    token.value = null
    user.value = null
    authService.logout()
    router.push('/login')
  }

  return {
    token,
    user,
    error,
    isAuthenticated,
    isAdmin,
    login,
    register,
    logout,
  }
})
