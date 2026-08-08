import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import api from '@/services/api'

interface User {
  id: string
  email: string
  name: string
  avatar?: string | null
}

interface AuthContextValue {
  user: User | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  updateUser: (data: Partial<User>) => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('listaih_access_token')
    if (token) {
      api
        .get('/users/me')
        .then((res) => setUser(res.data))
        .catch(() => {
          localStorage.removeItem('listaih_access_token')
          localStorage.removeItem('listaih_refresh_token')
        })
        .finally(() => setLoading(false))
    } else {
      setLoading(false)
    }
  }, [])

  const login = async (email: string, password: string) => {
    const res = await api.post('/auth/login', { email, password })
    localStorage.setItem('listaih_access_token', res.data.accessToken)
    localStorage.setItem('listaih_refresh_token', res.data.refreshToken)
    setUser(res.data.user)
  }

  const logout = () => {
    const refreshToken = localStorage.getItem('listaih_refresh_token')
    if (refreshToken) {
      api.post('/auth/logout', { refreshToken }).catch(() => {})
    }
    localStorage.removeItem('listaih_access_token')
    localStorage.removeItem('listaih_refresh_token')
    setUser(null)
  }

  const updateUser = (data: Partial<User>) => {
    setUser((prev) => prev ? { ...prev, ...data } : prev)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
