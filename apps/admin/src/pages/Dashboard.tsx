import { useState, useEffect } from 'react'
import { Box, Card, CardContent, Grid, Typography, Button, Chip } from '@mui/material'
import {
  ShoppingCart,
  Inventory2,
  Payments,
  CheckCircle,
  ArrowForward,
  Sync,
} from '@mui/icons-material'
import { useNavigate } from 'react-router-dom'
import api from '@/services/api'
import { useHousehold, type ShoppingList } from '@/hooks/useHousehold'
import { useSystemConfig, formatCurrency } from '@/hooks/useSystemConfig'
import { useI18n } from '@/hooks/useI18n'

interface HealthStatus {
  status: string
  services: { database: string; redis: string }
  integrations: {
    grocy: { enabled: boolean; url: string | null }
    homeAssistant: { enabled: boolean; url: string | null }
  }
}

export default function Dashboard() {
  const navigate = useNavigate()
  const { activeHousehold } = useHousehold()
  const { config } = useSystemConfig()
  const { t } = useI18n()
  const [lists, setLists] = useState<ShoppingList[]>([])
  const [health, setHealth] = useState<HealthStatus | null>(null)
  const currency = config?.currency || 'BRL'

  useEffect(() => {
    if (activeHousehold) {
      api.get(`/households/${activeHousehold.id}/lists`).then((res) => setLists(res.data.value || res.data))
    }
    api.get('/health').then((res) => setHealth(res.data))
  }, [activeHousehold])

  const totalItems = lists.reduce((sum, l) => sum + (l.items?.length || 0), 0)
  const pendingItems = lists.reduce(
    (sum, l) => sum + (l.items?.filter((i) => !i.checked).length || 0),
    0,
  )
  const totalEstimated = lists.reduce(
    (sum, l) => sum + (l.items?.reduce((s, i) => s + (i.estimatedPrice || 0) * i.quantity, 0) || 0),
    0,
  )

  const STAT_CARDS = [
    { label: t('dash.activeLists'), value: String(lists.length), icon: <ShoppingCart />, color: 'primary' as const },
    { label: t('dash.pendingItems'), value: String(pendingItems), icon: <Inventory2 />, color: 'secondary' as const },
    { label: t('dash.estimatedValue'), value: formatCurrency(totalEstimated, currency), icon: <Payments />, color: 'secondary' as const },
    { label: t('dash.checkedItems'), value: String(totalItems - pendingItems), icon: <CheckCircle />, color: 'error' as const },
  ]

  return (
    <Box sx={{ maxWidth: 1200 }}>
      <Typography variant="h4" sx={{ mb: 0.5 }}>{t('dash.title')}</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        {activeHousehold ? `${activeHousehold.name} · ` : ''}{t('dash.subtitle')}
      </Typography>

      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        {STAT_CARDS.map((stat) => (
          <Grid size={{ xs: 12, sm: 6, md: 3 }} key={stat.label}>
            <Card>
              <CardContent sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                <Box
                  sx={{
                    width: 48, height: 48, borderRadius: 2.5, flexShrink: 0,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    bgcolor: `${stat.color}.main`, color: '#fff',
                  }}
                >
                  {stat.icon}
                </Box>
                <Box sx={{ flex: 1 }}>
                  <Typography variant="body2" color="text.secondary">{stat.label}</Typography>
                  <Typography variant="h4" sx={{ fontWeight: 700, lineHeight: 1.2 }}>{stat.value}</Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Card sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>{t('dash.recentLists')}</Typography>
          <Button variant="contained" size="small" startIcon={<ArrowForward />} onClick={() => navigate('/admin/lists')}>{t('dash.seeAll')}</Button>
        </Box>
        {lists.length === 0 ? (
          <Box sx={{ p: 4, textAlign: 'center' }}>
            <Typography color="text.secondary">{t('dash.noLists')}</Typography>
          </Box>
        ) : (
          <Box component="table" sx={{ width: '100%', borderCollapse: 'collapse' }}>
            <Box component="thead">
              <Box component="tr" sx={{ borderBottom: '1px solid', borderColor: 'divider' }}>
                {[t('dash.col.name'), t('dash.col.items'), t('dash.col.progress'), t('dash.col.estimated')].map((h) => (
                  <Box key={h} component="th" sx={{ textAlign: 'left', px: 2.5, py: 1.5, fontSize: 12, fontWeight: 600, color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 0.5 }}>{h}</Box>
                ))}
              </Box>
            </Box>
            <Box component="tbody">
              {lists.slice(0, 5).map((list) => {
                const items = list.items || []
                const checked = items.filter((i) => i.checked).length
                const est = items.reduce((s, i) => s + (i.estimatedPrice || 0) * i.quantity, 0)
                return (
                  <Box key={list.id} component="tr" sx={{ borderBottom: '1px solid', borderColor: 'divider', cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }} onClick={() => navigate('/admin/lists', { state: { listId: list.id } })}>
                    <Box component="td" sx={{ px: 2.5, py: 2, fontSize: 14, fontWeight: 500 }}>{list.name}</Box>
                    <Box component="td" sx={{ px: 2.5, py: 2, fontSize: 14 }}>{items.length}</Box>
                    <Box component="td" sx={{ px: 2.5, py: 2, fontSize: 14 }}>{checked}/{items.length}</Box>
                    <Box component="td" sx={{ px: 2.5, py: 2, fontSize: 14, fontWeight: 600, color: 'secondary.main' }}>{formatCurrency(est, currency)}</Box>
                  </Box>
                )
              })}
            </Box>
          </Box>
        )}
      </Card>

      <Card>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>{t('dash.integrations')}</Typography>
          <Button variant="outlined" size="small" onClick={() => navigate('/admin/integrations')}>{t('dash.manage')}</Button>
        </Box>
        <Box component="table" sx={{ width: '100%', borderCollapse: 'collapse' }}>
          <Box component="tbody">
            <Box component="tr" sx={{ borderBottom: '1px solid', borderColor: 'divider' }}>
              <Box component="td" sx={{ px: 2.5, py: 2, fontSize: 14, fontWeight: 500 }}>PostgreSQL</Box>
              <Box component="td" sx={{ px: 2.5, py: 2 }}>
                <Chip label={health?.services.database === 'up' ? t('dash.online') : t('dash.offline')} size="small" color={health?.services.database === 'up' ? 'success' : 'error'} sx={{ fontSize: 12, fontWeight: 600 }} />
              </Box>
              <Box component="td" sx={{ px: 2.5, py: 2 }}><Button variant="outlined" size="small" startIcon={<Sync />}>{t('dash.sync')}</Button></Box>
            </Box>
            <Box component="tr" sx={{ borderBottom: '1px solid', borderColor: 'divider' }}>
              <Box component="td" sx={{ px: 2.5, py: 2, fontSize: 14, fontWeight: 500 }}>Redis</Box>
              <Box component="td" sx={{ px: 2.5, py: 2 }}>
                <Chip label={health?.services.redis === 'up' ? t('dash.online') : t('dash.offline')} size="small" color={health?.services.redis === 'up' ? 'success' : 'error'} sx={{ fontSize: 12, fontWeight: 600 }} />
              </Box>
              <Box component="td" sx={{ px: 2.5, py: 2 }}>—</Box>
            </Box>
            <Box component="tr" sx={{ borderBottom: '1px solid', borderColor: 'divider' }}>
              <Box component="td" sx={{ px: 2.5, py: 2, fontSize: 14, fontWeight: 500 }}>Grocy</Box>
              <Box component="td" sx={{ px: 2.5, py: 2 }}>
                <Chip label={health?.integrations.grocy.enabled ? t('dash.connected') : t('dash.disconnected')} size="small" color={health?.integrations.grocy.enabled ? 'success' : 'default'} sx={{ fontSize: 12, fontWeight: 600 }} />
              </Box>
              <Box component="td" sx={{ px: 2.5, py: 2 }}>{health?.integrations.grocy.url || '—'}</Box>
            </Box>
            <Box component="tr">
              <Box component="td" sx={{ px: 2.5, py: 2, fontSize: 14, fontWeight: 500 }}>Home Assistant</Box>
              <Box component="td" sx={{ px: 2.5, py: 2 }}>
                <Chip label={health?.integrations.homeAssistant.enabled ? t('dash.connected') : t('dash.disconnected')} size="small" color={health?.integrations.homeAssistant.enabled ? 'success' : 'default'} sx={{ fontSize: 12, fontWeight: 600 }} />
              </Box>
              <Box component="td" sx={{ px: 2.5, py: 2 }}>{health?.integrations.homeAssistant.url || '—'}</Box>
            </Box>
          </Box>
        </Box>
      </Card>
    </Box>
  )
}
