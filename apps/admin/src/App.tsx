import { useState, useMemo } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ThemeProvider, CssBaseline, CircularProgress, Box } from '@mui/material'
import { lightTheme, darkTheme } from '@/theme'
import { AuthProvider, useAuth } from '@/hooks/useAuth'
import { I18nProvider } from '@/hooks/useI18n'
import Layout from '@/components/Layout'
import Login from '@/pages/Login'
import Dashboard from '@/pages/Dashboard'
import Lists from '@/pages/Lists'
import Purchases from '@/pages/Purchases'
import Members from '@/pages/Members'
import Clients from '@/pages/Clients'
import Integrations from '@/pages/Integrations'
import Settings from '@/pages/Settings'

function ProtectedLayout({ mode, onToggleTheme }: { mode: 'light' | 'dark'; onToggleTheme: () => void }) {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    )
  }

  if (!user) {
    return <Navigate to="/admin/login" replace />
  }

  return <Layout mode={mode} onToggleTheme={onToggleTheme} />
}

export default function App() {
  const [mode, setMode] = useState<'light' | 'dark'>(() => {
    const saved = localStorage.getItem('listaih-admin-theme')
    return saved === 'dark' ? 'dark' : 'light'
  })

  const theme = useMemo(() => (mode === 'dark' ? darkTheme : lightTheme), [mode])

  const toggleTheme = () => {
    const next = mode === 'light' ? 'dark' : 'light'
    setMode(next)
    localStorage.setItem('listaih-admin-theme', next)
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <I18nProvider>
          <AuthProvider>
            <Routes>
              <Route path="/admin/login" element={<Login />} />
              <Route path="/admin" element={<ProtectedLayout mode={mode} onToggleTheme={toggleTheme} />}>
                <Route index element={<Dashboard />} />
                <Route path="lists" element={<Lists />} />
                <Route path="purchases" element={<Purchases />} />
                <Route path="members" element={<Members />} />
                <Route path="clients" element={<Clients />} />
                <Route path="integrations" element={<Integrations />} />
                <Route path="settings" element={<Settings />} />
              </Route>
              <Route path="*" element={<Navigate to="/admin" replace />} />
            </Routes>
          </AuthProvider>
        </I18nProvider>
      </BrowserRouter>
    </ThemeProvider>
  )
}
