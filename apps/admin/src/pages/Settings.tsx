import { useState, useEffect } from 'react'
import {
  Box, Card, Typography, TextField, Button, Switch, FormControlLabel,
  Divider, Alert, AlertTitle, CircularProgress, Snackbar, Dialog,
  DialogTitle, DialogContent, DialogActions, DialogContentText, MenuItem,
} from '@mui/material'
import { Save, Lock, Refresh, Download, Backup, Warning, DeleteSweep, RestartAlt, Dangerous } from '@mui/icons-material'
import api from '@/services/api'
import { useAuth } from '@/hooks/useAuth'
import { useHousehold } from '@/hooks/useHousehold'
import { useSystemConfig } from '@/hooks/useSystemConfig'
import { useI18n, type Lang } from '@/hooks/useI18n'

const CURRENCIES = [
  { code: 'BRL', label: 'Real Brasileiro (R$)' },
  { code: 'USD', label: 'US Dollar ($)' },
  { code: 'EUR', label: 'Euro (\u20AC)' },
  { code: 'GBP', label: 'Pound Sterling (\u00A3)' },
  { code: 'JPY', label: 'Japanese Yen (\u00A5)' },
  { code: 'ARS', label: 'Peso Argentino ($)' },
  { code: 'MXN', label: 'Peso Mexicano ($)' },
  { code: 'COP', label: 'Peso Colombiano ($)' },
  { code: 'CLP', label: 'Peso Chileno ($)' },
  { code: 'PEN', label: 'Sol Peruano (S/)' },
  { code: 'UYU', label: 'Peso Uruguayo ($U)' },
  { code: 'PYG', label: 'Guarani (\u20B2)' },
  { code: 'BOB', label: 'Boliviano (Bs)' },
  { code: 'VES', label: 'Bolivar (Bs)' },
  { code: 'INR', label: 'Indian Rupee (\u20B9)' },
  { code: 'CNY', label: 'Chinese Yuan (\u00A5)' },
  { code: 'AUD', label: 'Australian Dollar ($)' },
  { code: 'CAD', label: 'Canadian Dollar ($)' },
  { code: 'CHF', label: 'Swiss Franc (CHF)' },
]

const LANGUAGES: { value: Lang; label: string }[] = [
  { value: 'pt-BR', label: 'Português (BR)' },
  { value: 'en-US', label: 'English (US)' },
  { value: 'es-ES', label: 'Español (ES)' },
]

