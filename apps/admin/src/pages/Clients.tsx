import { useState, useEffect } from 'react'
import {
  Box, Card, Typography, TextField, Button, MenuItem, IconButton, Divider,
  Dialog, DialogTitle, DialogContent, DialogActions, Alert, Snackbar,
} from '@mui/material'
import { Key, PhoneAndroid, Watch, Mic, ContentCopy, Delete, Info, Check } from '@mui/icons-material'
import api from '@/services/api'
import { useI18n } from '@/hooks/useI18n'

interface ApiToken {
  id: string
  name: string
  prefix: string
  type: string
  lastUsedAt: string | null
  createdAt: string
}

const TYPE_ICONS: Record<string, React.ReactNode> = {
  android: <PhoneAndroid />,
  wear: <Watch />,
  alexa: <Mic />,
  ha: <Key />,
  generic: <Key />,
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })
}

export default function Clients() {
  const { t } = useI18n()
  const [tokens, setTokens] = useState<ApiToken[]>([])
  const [name, setName] = useState('')
  const [type, setType] = useState('android')
  const [creating, setCreating] = useState(false)
  const [newToken, setNewToken] = useState<string | null>(null)
  const [snack, setSnack] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  const load = async () => {
    try {
      const res = await api.get('/tokens')
      setTokens(Array.isArray(res.data) ? res.data : (res.data.value || []))
    } catch {
      setSnack(t('client.loadError'))
    }
  }

  useEffect(() => { load() }, [])

  const createToken = async () => {
    if (!name.trim()) { setSnack(t('client.needName')); return }
    setCreating(true)
    try {
      const res = await api.post('/tokens', { name, type })
      setNewToken(res.data.token)
      setName('')
      load()
    } catch {
      setSnack(t('client.createError'))
    } finally {
      setCreating(false)
    }
  }

  const revokeToken = async (id: string) => {
    try {
      await api.delete(`/tokens/${id}`)
      setSnack(t('client.revoked'))
      load()
    } catch {
      setSnack(t('client.revokeError'))
    }
  }

  const copyToken = () => {
    if (newToken) {
      navigator.clipboard.writeText(newToken)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  return (
    <Box sx={{ maxWidth: 800 }}>
      <Typography variant="h4" sx={{ mb: 0.5 }}>{t('clients.title')}</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>{t('clients.subtitle')}</Typography>

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 3, color: 'text.secondary' }}>
        <Info sx={{ fontSize: 16 }} />
        <Typography variant="body2">{t('clients.info')}</Typography>
      </Box>

      <Typography variant="h6" sx={{ fontWeight: 600, mb: 1.5 }}>{t('clients.active')}</Typography>
      {tokens.length === 0 ? (
        <Card sx={{ mb: 3, p: 4, textAlign: 'center' }}>
          <Typography color="text.secondary">{t('clients.none')}</Typography>
        </Card>
      ) : (
        tokens.map((tk) => (
          <Card key={tk.id} sx={{ mb: 1.5, p: 2.5, display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ width: 44, height: 44, borderRadius: 2.5, bgcolor: 'action.hover', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
              {TYPE_ICONS[tk.type] || <Key />}
            </Box>
            <Box sx={{ flex: 1 }}>
              <Typography sx={{ fontWeight: 600, fontSize: 15 }}>{tk.name}</Typography>
              <Typography variant="body2" color="text.secondary">
                {t('clients.createdOn')} {formatDate(tk.createdAt)} · {tk.lastUsedAt ? `${t('clients.lastUsed')}: ${formatDate(tk.lastUsedAt)}` : t('clients.neverUsed')}
              </Typography>
              <Typography sx={{ fontFamily: 'monospace', fontSize: 13, bgcolor: 'action.hover', p: 0.5, px: 1, borderRadius: 1, mt: 0.5, display: 'inline-block' }}>
                {tk.prefix}...
              </Typography>
            </Box>
            <IconButton size="small" color="error" onClick={() => revokeToken(tk.id)}><Delete fontSize="small" /></IconButton>
          </Card>
        ))
      )}

      <Divider sx={{ my: 3 }} />
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>{t('clients.generate')}</Typography>
      <Box sx={{ maxWidth: 500, display: 'flex', flexDirection: 'column', gap: 2.5 }}>
        <TextField label={t('clients.clientName')} placeholder={t('clients.clientNamePh')} value={name} onChange={(e) => setName(e.target.value)} fullWidth />
        <TextField select label={t('clients.clientType')} value={type} onChange={(e) => setType(e.target.value)} fullWidth>
          <MenuItem value="android">{t('clients.type.android')}</MenuItem>
          <MenuItem value="wear">{t('clients.type.wear')}</MenuItem>
          <MenuItem value="alexa">{t('clients.type.alexa')}</MenuItem>
          <MenuItem value="ha">{t('clients.type.ha')}</MenuItem>
          <MenuItem value="generic">{t('clients.type.generic')}</MenuItem>
        </TextField>
        <Button variant="contained" startIcon={<Key />} sx={{ alignSelf: 'flex-start' }} onClick={createToken} disabled={creating}>
          {creating ? t('clients.generating') : t('clients.generateBtn')}
        </Button>
      </Box>

      <Dialog open={!!newToken} onClose={() => setNewToken(null)} maxWidth="sm" fullWidth>
        <DialogTitle>{t('clients.tokenCreated')}</DialogTitle>
        <DialogContent>
          <Alert severity="warning" sx={{ mb: 2 }}>
            {t('client.copyWarning')}
          </Alert>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            <TextField value={newToken || ''} fullWidth slotProps={{ htmlInput: { readOnly: true, style: { fontFamily: 'monospace', fontSize: 13 } } }} />
            <Button variant="outlined" startIcon={copied ? <Check /> : <ContentCopy />} onClick={copyToken}>
              {copied ? t('client.copied') : t('client.copy')}
            </Button>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button variant="contained" onClick={() => setNewToken(null)}>{t('client.close')}</Button>
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
