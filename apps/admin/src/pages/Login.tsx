import { useState } from 'react'
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  InputAdornment,
  IconButton,
  Divider,
} from '@mui/material'
import { Visibility, VisibilityOff, Login as LoginIcon, Shield } from '@mui/icons-material'
import { useAuth } from '@/hooks/useAuth'
import { useI18n } from '@/hooks/useI18n'

export default function Login() {
  const { login } = useAuth()
  const { t } = useI18n()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(email, password)
      window.location.href = '/admin/'
    } catch (err: any) {
      setError(err.response?.data?.message || t('login.error'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 3, bgcolor: 'background.default' }}>
      <Card sx={{ width: '100%', maxWidth: 420, p: 2 }}>
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 4 }}>
            <Box sx={{ width: 64, height: 64, mb: 1.5 }}>
              <img src="./logo.svg" alt="Listaih" width="64" height="64" />
            </Box>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>{t('login.title')}</Typography>
            <Typography variant="body2" color="text.secondary">{t('login.subtitle')}</Typography>
          </Box>

          <Typography variant="h6" sx={{ mb: 3 }}>{t('login.heading')}</Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label={t('login.email')}
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="seu@email.com"
              required
              fullWidth
            />
            <TextField
              label={t('login.password')}
              type={showPassword ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
              fullWidth
              slotProps={{
                input: {
                  endAdornment: (
                    <InputAdornment position="end">
                      <IconButton onClick={() => setShowPassword(!showPassword)} edge="end">
                        {showPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  ),
                },
              }}
            />
            <Button type="submit" variant="contained" size="large" disabled={loading} startIcon={<LoginIcon />} sx={{ mt: 1 }}>
              {loading ? t('login.loading') : t('login.submit')}
            </Button>
          </Box>

          <Divider sx={{ my: 2 }} />
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 0.5 }}>
            <Shield sx={{ fontSize: 14, color: 'text.secondary' }} />
            <Typography variant="body2" color="text.secondary">{t('login.selfhosted')}</Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  )
}
