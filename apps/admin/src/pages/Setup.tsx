import { useState } from 'react'
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material'
import { RocketLaunch } from '@mui/icons-material'
import { useI18n } from '@/hooks/useI18n'
import api from '@/services/api'

export default function Setup({ onDone }: { onDone: () => void }) {
  const { t } = useI18n()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [householdName, setHouseholdName] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const passwordsMatch = password === confirmPassword
  const passwordTooShort = password.length > 0 && password.length < 6
  const confirmError = confirmPassword.length > 0 && !passwordsMatch

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!passwordsMatch) {
      setError(t('setup.mismatch'))
      return
    }
    if (password.length < 6) {
      setError(t('setup.tooShort'))
      return
    }
    setError('')
    setLoading(true)
    try {
      await api.post('/setup', {
        name,
        email,
        password,
        householdName,
      })
      onDone()
    } catch (err: any) {
      setError(err.response?.data?.message || t('setup.error'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 3, bgcolor: 'background.default' }}>
      <Card sx={{ width: '100%', maxWidth: 480, p: 2 }}>
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 4 }}>
            <Box sx={{ width: 64, height: 64, mb: 1.5 }}>
              <img src="./logo.svg" alt="Listaih" width="64" height="64" />
            </Box>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>{t('setup.title')}</Typography>
            <Typography variant="body2" color="text.secondary">{t('setup.subtitle')}</Typography>
          </Box>

          <Typography variant="h6" sx={{ mb: 3 }}>{t('setup.heading')}</Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label={t('setup.name')}
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              fullWidth
            />
            <TextField
              label={t('setup.email')}
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="seu@email.com"
              required
              fullWidth
            />
            <TextField
              label={t('setup.password')}
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
              fullWidth
              error={passwordTooShort}
              helperText={passwordTooShort ? t('setup.tooShort') : ''}
            />
            <TextField
              label={t('setup.confirmPassword')}
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="••••••••"
              required
              fullWidth
              error={confirmError}
              helperText={confirmError ? t('setup.mismatch') : ''}
            />
            <TextField
              label={t('setup.household')}
              value={householdName}
              onChange={(e) => setHouseholdName(e.target.value)}
              required
              fullWidth
            />
            <Button type="submit" variant="contained" size="large" disabled={loading || !passwordsMatch || passwordTooShort} startIcon={loading ? undefined : <RocketLaunch />} sx={{ mt: 1 }}>
              {loading ? <CircularProgress size={24} color="inherit" /> : t('setup.submit')}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  )
}