export default function Settings() {
  const { user, logout } = useAuth()
  const { activeHousehold, reload: reloadHousehold } = useHousehold()
  const { t, lang, setLang } = useI18n()

  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [savingProfile, setSavingProfile] = useState(false)
  const [snack, setSnack] = useState<string | null>(null)

  const [pwOpen, setPwOpen] = useState(false)
  const [currentPw, setCurrentPw] = useState('')
  const [newPw, setNewPw] = useState('')
  const [confirmPw, setConfirmPw] = useState('')
  const [pwError, setPwError] = useState('')
  const [savingPw, setSavingPw] = useState(false)

  const [regenerating, setRegenerating] = useState(false)

  const { config, updateConfig } = useSystemConfig()
  const [currency, setCurrency] = useState('BRL')
  const [savingCurrency, setSavingCurrency] = useState(false)

  useEffect(() => {
    if (user) {
      setName(user.name)
      setEmail(user.email)
    }
  }, [user])

  useEffect(() => {
    if (config) setCurrency(config.currency)
  }, [config])

  const saveProfile = async () => {
    setSavingProfile(true)
    try {
      await api.put('/users/me', { name })
      setSnack(t('settings.profileSaved'))
    } catch {
      setSnack(t('settings.profileError'))
    } finally {
      setSavingProfile(false)
    }
  }

  const changePassword = async () => {
    setPwError('')
    if (newPw.length < 6) { setPwError(t('settings.pwTooShort')); return }
    if (newPw !== confirmPw) { setPwError(t('settings.pwMismatch')); return }
    setSavingPw(true)
    try {
      await api.post('/auth/change-password', { currentPassword: currentPw, newPassword: newPw })
      setPwOpen(false)
      setCurrentPw(''); setNewPw(''); setConfirmPw('')
      setSnack(t('settings.pwSaved'))
    } catch {
      setPwError(t('settings.pwIncorrect'))
    } finally {
      setSavingPw(false)
    }
  }

  const saveCurrency = async () => {
    setSavingCurrency(true)
    try {
      await updateConfig({ currency })
      setSnack(t('settings.currencySaved'))
    } catch {
      setSnack(t('settings.currencyError'))
    } finally {
      setSavingCurrency(false)
    }
  }

  const regenerateInviteCode = async () => {
    setRegenerating(true)
    try {
      await api.patch(`/users/households/${activeHousehold?.id}/regenerate-code`)
      await reloadHousehold()
      setSnack(t('settings.codeRegenerated'))
    } catch {
      setSnack(t('settings.codeError'))
    } finally {
      setRegenerating(false)
    }
  }

  return (
    <Box sx={{ maxWidth: 800 }}>
      <Typography variant="h4" sx={{ mb: 0.5 }}>{t('settings.title')}</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>{t('settings.subtitle')}</Typography>

      {/* Conta do administrador */}
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>{t('settings.adminAccount')}</Typography>
      <Card sx={{ mb: 3, p: 3 }}>
        {!user ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}><CircularProgress size={24} /></Box>
        ) : (
          <>
            <Box sx={{ display: 'flex', gap: 2 }}>
              <TextField label={t('settings.name')} value={name} onChange={(e) => setName(e.target.value)} fullWidth sx={{ mb: 2 }} />
              <TextField label={t('settings.email')} value={email} disabled fullWidth sx={{ mb: 2 }} />
            </Box>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button variant="contained" size="small" startIcon={<Save />} onClick={saveProfile} disabled={savingProfile}>
                {savingProfile ? t('settings.saving') : t('settings.saveChanges')}
              </Button>
              <Button variant="outlined" size="small" startIcon={<Lock />} onClick={() => setPwOpen(true)}>{t('settings.changePassword')}</Button>
            </Box>
          </>
        )}
      </Card>

      {/* Casa (Household) */}
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>{t('settings.household')}</Typography>
      <Card sx={{ mb: 3, p: 3 }}>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <TextField label={t('settings.householdName')} value={activeHousehold?.name || ''} disabled fullWidth sx={{ mb: 2 }} />
          <TextField
            label={t('settings.inviteCode')}
            value={activeHousehold?.inviteCode || ''}
            fullWidth
            sx={{ mb: 2 }}
            slotProps={{ htmlInput: { readOnly: true } }}
          />
        </Box>
        <Button
          variant="outlined"
          size="small"
          startIcon={<Refresh />}
          onClick={regenerateInviteCode}
          disabled={regenerating || !activeHousehold}
        >
          {regenerating ? t('settings.generating') : t('settings.generateCode')}
        </Button>
      </Card>

      {/* Localização & Moeda */}
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>{t('settings.locMoney')}</Typography>
      <Card sx={{ mb: 3, p: 3 }}>
        <TextField
          select
          label={t('settings.language')}
          value={lang}
          onChange={(e) => setLang(e.target.value as Lang)}
          fullWidth
          sx={{ mb: 2 }}
        >
          {LANGUAGES.map((l) => (
            <MenuItem key={l.value} value={l.value}>{l.label}</MenuItem>
          ))}
        </TextField>
        <TextField
          select
          label={t('settings.currency')}
          value={currency}
          onChange={(e) => setCurrency(e.target.value)}
          fullWidth
          sx={{ mb: 2 }}
        >
          {CURRENCIES.map((c) => (
            <MenuItem key={c.code} value={c.code}>{c.label}</MenuItem>
          ))}
        </TextField>
        <Button variant="contained" size="small" startIcon={<Save />} onClick={saveCurrency} disabled={savingCurrency}>
          {savingCurrency ? t('settings.saving') : t('settings.saveCurrency')}
        </Button>
      </Card>

      {/* Segurança */}
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>{t('settings.security')}</Typography>
      <Card sx={{ mb: 3 }}>
        {[
          { icon: <Lock />, name: t('settings.https'), desc: t('settings.httpsDesc'), defaultChecked: true },
          { icon: <Refresh />, name: t('settings.rateLimit'), desc: t('settings.rateLimitDesc'), defaultChecked: true },
        ].map((s, i) => (
          <Box key={s.name}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 2.5 }}>
              <Box sx={{ width: 44, height: 44, borderRadius: 2.5, bgcolor: 'action.hover', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{s.icon}</Box>
              <Box sx={{ flex: 1 }}>
                <Typography sx={{ fontWeight: 600 }}>{s.name}</Typography>
                <Typography variant="body2" color="text.secondary">{s.desc}</Typography>
              </Box>
              <Switch defaultChecked={s.defaultChecked} disabled />
            </Box>
            {i < 1 && <Divider />}
          </Box>
        ))}
      </Card>

      {/* Backup & Dados */}
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>{t('settings.backup')}</Typography>
      <Card sx={{ mb: 3, p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box>
            <Typography sx={{ fontWeight: 600 }}>{t('settings.exportData')}</Typography>
            <Typography variant="body2" color="text.secondary">{t('settings.exportDesc')}</Typography>
          </Box>
          <Button variant="outlined" size="small" startIcon={<Download />} disabled>{t('settings.export')}</Button>
        </Box>
        <Divider sx={{ my: 2 }} />
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Box>
            <Typography sx={{ fontWeight: 600 }}>{t('settings.autoBackup')}</Typography>
            <Typography variant="body2" color="text.secondary">{t('settings.autoBackupDesc')}</Typography>
          </Box>
          <Button variant="outlined" size="small" startIcon={<Backup />} disabled>{t('settings.backupNow')}</Button>
        </Box>
      </Card>

      {/* Sobre */}
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1 }}>{t('settings.about')}</Typography>
      <Card sx={{ mb: 3, p: 3, display: 'flex', alignItems: 'center', gap: 3 }}>
        <Box sx={{ width: 56, height: 56, flexShrink: 0 }}>
          <img src="./logo.svg" alt="Listaih" width="56" height="56" style={{ borderRadius: 8 }} />
        </Box>
        <Box sx={{ flex: 1 }}>
          <Typography variant="h6">Listaih</Typography>
          <Typography variant="body2" color="text.secondary">{t('settings.aboutVer')}</Typography>
          <Typography variant="caption" color="text.secondary">{t('settings.aboutStack')}</Typography>
        </Box>
      </Card>

      {/* Zona de Perigo */}
      <Alert severity="error" sx={{ mt: 3, border: '1.5px solid', borderColor: 'error.main', borderRadius: 3 }}>
        <AlertTitle sx={{ display: 'flex', alignItems: 'center', gap: 1 }}><Warning fontSize="small" /> {t('settings.dangerZone')}</AlertTitle>
        <Typography sx={{ mb: 2, fontSize: 14 }}>{t('settings.dangerDesc')}</Typography>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          <Button variant="outlined" color="error" size="small" startIcon={<DeleteSweep />} disabled>{t('settings.clearArchived')}</Button>
          <Button variant="outlined" color="error" size="small" startIcon={<RestartAlt />} disabled>{t('settings.resetInteg')}</Button>
          <Button variant="contained" color="error" size="small" startIcon={<Dangerous />} disabled>{t('settings.resetAll')}</Button>
        </Box>
      </Alert>

      {/* Dialog de troca de senha */}
      <Dialog open={pwOpen} onClose={() => setPwOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{t('settings.changePassword')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '16px !important' }}>
          <TextField label={t('settings.pwCurrent')} type="password" value={currentPw} onChange={(e) => setCurrentPw(e.target.value)} fullWidth />
          <TextField label={t('settings.pwNew')} type="password" value={newPw} onChange={(e) => setNewPw(e.target.value)} fullWidth />
          <TextField label={t('settings.pwConfirm')} type="password" value={confirmPw} onChange={(e) => setConfirmPw(e.target.value)} fullWidth />
          {pwError && <Typography color="error" variant="body2">{pwError}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPwOpen(false)}>{t('settings.cancel')}</Button>
          <Button variant="contained" onClick={changePassword} disabled={savingPw || !currentPw || !newPw}>
            {savingPw ? t('settings.saving') : t('settings.pwChange')}
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={!!snack}
        autoHideDuration={4000}
        onClose={() => setSnack(null)}
        message={snack}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </Box>
  )
}
