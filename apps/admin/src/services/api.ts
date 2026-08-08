import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('listaih_access_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/')) {
      const refreshToken = localStorage.getItem('listaih_refresh_token')
      if (refreshToken) {
        try {
          const res = await axios.post('/api/auth/refresh', { refreshToken })
          localStorage.setItem('listaih_access_token', res.data.accessToken)
          error.config.headers.Authorization = `Bearer ${res.data.accessToken}`
          return api(error.config)
        } catch {
          localStorage.removeItem('listaih_access_token')
          localStorage.removeItem('listaih_refresh_token')
          window.location.href = '/admin/login'
        }
      } else {
        window.location.href = '/admin/login'
      }
    }
    return Promise.reject(error)
  },
)

export default api
