import { useState, useMemo, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ThemeProvider, CssBaseline, CircularProgress, Box } from '@mui/material'
import { lightTheme, darkTheme } from '@/theme'
import { AuthProvider, useAuth } from '@/hooks/useAuth'
import { I18nProvider } from '@/hooks/useI18n'
import Layout from '@/components/Layout'
import Login from '@/pages/Login'
import Setup from '@/pages/Setup'
import Dashboard from '@/pages/Dashboard'
import Lists from '@/pages/Lists'
import Purchases from '@/pages/Purchases'
import Members from '@/pages/Members'
import Clients from '@/pages/Clients'
import Integrations from '@/pages/Integrations'
import Settings from '@/pages/Settings'
import api from '@/services/api'

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

function SetupGate() {
  const { user, loading } = useAuth()
  const [isSetup, setIsSetup] = useState<boolean | null>(null)

  useEffect(() => {
    if (user) {
      setIsSetup(true)
      return
    }
    api.get('/setup/status').then((res) => setIsSetup(res.data.isSetup)).catch(() => setIsSetup(true))
  }, [user])

  if (loading || isSetup === null) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    )
  }

  if (!isSetup) {
    return <Setup onDone={() => setIsSetup(true)} />
  }

  return <Login />
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
              <Route path="/admin/login" element={<SetupGate />} />
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
