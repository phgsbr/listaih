import { useState, useEffect } from 'react'
import { Box, Card, Typography, TextField, Button, Switch, FormControlLabel, Divider, Alert } from '@mui/material'
import { Save, WifiProtectedSetup, Link as LinkIcon, Info, Api, Security } from '@mui/icons-material'
import api from '@/services/api'
import { useI18n } from '@/hooks/useI18n'

interface SystemConfig {
  grocyUrl: string | null
  grocyApiKey: string | null
  grocyEnabled: boolean
  haUrl: string | null
  haWebhookToken: string | null
  haEnabled: boolean
  apiEnabled: boolean
  apiBaseUrl: string | null
  apiKey: string | null
}

export default function Integrations() {
  const { t } = useI18n()
  const [grocyUrl, setGrocyUrl] = useState('')
  const [grocyApiKey, setGrocyApiKey] = useState('')
  const [grocyEnabled, setGrocyEnabled] = useState(false)
  const [haUrl, setHaUrl] = useState('')
  const [haWebhookToken, setHaWebhookToken] = useState('')
  const [haEnabled, setHaEnabled] = useState(false)
  const [apiEnabled, setApiEnabled] = useState(false)
  const [apiBaseUrl, setApiBaseUrl] = useState('')
  const [apiKey, setApiKey] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    api.get('/system/config').then((res) => {
      setGrocyUrl(res.data.grocyUrl || '')
      setGrocyApiKey(res.data.grocyApiKey || '')
      setGrocyEnabled(res.data.grocyEnabled)
      setHaUrl(res.data.haUrl || '')
      setHaWebhookToken(res.data.haWebhookToken || '')
      setHaEnabled(res.data.haEnabled)
      setApiEnabled(res.data.apiEnabled)
      setApiBaseUrl(res.data.apiBaseUrl || '')
      setApiKey(res.data.apiKey || '')
    })
  }, [])

  const saveGrocy = async () => {
    setSaving(true)
    await api.put('/system/config', { grocyUrl, grocyApiKey, grocyEnabled })
    setSaving(false)
  }

  const saveHA = async () => {
    setSaving(true)
    await api.put('/system/config', { haUrl, haWebhookToken, haEnabled })
    setSaving(false)
  }

  const saveExternalApi = async () => {
    setSaving(true)
    await api.put('/system/config', { apiEnabled, apiBaseUrl, apiKey })
    setSaving(false)
  }

  return (
    <Box sx={{ maxWidth: 1200 }}>
      <Typography variant="h4" sx={{ mb: 0.5 }}>{t('integ.title')}</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>{t('integ.subtitle')}</Typography>

      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: 2.5 }}>
        {/* GROCY */}
        <Card sx={{ p: 3, border: grocyEnabled ? '1.5px solid #4CAF50' : 'none' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            <Box sx={{ width: 56, height: 56, borderRadius: 2, flexShrink: 0, overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <img src="./icons/grocy.png" alt="Grocy" width="48" height="48" />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>{t('integ.grocy')}</Typography>
              <Typography variant="body2" color="text.secondary">{t('integ.grocy.desc')}</Typography>
            </Box>
          </Box>
          <TextField label={t('integ.grocyUrl')} placeholder={t('integ.grocyUrlPh')} value={grocyUrl} onChange={(e) => setGrocyUrl(e.target.value)} fullWidth sx={{ mb: 2 }} />
          <TextField label={t('integ.apiKey')} type="password" value={grocyApiKey} onChange={(e) => setGrocyApiKey(e.target.value)} fullWidth sx={{ mb: 2 }} />
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <Button variant="outlined" size="small" startIcon={<WifiProtectedSetup />}>{t('integ.test')}</Button>
            <Button variant="contained" size="small" startIcon={<Save />} disabled={saving} onClick={saveGrocy}>{t('integ.save')}</Button>
          </Box>
          <Divider sx={{ mb: 2 }} />
          <FormControlLabel
            control={<Switch checked={grocyEnabled} onChange={(e) => setGrocyEnabled(e.target.checked)} />}
            label={<Typography sx={{ fontSize: 14, fontWeight: 500 }}>{t('integ.syncActive')}</Typography>}
          />
        </Card>

        {/* HOME ASSISTANT */}
        <Card sx={{ p: 3, border: haEnabled ? '1.5px solid #4CAF50' : 'none' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            <Box sx={{ width: 56, height: 56, borderRadius: 2, flexShrink: 0, overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <img src="./icons/home-assistant.png" alt="Home Assistant" width="48" height="48" />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>{t('integ.ha')}</Typography>
              <Typography variant="body2" color="text.secondary">{t('integ.ha.desc')}</Typography>
            </Box>
          </Box>
          <TextField label={t('integ.haUrl')} placeholder={t('integ.haUrlPh')} value={haUrl} onChange={(e) => setHaUrl(e.target.value)} fullWidth sx={{ mb: 2 }} />
          <TextField label={t('integ.webhookToken')} type="password" value={haWebhookToken} onChange={(e) => setHaWebhookToken(e.target.value)} fullWidth sx={{ mb: 2 }} />
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <Button variant="outlined" size="small" startIcon={<WifiProtectedSetup />}>{t('integ.test')}</Button>
            <Button variant="contained" size="small" startIcon={<Save />} disabled={saving} onClick={saveHA}>{t('integ.save')}</Button>
          </Box>
          <Divider sx={{ mb: 2 }} />
          <FormControlLabel
            control={<Switch checked={haEnabled} onChange={(e) => setHaEnabled(e.target.checked)} />}
            label={<Typography sx={{ fontSize: 14, fontWeight: 500 }}>{t('integ.integActive')}</Typography>}
          />
        </Card>

        {/* EXTERNAL API */}
        <Card sx={{ p: 3, border: apiEnabled ? '1.5px solid #2196F3' : 'none' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            <Box sx={{ width: 56, height: 56, borderRadius: 2, flexShrink: 0, overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'action.hover' }}>
              <Api sx={{ fontSize: 28, color: 'primary.main' }} />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>{t('integ.externalApi')}</Typography>
              <Typography variant="body2" color="text.secondary">{t('integ.externalApi.desc')}</Typography>
            </Box>
          </Box>
          <TextField label={t('integ.apiBaseUrl')} placeholder={t('integ.apiBaseUrlPh')} value={apiBaseUrl} onChange={(e) => setApiBaseUrl(e.target.value)} fullWidth sx={{ mb: 2 }} />
          <TextField label={t('integ.apiKey')} type="password" value={apiKey} onChange={(e) => setApiKey(e.target.value)} fullWidth sx={{ mb: 2 }} />
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <Button variant="outlined" size="small" startIcon={<WifiProtectedSetup />}>{t('integ.test')}</Button>
            <Button variant="contained" size="small" startIcon={<Save />} disabled={saving} onClick={saveExternalApi}>{t('integ.save')}</Button>
          </Box>
          <Divider sx={{ mb: 2 }} />
          <FormControlLabel
            control={<Switch checked={apiEnabled} onChange={(e) => setApiEnabled(e.target.checked)} />}
            label={<Typography sx={{ fontSize: 14, fontWeight: 500 }}>{t('integ.integActive')}</Typography>}
          />
        </Card>

        {/* ALEXA (placeholder - futuro) */}
        <Card sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            <Box sx={{ width: 56, height: 56, borderRadius: 2, flexShrink: 0, overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <img src="./icons/amazon-alexa.png" alt="Alexa" width="48" height="48" />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>{t('integ.alexa')}</Typography>
              <Typography variant="body2" color="text.secondary">{t('integ.alexa.desc')}</Typography>
            </Box>
          </Box>
          <Alert severity="info" sx={{ mb: 2, fontSize: 13 }}>{t('integ.alexa.alert')}</Alert>
          <TextField label={t('integ.publicUrl')} placeholder="https://listaih.seu-dominio.com" disabled fullWidth sx={{ mb: 2 }} />
          <Button variant="outlined" size="small" disabled startIcon={<LinkIcon />}>{t('integ.alexaConfig')}</Button>
        </Card>
      </Box>
    </Box>
  )
}
