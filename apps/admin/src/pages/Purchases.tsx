import { useState, useEffect } from 'react'
import {
  Box, Card, Typography, Chip, IconButton, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Dialog, DialogTitle, DialogContent,
  DialogActions, Button, TextField, MenuItem, Snackbar, Alert, Divider,
  CircularProgress, Grid,
} from '@mui/material'
import {
  ArrowBack, Receipt, Payments, Inventory, PhotoCamera, Sync as SyncIcon,
} from '@mui/icons-material'
import api from '@/services/api'
import { useHousehold, type Purchase } from '@/hooks/useHousehold'
import { useSystemConfig, formatCurrency, getCurrencySymbol } from '@/hooks/useSystemConfig'
import { useI18n } from '@/hooks/useI18n'
import { getLocale } from '@/hooks/useI18n'

const PAYMENT_METHODS = ['DEBITO', 'CREDITO', 'DINHEIRO', 'PIX', 'VR', 'VA'] as const

export default function Purchases() {
  const { activeHousehold } = useHousehold()
  const { config } = useSystemConfig()
  const { t, lang } = useI18n()
  const currency = config?.currency || 'BRL'

  const [purchases, setPurchases] = useState<Purchase[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedPurchase, setSelectedPurchase] = useState<Purchase | null>(null)
  const [snack, setSnack] = useState<string | null>(null)
  const [snackSev, setSnackSev] = useState<'success' | 'error' | 'info'>('success')

  const [editing, setEditing] = useState(false)
  const [editPayment, setEditPayment] = useState('')
  const [editTotal, setEditTotal] = useState('')
  const [editNotes, setEditNotes] = useState('')
  const [editReceipt, setEditReceipt] = useState('')
  const [saving, setSaving] = useState(false)

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString(getLocale(lang), {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const getPaymentLabel = (pm: string | null) => {
    if (!pm) return '—'
    return t(`pay.${pm.toLowerCase()}`)
  }

  const getReceiptStatusColor = (status: string): 'default' | 'warning' | 'info' | 'success' | 'error' => {
    switch (status) {
      case 'PARSED': return 'success'
      case 'PROCESSING': return 'info'
      case 'PENDING': return 'warning'
      case 'FAILED': return 'error'
      default: return 'default'
    }
  }

  const loadPurchases = async () => {
    if (!activeHousehold) return
    setLoading(true)
    try {
      const res = await api.get(`/households/${activeHousehold.id}/purchases`)
      setPurchases(Array.isArray(res.data) ? res.data : (res.data.value || []))
    } catch {
      setSnack(t('purch.loadError'))
      setSnackSev('error')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadPurchases()
  }, [activeHousehold])

  const openDetail = async (purchase: Purchase) => {
    try {
      const res = await api.get(`/purchases/${purchase.id}`)
      setSelectedPurchase(res.data)
    } catch {
      setSnack(t('purch.loadError'))
      setSnackSev('error')
    }
  }

  const startEdit = () => {
    if (!selectedPurchase) return
    setEditPayment(selectedPurchase.paymentMethod ? String(selectedPurchase.paymentMethod) : '')
    setEditTotal(selectedPurchase.totalAmount ? String(selectedPurchase.totalAmount) : '')
    setEditNotes(selectedPurchase.notes || '')
    setEditReceipt(selectedPurchase.receiptPhoto || '')
    setEditing(true)
  }

  const saveEdit = async () => {
    if (!selectedPurchase) return
    setSaving(true)
    try {
      const data: Record<string, any> = {}
      if (editPayment) data.paymentMethod = editPayment as Purchase['paymentMethod']
      if (editTotal !== '') data.totalAmount = parseFloat(editTotal)
      if (editNotes !== selectedPurchase.notes) data.notes = editNotes
      if (editReceipt !== (selectedPurchase.receiptPhoto || '')) {
        data.receiptPhoto = editReceipt || null
      }
      const res = await api.patch(`/purchases/${selectedPurchase.id}`, data)
      setSelectedPurchase(res.data)
      setEditing(false)
      loadPurchases()
      setSnack(t('purch.updated'))
      setSnackSev('success')
    } catch {
      setSnack(t('purch.updateError'))
      setSnackSev('error')
    } finally {
      setSaving(false)
    }
  }

  const totalSpent = purchases.reduce((s, p) => s + (p.totalAmount || 0), 0)
  totalSpent.toFixed(2)
  const totalItems = purchases.reduce((s, p) => s + p.itemCount, 0)

  // ===== DETAIL VIEW =====
  if (selectedPurchase) {
    const items: Purchase['items'] = selectedPurchase.items as any || []
    const totalFromItems = items.reduce((s, i) => {
      const price = i.actualPrice ?? i.estimatedPrice ?? 0
      return s + price * i.quantity
    }, 0)

    return (
      <Box sx={{ maxWidth: 1200 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2, cursor: 'pointer' }} onClick={() => setSelectedPurchase(null)}>
          <ArrowBack sx={{ color: 'primary.main' }} />
          <Typography sx={{ color: 'primary.main', fontWeight: 500, fontSize: 14 }}>{t('purch.back')}</Typography>
        </Box>

        <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
          <Box sx={{ width: 280, flexShrink: 0 }}>
            <Card sx={{ mb: 2, p: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <Receipt sx={{ color: 'primary.main' }} />
                <Typography variant="h6">{t('purch.detail')}</Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">{selectedPurchase.list?.name || '—'}</Typography>
              <Typography variant="caption" color="text.secondary">{formatDate(selectedPurchase.date)}</Typography>
            </Card>
            <Card sx={{ mb: 2, p: 2 }}>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>{t('purch.summary')}</Typography>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="body2">{t('purch.total')}</Typography>
                <Typography sx={{ fontWeight: 600, color: 'primary.main' }}>
                  {formatCurrency(selectedPurchase.totalAmount || totalFromItems, currency)}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="body2">{t('purch.payment')}</Typography>
                <Typography sx={{ fontWeight: 600 }}>{getPaymentLabel(selectedPurchase.paymentMethod)}</Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="body2">{t('purch.itemCount')}</Typography>
                <Typography sx={{ fontWeight: 600 }}>{selectedPurchase.itemCount}</Typography>
              </Box>
              <Divider sx={{ my: 1 }} />
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="body2">Grocy</Typography>
                <Chip
                  size="small"
                  label={selectedPurchase.grocySynced ? t('purch.synced') : t('purch.notSynced')}
                  color={selectedPurchase.grocySynced ? 'success' : 'default'}
                  sx={{ fontSize: 11 }}
                />
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="body2">{t('purch.receiptStatus')}</Typography>
                <Chip
                  size="small"
                  label={t(`purch.rs.${selectedPurchase.receiptStatus.toLowerCase()}`)}
                  color={getReceiptStatusColor(selectedPurchase.receiptStatus)}
                  sx={{ fontSize: 11 }}
                />
              </Box>
            </Card>
            {selectedPurchase.notes && (
              <Card sx={{ mb: 2, p: 2 }}>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>{t('purch.notes')}</Typography>
                <Typography variant="body2">{selectedPurchase.notes}</Typography>
              </Card>
            )}
            <Button variant="outlined" fullWidth startIcon={<PhotoCamera />} onClick={startEdit}>
              {t('purch.edit')}
            </Button>
          </Box>

          {/* Items table */}
          <Box sx={{ flex: 1, minWidth: 300 }}>
            <Card sx={{ p: 3 }}>
              <Typography variant="h6" sx={{ mb: 2 }}>{t('purch.items')} ({items.length})</Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>{t('purch.col.name')}</TableCell>
                      <TableCell align="right">{t('purch.col.qty')}</TableCell>
                      <TableCell align="right">{t('purch.col.est')}</TableCell>
                      <TableCell align="right">{t('purch.col.actual')}</TableCell>
                      <TableCell>{t('purch.col.category')}</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {items.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell sx={{ fontWeight: 500 }}>{item.name}</TableCell>
                        <TableCell align="right">{item.quantity} {item.unit}</TableCell>
                        <TableCell align="right">{item.estimatedPrice ? formatCurrency(item.estimatedPrice, currency) : '—'}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 600, color: 'primary.main' }}>
                          {item.actualPrice ? formatCurrency(item.actualPrice, currency) : '—'}
                        </TableCell>
                        <TableCell>
                          {item.category ? (
                            <Chip size="small" label={item.category} sx={{ fontSize: 11 }} />
                          ) : '—'}
                        </TableCell>
                      </TableRow>
                    ))}
                    <TableRow>
                      <TableCell colSpan={3} />
                      <TableCell align="right" sx={{ fontWeight: 700, borderTop: '2px solid', borderColor: 'divider' }}>
                        {formatCurrency(totalFromItems, currency)}
                      </TableCell>
                      <TableCell />
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </Card>
          </Box>
        </Box>

        {/* Edit purchase dialog */}
        <Dialog open={editing} onClose={() => setEditing(false)} maxWidth="xs" fullWidth>
          <DialogTitle>{t('purch.editTitle')}</DialogTitle>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '16px !important' }}>
            <TextField
              select
              label={t('purch.payment')}
              value={editPayment}
              onChange={(e) => setEditPayment(e.target.value)}
              fullWidth
            >
              <MenuItem value="">{t('purch.noPayment')}</MenuItem>
              {PAYMENT_METHODS.map((pm) => (
                <MenuItem key={pm} value={pm}>{t(`pay.${pm.toLowerCase()}`)}</MenuItem>
              ))}
            </TextField>
            <TextField
              label={`${t('purch.total')} (${getCurrencySymbol(currency)})`}
              type="number"
              value={editTotal}
              onChange={(e) => setEditTotal(e.target.value)}
              fullWidth
            />
            <TextField
              label={t('purch.notes')}
              value={editNotes}
              onChange={(e) => setEditNotes(e.target.value)}
              fullWidth
              multiline
              rows={2}
            />
            <TextField
              label={t('purch.receiptUrl')}
              value={editReceipt}
              onChange={(e) => setEditReceipt(e.target.value)}
              fullWidth
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditing(false)}>{t('purch.cancel')}</Button>
            <Button variant="contained" onClick={saveEdit} disabled={saving}>
              {saving ? t('purch.saving') : t('purch.save')}
            </Button>
          </DialogActions>
        </Dialog>

        <Snackbar open={!!snack} autoHideDuration={3000} onClose={() => setSnack(null)} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
          <Alert severity={snackSev} onClose={() => setSnack(null)}>{snack}</Alert>
        </Snackbar>
      </Box>
    )
  }

  // ===== LIST VIEW =====
  return (
    <Box sx={{ maxWidth: 1200 }}>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" sx={{ mb: 0.5 }}>{t('purch.title')}</Typography>
        <Typography color="text.secondary">{t('purch.subtitle')}</Typography>
      </Box>

      {/* Summary cards */}
      <Grid container spacing={2.5} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <Card>
            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ width: 48, height: 48, borderRadius: 2.5, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'primary.main', color: '#fff' }}>
                <Payments />
              </Box>
              <Box>
                <Typography variant="body2" color="text.secondary">{t('purch.totalSpent')}</Typography>
                <Typography variant="h5" sx={{ fontWeight: 700 }}>{formatCurrency(totalSpent, currency)}</Typography>
              </Box>
            </Box>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <Card>
            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ width: 48, height: 48, borderRadius: 2.5, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'secondary.main', color: '#fff' }}>
                <Receipt />
              </Box>
              <Box>
                <Typography variant="body2" color="text.secondary">{t('purch.totalPurchases')}</Typography>
                <Typography variant="h5" sx={{ fontWeight: 700 }}>{purchases.length}</Typography>
              </Box>
            </Box>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 4 }}>
          <Card>
            <Box sx={{ p: 2, display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ width: 48, height: 48, borderRadius: 2.5, display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'info.main', color: '#fff' }}>
                <Inventory />
              </Box>
              <Box>
                <Typography variant="body2" color="text.secondary">{t('purch.totalItems')}</Typography>
                <Typography variant="h5" sx={{ fontWeight: 700 }}>{totalItems}</Typography>
              </Box>
            </Box>
          </Card>
        </Grid>
      </Grid>

      <Card>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
            <CircularProgress />
          </Box>
        ) : purchases.length === 0 ? (
          <Box sx={{ p: 4, textAlign: 'center' }}>
            <Receipt sx={{ fontSize: 48, color: 'text.disabled', mb: 1 }} />
            <Typography color="text.secondary">{t('purch.none')}</Typography>
          </Box>
        ) : (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>{t('purch.col.date')}</TableCell>
                  <TableCell>{t('purch.col.list')}</TableCell>
                  <TableCell align="right">{t('purch.col.items')}</TableCell>
                  <TableCell align="right">{t('purch.col.total')}</TableCell>
                  <TableCell>{t('purch.col.payment')}</TableCell>
                  <TableCell>{t('purch.col.grocy')}</TableCell>
                  <TableCell>{t('purch.col.receipt')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {purchases.map((purch) => (
                  <TableRow
                    key={purch.id}
                    hover
                    onClick={() => openDetail(purch)}
                    sx={{ cursor: 'pointer' }}
                  >
                    <TableCell sx={{ fontSize: 13 }}>{formatDate(purch.date)}</TableCell>
                    <TableCell sx={{ fontWeight: 500 }}>{purch.list?.name || '—'}</TableCell>
                    <TableCell align="right">{purch.itemCount}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 600, color: 'primary.main' }}>
                      {purch.totalAmount ? formatCurrency(purch.totalAmount, currency) : '—'}
                    </TableCell>
                    <TableCell>{getPaymentLabel(purch.paymentMethod)}</TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={purch.grocySynced ? t('purch.synced') : t('purch.notSynced')}
                        color={purch.grocySynced ? 'success' : 'default'}
                        sx={{ fontSize: 11 }}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={t(`purch.rs.${purch.receiptStatus.toLowerCase()}`)}
                        color={getReceiptStatusColor(purch.receiptStatus)}
                        sx={{ fontSize: 11 }}
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Card>

      <Snackbar open={!!snack} autoHideDuration={3000} onClose={() => setSnack(null)} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        <Alert severity={snackSev} onClose={() => setSnack(null)}>{snack}</Alert>
      </Snackbar>
    </Box>
  )
}
