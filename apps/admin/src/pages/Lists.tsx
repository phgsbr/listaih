import { useState, useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import {
  Box, Card, Typography, Button, Chip, IconButton, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, MenuItem, Snackbar, Alert,
} from '@mui/material'
import {
  Add, Archive, Unarchive, Delete, ArrowBack, Check, Close,
  ShoppingCart, Edit, PointOfSale,
} from '@mui/icons-material'
import api from '@/services/api'
import { useHousehold, type ShoppingList, type ShoppingListItem } from '@/hooks/useHousehold'
import { useSystemConfig, formatCurrency, getCurrencySymbol } from '@/hooks/useSystemConfig'
import { useI18n } from '@/hooks/useI18n'
import { CATEGORIES, getCategoryLabel, LIST_CATEGORIES, getProductCategoriesForList, getListCategoryLabel, getListCategoryIcon } from '@/utils/categories'

export default function Lists() {
  const { activeHousehold } = useHousehold()
  const { config } = useSystemConfig()
  const { t } = useI18n()
  const currency = config?.currency || 'BRL'

  const UNITS = ['unit', 'kg', 'g', 'L', 'ml', 'caixa', 'dúzia', 'saco', 'bandeja', 'garrafa', 'maço', 'lata', 'pacote', 'lb', 'oz', 'gal', 'qt', 'pt', 'fl oz', 'cup', 'tsp', 'tbsp']
  const getUnitLabel = (u: string) => t(`unit.${u}`)
  const location = useLocation()
  const [lists, setLists] = useState<ShoppingList[]>([])
  const [history, setHistory] = useState<ShoppingList[]>([])
  const [selectedList, setSelectedList] = useState<ShoppingList | null>(null)
  const [showCreate, setShowCreate] = useState(false)
  const [newListName, setNewListName] = useState('')
  const [newListCategory, setNewListCategory] = useState('Geral')
  const [newListType, setNewListType] = useState<'RECORRENTE' | 'PONTUAL' | 'MODELO'>('PONTUAL')
  const [newListGrocy, setNewListGrocy] = useState(false)
  const [editingList, setEditingList] = useState<ShoppingList | null>(null)
  const [editListName, setEditListName] = useState('')
  const [editListCategory, setEditListCategory] = useState('')
  const [editListType, setEditListType] = useState<'RECORRENTE' | 'PONTUAL' | 'MODELO'>('PONTUAL')
  const [editListGrocy, setEditListGrocy] = useState(false)
  const [snack, setSnack] = useState<string | null>(null)

  const [newItemName, setNewItemName] = useState('')
  const [newItemQty, setNewItemQty] = useState(1)
  const [newItemUnit, setNewItemUnit] = useState('unit')
  const [newItemCategory, setNewItemCategory] = useState('')
  const [addingItem, setAddingItem] = useState(false)

  const [editingItem, setEditingItem] = useState<ShoppingListItem | null>(null)
  const [editItemName, setEditItemName] = useState('')
  const [editItemQty, setEditItemQty] = useState(1)
  const [editItemUnit, setEditItemUnit] = useState('unit')
  const [editItemPrice, setEditItemPrice] = useState('')
  const [editItemCategory, setEditItemCategory] = useState('')

  const loadLists = async () => {
    if (!activeHousehold) return
    try {
      const [active, archived] = await Promise.all([
        api.get(`/households/${activeHousehold.id}/lists`),
        api.get(`/households/${activeHousehold.id}/history`),
      ])
      setLists(Array.isArray(active.data) ? active.data : (active.data.value || []))
      setHistory(Array.isArray(archived.data) ? archived.data : (archived.data.value || []))
    } catch {
      setSnack(t('lists.loadError'))
    }
  }

  useEffect(() => {
    loadLists()
  }, [activeHousehold])

  useEffect(() => {
    const listId = (location.state as { listId?: string } | null)?.listId
    if (listId) {
      api.get(`/lists/${listId}`).then((res) => setSelectedList(res.data)).catch(() => {})
    }
  }, [location.state])

  const reloadSelectedList = async (listId: string) => {
    const res = await api.get(`/lists/${listId}`)
    setSelectedList(res.data)
  }

  const createList = async () => {
    if (!activeHousehold || !newListName.trim()) return
    try {
      await api.post(`/households/${activeHousehold.id}/lists`, {
        name: newListName,
        category: newListCategory,
        listType: newListType,
        grocyAssociated: newListGrocy,
      })
      setNewListName('')
      setNewListCategory('Geral')
      setShowCreate(false)
      setSnack(t('lists.created'))
      loadLists()
    } catch {
      setSnack(t('lists.createError'))
    }
  }

  const saveEditList = async () => {
    if (!editingList || !editListName.trim()) return
    try {
      await api.put(`/lists/${editingList.id}`, {
        name: editListName,
        category: editListCategory,
        listType: editListType,
        grocyAssociated: editListGrocy,
      })
      setEditingList(null)
      setSnack(t('lists.updated'))
      loadLists()
    } catch {
      setSnack(t('lists.updateError'))
    }
  }

  const archiveList = async (list: ShoppingList) => {
    try {
      await api.put(`/lists/${list.id}`, { archivedAt: new Date().toISOString() })
      setSnack(t('lists.archivedMsg'))
      loadLists()
    } catch {
      setSnack(t('lists.archiveError'))
    }
  }

  const unarchiveList = async (list: ShoppingList) => {
    try {
      await api.put(`/lists/${list.id}`, { archivedAt: null })
      setSnack(t('lists.restored'))
      loadLists()
    } catch {
      setSnack(t('lists.restoreError'))
    }
  }

  const deleteList = async (list: ShoppingList) => {
    try {
      await api.delete(`/lists/${list.id}`)
      setSnack(t('lists.deleted'))
      loadLists()
    } catch {
      setSnack(t('lists.deleteError'))
    }
  }

  const addItem = async () => {
    if (!selectedList || !newItemName.trim()) return
    setAddingItem(true)
    try {
      await api.post(`/lists/${selectedList.id}/items`, {
        name: newItemName,
        quantity: newItemQty,
        unit: newItemUnit,
        category: newItemCategory || null,
      })
      setNewItemName('')
      setNewItemQty(1)
      setNewItemUnit('unit')
      setNewItemCategory('')
      await reloadSelectedList(selectedList.id)
      loadLists()
      setSnack(t('lists.itemAdded'))
    } catch {
      setSnack(t('lists.itemAddError'))
    } finally {
      setAddingItem(false)
    }
  }

  const toggleCheck = async (item: ShoppingListItem) => {
    if (!selectedList) return
    try {
      await api.patch(`/lists/${selectedList.id}/items/${item.id}`, { checked: !item.checked })
      await reloadSelectedList(selectedList.id)
    } catch {
      setSnack(t('lists.itemUpdateError'))
    }
  }

  const startEditItem = (item: ShoppingListItem) => {
    setEditingItem(item)
    setEditItemName(item.name)
    setEditItemQty(item.quantity)
    setEditItemUnit(item.unit)
    setEditItemPrice(item.estimatedPrice ? String(item.estimatedPrice) : '')
    setEditItemCategory(item.category || '')
  }

  const saveEditItem = async () => {
    if (!selectedList || !editingItem || !editItemName.trim()) return
    try {
      await api.patch(`/lists/${selectedList.id}/items/${editingItem.id}`, {
        name: editItemName,
        quantity: editItemQty,
        unit: editItemUnit,
        estimatedPrice: editItemPrice ? parseFloat(editItemPrice) : null,
        category: editItemCategory || null,
      })
      setEditingItem(null)
      await reloadSelectedList(selectedList.id)
      setSnack(t('lists.itemUpdated'))
    } catch {
      setSnack(t('lists.itemUpdateError'))
    }
  }

  const deleteItem = async (item: ShoppingListItem) => {
    if (!selectedList) return
    try {
      await api.delete(`/lists/${selectedList.id}/items/${item.id}`)
      await reloadSelectedList(selectedList.id)
      loadLists()
      setSnack(t('lists.itemDeleted'))
    } catch {
      setSnack(t('lists.itemDeleteError'))
    }
  }

  const clearList = async () => {
    if (!selectedList) return
    if (!window.confirm(t('lists.clearListConfirm'))) return
    try {
      const itemsToDelete = selectedList.items || []
      await Promise.all(itemsToDelete.map((item) => api.delete(`/lists/${selectedList.id}/items/${item.id}`)))
      await reloadSelectedList(selectedList.id)
      loadLists()
      setSnack(t('lists.clearListSuccess'))
    } catch {
      setSnack(t('lists.clearListError'))
    }
  }

  const [showCheckout, setShowCheckout] = useState(false)
  const [checkoutPayment, setCheckoutPayment] = useState('')
  const [checkoutTotal, setCheckoutTotal] = useState('')
  const [checkoutNotes, setCheckoutNotes] = useState('')
  const [checkoutGrocy, setCheckoutGrocy] = useState(true)
  const [checkingOut, setCheckingOut] = useState(false)

  const doCheckout = async () => {
    if (!selectedList) return
    setCheckingOut(true)
    try {
      const data: Record<string, any> = { grocySync: checkoutGrocy }
      if (checkoutPayment) data.paymentMethod = checkoutPayment
      if (checkoutTotal) data.totalAmount = parseFloat(checkoutTotal)
      if (checkoutNotes) data.notes = checkoutNotes
      await api.post(`/lists/${selectedList.id}/checkout`, data)
      setShowCheckout(false)
      setCheckoutPayment('')
      setCheckoutTotal('')
      setCheckoutNotes('')
      setCheckoutGrocy(true)
      setSnack(t('purch.checkoutSuccess'))
      await reloadSelectedList(selectedList.id)
      loadLists()
    } catch (err: any) {
      setSnack(err.response?.data?.message || t('purch.checkoutError'))
    } finally {
      setCheckingOut(false)
    }
  }

  if (selectedList) {
    const items = selectedList.items || []
    const checkedCount = items.filter((i) => i.checked).length
    const totalEst = items.reduce((s, i) => s + (i.estimatedPrice || 0) * i.quantity, 0)
    const totalSpent = items.reduce((s, i) => s + (i.actualPrice || 0), 0)

    return (
      <Box sx={{ maxWidth: 1200 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2, cursor: 'pointer' }} onClick={() => setSelectedList(null)}>
          <ArrowBack sx={{ color: 'primary.main' }} />
          <Typography sx={{ color: 'primary.main', fontWeight: 500, fontSize: 14 }}>{t('lists.back')}</Typography>
        </Box>

        <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
          {/* Sidebar */}
          <Box sx={{ width: 280, flexShrink: 0 }}>
            <Card sx={{ mb: 2, p: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', color: 'primary.main' }}>{getListCategoryIcon(selectedList.category)}</Box>
                <Typography variant="h6">{selectedList.name}</Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">{items.length} {t('lists.itemsUnit')}</Typography>
<Chip
                  size="small"
                  label={getListCategoryLabel(selectedList.category, t)}
                  sx={{ mt: 1, fontSize: 12, fontWeight: 600 }}
                />
            </Card>
            <Card sx={{ mb: 2, p: 2 }}>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>{t('lists.progress')}</Typography>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                <Typography sx={{ fontWeight: 600 }}>{checkedCount} {t('lists.of')} {items.length} {t('lists.itemsUnit')}</Typography>
                <Typography sx={{ fontWeight: 600, color: 'primary.main' }}>
                  {items.length > 0 ? Math.round((checkedCount / items.length) * 100) : 0}%
                </Typography>
              </Box>
              <Box sx={{ height: 6, borderRadius: 3, bgcolor: 'action.hover', overflow: 'hidden' }}>
                <Box sx={{ height: '100%', width: `${items.length > 0 ? (checkedCount / items.length) * 100 : 0}%`, bgcolor: 'primary.main', transition: 'width 0.3s' }} />
              </Box>
            </Card>
            <Card sx={{ mb: 2, p: 2 }}>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>{t('lists.totals')}</Typography>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="body2">{t('lists.estimated')}</Typography>
                <Typography sx={{ fontWeight: 600 }}>{formatCurrency(totalEst, currency)}</Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="body2">{t('lists.spent')}</Typography>
                <Typography sx={{ fontWeight: 600, color: 'primary.main' }}>{formatCurrency(totalSpent, currency)}</Typography>
              </Box>
            </Card>
            <Button variant="outlined" fullWidth color="error" size="small" startIcon={<Archive />}
              onClick={() => { archiveList(selectedList); setSelectedList(null) }}>
              {t('lists.archivedList')}
            </Button>
            {items.length > 0 && (
              <Button variant="outlined" fullWidth color="warning" size="small" startIcon={<Delete />} sx={{ mt: 1 }}
                onClick={clearList}>
                {t('lists.clearList')}
              </Button>
            )}
            {checkedCount > 0 && (
              <Button variant="contained" fullWidth size="small" startIcon={<PointOfSale />} sx={{ mt: 2 }}
                onClick={() => setShowCheckout(true)}>
                {t('purch.checkout')}
              </Button>
            )}
          </Box>

          {/* Items */}
          <Box sx={{ flex: 1, minWidth: 300 }}>
            <Card sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">{t('lists.items')}</Typography>
                 <Button size="small" startIcon={<Edit />} onClick={() => { setEditListName(selectedList.name); setEditListCategory(selectedList.category || 'Geral'); setEditListType(selectedList.listType || 'PONTUAL'); setEditListGrocy(selectedList.grocyAssociated || false); setEditingList(selectedList) }}>
                  {t('lists.edit')}
                </Button>
              </Box>

              {/* Add item form */}
              <Box sx={{ display: 'flex', gap: 1, mb: 2, alignItems: 'center', flexWrap: 'wrap' }}>
                <TextField
                  placeholder={t('lists.addItem')}
                  value={newItemName}
                  onChange={(e) => setNewItemName(e.target.value)}
                  onKeyUp={(e) => e.key === 'Enter' && addItem()}
                  size="small"
                  sx={{ flex: 1, minWidth: 150 }}
                />
                <TextField
                  type="number"
                  label={t('lists.qty')}
                  value={newItemQty}
                  onChange={(e) => setNewItemQty(parseFloat(e.target.value) || 1)}
                  size="small"
                  sx={{ width: 70 }}
                />
                <TextField
                  select
                  value={newItemUnit}
                  onChange={(e) => setNewItemUnit(e.target.value)}
                  size="small"
                  sx={{ width: 80 }}
                >
{UNITS.map((u) => <MenuItem key={u} value={u}>{getUnitLabel(u)}</MenuItem>)}
                </TextField>
                <TextField
                  select
                  label={t('lists.categ')}
                  value={newItemCategory}
                  onChange={(e) => setNewItemCategory(e.target.value)}
                  size="small"
                  sx={{ width: 130 }}
                  slotProps={{ select: { renderValue: (v: unknown) => v ? getCategoryLabel(v as string, t) : t('lists.noCateg') } }}
                >
                  {getProductCategoriesForList(selectedList.category).map((c) => (
                    <MenuItem key={c.name} value={c.name}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        {c.icon}{getCategoryLabel(c.name, t)}
                      </Box>
                    </MenuItem>
                  ))}
                </TextField>
                <Button variant="contained" size="small" startIcon={<Add />} onClick={addItem} disabled={addingItem || !newItemName.trim()}>
                  {t('lists.add')}
                </Button>
              </Box>

              {/* Items list — grouped by category */}
              {items.length === 0 ? (
                <Typography color="text.secondary" sx={{ textAlign: 'center', py: 3 }}>
                  {t('lists.noItems')}
                </Typography>
              ) : (
                (() => {
                  const uncheckedItems = items.filter((i) => !i.checked)
                  const checkedItems = items.filter((i) => i.checked)

                  const grouped: Record<string, ShoppingListItem[]> = {}
                  uncheckedItems.forEach((item) => {
                    const cat = item.category || 'Sem Categoria'
                    if (!grouped[cat]) grouped[cat] = []
                    grouped[cat].push(item)
                  })

                  return (
                    <>
                      {/* Unchecked items — grouped by category */}
                      {getProductCategoriesForList(selectedList.category)
                        .filter((c) => grouped[c.name])
                        .map((cat) => (
                          <Box key={cat.name} sx={{ mb: 2 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1, color: 'text.secondary' }}>
                              <Box sx={{ display: 'flex', alignItems: 'center' }}>{cat.icon}</Box>
                              <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'uppercase', fontSize: 12, letterSpacing: 0.5 }}>
                                {getCategoryLabel(cat.name, t)} ({grouped[cat.name].length})
                              </Typography>
                            </Box>
                            {grouped[cat.name].map((item) => (
                              <Box key={item.id} sx={{ display: 'flex', alignItems: 'center', gap: 1.5, py: 1, borderBottom: '1px solid', borderColor: 'divider' }}>
                                <Box
                                  onClick={() => toggleCheck(item)}
                                  sx={{
                                    width: 24, height: 24, borderRadius: '50%', border: '2px solid',
                                    borderColor: item.checked ? 'primary.main' : 'divider',
                                    bgcolor: item.checked ? 'primary.main' : 'transparent',
                                    flexShrink: 0, cursor: 'pointer',
                                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                                    '&:hover': { borderColor: 'primary.main' },
                                  }}
                                >
                                  {item.checked && <Check sx={{ fontSize: 14, color: 'primary.contrastText' }} />}
                                </Box>
                                <Box sx={{ flex: 1 }}>
                                  <Typography sx={{
                                    fontSize: 14, fontWeight: 500,
                                    textDecoration: item.checked ? 'line-through' : 'none',
                                    color: item.checked ? 'text.secondary' : 'text.primary',
                                  }}>
                                    {item.name}
                                  </Typography>
                                  <Typography variant="caption" color="text.secondary">
                                    {item.quantity} {item.unit}
                                    {item.estimatedPrice ? ` · ${formatCurrency(item.estimatedPrice * item.quantity, currency)}` : ''}
                                  </Typography>
                                </Box>
                                <IconButton size="small" onClick={() => startEditItem(item)}><Edit fontSize="small" /></IconButton>
                                <IconButton size="small" color="error" onClick={() => deleteItem(item)}><Delete fontSize="small" /></IconButton>
                              </Box>
                            ))}
                          </Box>
                        ))}

                      {/* Checked items — Comprados section at bottom */}
                      {checkedItems.length > 0 && (
                        <Box key="comprados" sx={{ mb: 2, pt: 2, borderTop: '2px dashed', borderColor: 'divider' }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1, color: 'text.secondary' }}>
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                              <Check sx={{ fontSize: 20, color: 'primary.main' }} />
                            </Box>
                            <Typography variant="body2" sx={{ fontWeight: 600, textTransform: 'uppercase', fontSize: 12, letterSpacing: 0.5 }}>
                              {t('cat.Comprados')} ({checkedItems.length})
                            </Typography>
                          </Box>
                          {checkedItems.map((item) => (
                            <Box key={item.id} sx={{ display: 'flex', alignItems: 'center', gap: 1.5, py: 1, borderBottom: '1px solid', borderColor: 'divider', opacity: 0.6 }}>
                              <Box
                                onClick={() => toggleCheck(item)}
                                sx={{
                                  width: 24, height: 24, borderRadius: '50%', border: '2px solid',
                                  borderColor: 'primary.main',
                                  bgcolor: 'primary.main',
                                  flexShrink: 0, cursor: 'pointer',
                                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                                  '&:hover': { borderColor: 'primary.main' },
                                }}
                              >
                                <Check sx={{ fontSize: 14, color: 'primary.contrastText' }} />
                              </Box>
                              <Box sx={{ flex: 1 }}>
                                <Typography sx={{
                                  fontSize: 14, fontWeight: 500,
                                  textDecoration: 'line-through',
                                  color: 'text.secondary',
                                }}>
                                  {item.name}
                                </Typography>
                                <Typography variant="caption" color="text.secondary">
                                  {item.quantity} {item.unit}
                                  {item.estimatedPrice ? ` · ${formatCurrency(item.estimatedPrice * item.quantity, currency)}` : ''}
                                </Typography>
                              </Box>
                              <IconButton size="small" onClick={() => startEditItem(item)}><Edit fontSize="small" /></IconButton>
                              <IconButton size="small" color="error" onClick={() => deleteItem(item)}><Delete fontSize="small" /></IconButton>
                            </Box>
                          ))}
                        </Box>
                      )}
                    </>
                  )
                })()
              )}
            </Card>
          </Box>
        </Box>

        {/* Edit list name dialog */}
        <Dialog open={!!editingList} onClose={() => setEditingList(null)} maxWidth="xs" fullWidth>
          <DialogTitle>{t('lists.editList')}</DialogTitle>
  <DialogContent>
    <TextField label={t('lists.listName')} fullWidth value={editListName} onChange={(e) => setEditListName(e.target.value)} sx={{ mt: 1 }}
      onKeyUp={(e) => e.key === 'Enter' && saveEditList()} />
    <TextField
      select
      label={t('lists.category')}
      value={editListCategory}
      onChange={(e) => setEditListCategory(e.target.value)}
      fullWidth
      sx={{ mt: 1 }}
    >
{LIST_CATEGORIES.map((cat) => (
              <MenuItem key={cat.name} value={cat.name}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  {cat.icon}{getListCategoryLabel(cat.name, t)}
                </Box>
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label={t('lists.type')}
            value={editListType}
            onChange={(e) => setEditListType(e.target.value as any)}
            fullWidth
            sx={{ mt: 1 }}
          >
            <MenuItem value="PONTUAL">{t('lists.type.pontual')}</MenuItem>
            <MenuItem value="RECORRENTE">{t('lists.type.recorrente')}</MenuItem>
            <MenuItem value="MODELO">{t('lists.type.modelo')}</MenuItem>
          </TextField>
          <TextField
            select
            label={t('lists.grocy')}
            value={editListGrocy}
            onChange={(e) => setEditListGrocy(e.target.value === 'true')}
            fullWidth
            sx={{ mt: 1 }}
            slotProps={{ select: { renderValue: (v: unknown) => (v as boolean) ? t('lists.grocyYes') : t('lists.grocyNo') } }}
          >
            <MenuItem value={String(false)}>{t('lists.grocyNo')}</MenuItem>
            <MenuItem value={String(true)}>{t('lists.grocyYes')}</MenuItem>
          </TextField>
        </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditingList(null)}>{t('lists.cancel')}</Button>
            <Button variant="contained" onClick={saveEditList}>{t('lists.save')}</Button>
          </DialogActions>
        </Dialog>

        {/* Edit item dialog */}
        <Dialog open={!!editingItem} onClose={() => setEditingItem(null)} maxWidth="xs" fullWidth>
          <DialogTitle>{t('lists.editItem')}</DialogTitle>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '16px !important' }}>
            <TextField label={t('lists.itemName')} value={editItemName} onChange={(e) => setEditItemName(e.target.value)} fullWidth />
            <Box sx={{ display: 'flex', gap: 1 }}>
              <TextField type="number" label={t('lists.qty')} value={editItemQty} onChange={(e) => setEditItemQty(parseFloat(e.target.value) || 1)} sx={{ width: 100 }} />
              <TextField select label={t('lists.unit')} value={editItemUnit} onChange={(e) => setEditItemUnit(e.target.value)} sx={{ flex: 1 }}>
                {UNITS.map((u) => <MenuItem key={u} value={u}>{getUnitLabel(u)}</MenuItem>)}
              </TextField>
            </Box>
            <TextField label={t('lists.estimatedPrice') + ' (' + getCurrencySymbol(currency) + ')'} type="number" value={editItemPrice} onChange={(e) => setEditItemPrice(e.target.value)} fullWidth />
            <TextField
              select
              label={t('lists.category')}
              value={editItemCategory}
              onChange={(e) => setEditItemCategory(e.target.value)}
              fullWidth
            >
              {getProductCategoriesForList(selectedList.category).map((c) => (
                <MenuItem key={c.name} value={c.name}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>{c.icon}{getCategoryLabel(c.name, t)}</Box>
                </MenuItem>
              ))}
            </TextField>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditingItem(null)}>{t('lists.cancel')}</Button>
            <Button variant="contained" onClick={saveEditItem}>{t('lists.save')}</Button>
          </DialogActions>
        </Dialog>

        <Snackbar open={!!snack} autoHideDuration={3000} onClose={() => setSnack(null)} message={snack}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }} />

        {/* Checkout dialog */}
        <Dialog open={showCheckout} onClose={() => setShowCheckout(false)} maxWidth="xs" fullWidth>
          <DialogTitle>{t('purch.checkoutTitle')}</DialogTitle>
          <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '16px !important' }}>
            <Typography variant="body2" color="text.secondary">
              {t('purch.checkoutConfirm', { count: checkedCount })}
            </Typography>
            <TextField
              select
              label={t('purch.payment')}
              value={checkoutPayment}
              onChange={(e) => setCheckoutPayment(e.target.value)}
              fullWidth
            >
              <MenuItem value="">{t('purch.noPayment')}</MenuItem>
              {['DEBITO', 'CREDITO', 'DINHEIRO', 'PIX', 'VR', 'VA'].map((pm) => (
                <MenuItem key={pm} value={pm}>{t(`pay.${pm.toLowerCase()}`)}</MenuItem>
              ))}
            </TextField>
            <TextField
              label={`${t('purch.total')} (${getCurrencySymbol(currency)})`}
              type="number"
              value={checkoutTotal}
              onChange={(e) => setCheckoutTotal(e.target.value)}
              fullWidth
              helperText={t('purch.totalHelper')}
            />
            <TextField
              label={t('purch.notes')}
              value={checkoutNotes}
              onChange={(e) => setCheckoutNotes(e.target.value)}
              fullWidth
              multiline
              rows={2}
            />
            {selectedList.grocyAssociated && (
              <TextField
                select
                label="Grocy"
                value={checkoutGrocy ? 'yes' : 'no'}
                onChange={(e) => setCheckoutGrocy(e.target.value === 'yes')}
                fullWidth
              >
                <MenuItem value="yes">{t('purch.grocySyncYes')}</MenuItem>
                <MenuItem value="no">{t('purch.grocySyncNo')}</MenuItem>
              </TextField>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setShowCheckout(false)}>{t('purch.cancel')}</Button>
            <Button variant="contained" onClick={doCheckout} disabled={checkingOut || checkedCount === 0}>
              {checkingOut ? t('purch.saving') : t('purch.checkout')}
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    )
  }

  // ===== LIST VIEW =====
  return (
    <Box sx={{ maxWidth: 1200 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ mb: 0.5 }}>{t('lists.title')}</Typography>
          <Typography color="text.secondary">{t('lists.subtitle')}</Typography>
        </Box>
        <Button variant="contained" startIcon={<Add />} onClick={() => setShowCreate(true)}>{t('lists.new')}</Button>
      </Box>

      <Dialog open={showCreate} onClose={() => setShowCreate(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{t('lists.new')}</DialogTitle>
        <DialogContent>
          <TextField label={t('lists.listName')} fullWidth sx={{ mt: 1 }} value={newListName} onChange={(e) => setNewListName(e.target.value)}
            onKeyUp={(e) => e.key === 'Enter' && createList()} autoFocus />
          <TextField
            select
            label={t('lists.category')}
            value={newListCategory}
            onChange={(e) => setNewListCategory(e.target.value)}
            fullWidth
            sx={{ mt: 1 }}
          >
            {LIST_CATEGORIES.map((cat) => (
              <MenuItem key={cat.name} value={cat.name}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  {cat.icon}{cat.label}
                </Box>
              </MenuItem>
            ))}
          </TextField>
          <TextField
            select
            label={t('lists.type')}
            value={newListType}
            onChange={(e) => setNewListType(e.target.value as any)}
            fullWidth
            sx={{ mt: 1 }}
          >
            <MenuItem value="PONTUAL">{t('lists.type.pontual')}</MenuItem>
            <MenuItem value="RECORRENTE">{t('lists.type.recorrente')}</MenuItem>
            <MenuItem value="MODELO">{t('lists.type.modelo')}</MenuItem>
          </TextField>
          <TextField
            select
            label={t('lists.grocy')}
            value={newListGrocy}
            onChange={(e) => setNewListGrocy(e.target.value === 'true')}
            fullWidth
            sx={{ mt: 1 }}
            slotProps={{ select: { renderValue: (v: unknown) => (v as boolean) ? t('lists.grocyYes') : t('lists.grocyNo') } }}
          >
            <MenuItem value={String(false)}>{t('lists.grocyNo')}</MenuItem>
            <MenuItem value={String(true)}>{t('lists.grocyYes')}</MenuItem>
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowCreate(false)}>{t('lists.cancel')}</Button>
          <Button variant="contained" onClick={createList} disabled={!newListName.trim()}>{t('lists.create')}</Button>
        </DialogActions>
      </Dialog>

      {/* Listas ativas */}
      <Card sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>{t('lists.active')} ({lists.length})</Typography>
        </Box>
        {lists.length === 0 ? (
          <Box sx={{ p: 4, textAlign: 'center' }}>
            <ShoppingCart sx={{ fontSize: 48, color: 'text.disabled', mb: 1 }} />
            <Typography color="text.secondary">{t('lists.noActive')}</Typography>
          </Box>
        ) : (
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>{t('lists.col.name')}</TableCell>
                  <TableCell>{t('lists.col.category')}</TableCell>
                  <TableCell>{t('lists.col.items')}</TableCell>
                  <TableCell>{t('lists.col.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {lists.map((list) => (
                  <TableRow
                    key={list.id}
                    hover
                    onClick={() => setSelectedList(list)}
                    sx={{ cursor: 'pointer' }}
                  >
                    <TableCell sx={{ fontWeight: 500 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', color: 'text.secondary' }}>{getListCategoryIcon(list.category)}</Box>
                        {list.name}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={getListCategoryLabel(list.category, t)}
                        sx={{ fontSize: 12, fontWeight: 600 }}
                      />
                    </TableCell>
                    <TableCell>{list.items?.length || 0}</TableCell>
                    <TableCell onClick={(e) => e.stopPropagation()}>
                       <IconButton size="small" onClick={() => { setEditListName(list.name); setEditListCategory(list.category || 'Geral'); setEditListType(list.listType || 'PONTUAL'); setEditListGrocy(list.grocyAssociated || false); setEditingList(list) }}>
                        <Edit fontSize="small" />
                      </IconButton>
                      <IconButton size="small" onClick={() => archiveList(list)} title={t('lists.archive')}><Archive fontSize="small" /></IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </Card>

      {/* Listas arquivadas */}
      {history.length > 0 && (
        <Card>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
            <Typography variant="h6" sx={{ fontWeight: 600 }}>{t('lists.archived')} ({history.length})</Typography>
          </Box>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>{t('lists.col.name')}</TableCell>
                  <TableCell>{t('lists.col.category')}</TableCell>
                  <TableCell>{t('lists.col.items')}</TableCell>
                  <TableCell>{t('lists.col.actions')}</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {history.map((list) => (
                  <TableRow key={list.id} hover>
                    <TableCell sx={{ fontWeight: 500 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', color: 'text.secondary' }}>{getListCategoryIcon(list.category)}</Box>
                        {list.name}
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        label={getListCategoryLabel(list.category, t)}
                        sx={{ fontSize: 12, fontWeight: 600 }}
                      />
                    </TableCell>
                    <TableCell>{list.items?.length || 0}</TableCell>
                    <TableCell>
                      <IconButton size="small" onClick={() => unarchiveList(list)} title={t('lists.restore')}><Unarchive fontSize="small" /></IconButton>
                      <IconButton size="small" color="error" onClick={() => deleteList(list)} title={t('lists.delete')}><Delete fontSize="small" /></IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Card>
      )}

      {/* Edit list name dialog (also accessible from list view) */}
      <Dialog open={!!editingList} onClose={() => setEditingList(null)} maxWidth="xs" fullWidth>
        <DialogTitle>{t('lists.editList')}</DialogTitle>
        <DialogContent>
          <TextField label={t('lists.listName')} fullWidth value={editListName} onChange={(e) => setEditListName(e.target.value)} sx={{ mt: 1 }}
            onKeyUp={(e) => e.key === 'Enter' && saveEditList()} autoFocus />
          <TextField
            select
            label={t('lists.category')}
            value={editListCategory}
            onChange={(e) => setEditListCategory(e.target.value)}
            fullWidth
            sx={{ mt: 1 }}
          >
            {LIST_CATEGORIES.map((cat) => (
              <MenuItem key={cat.name} value={cat.name}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  {cat.icon}{cat.label}
                </Box>
              </MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditingList(null)}>{t('lists.cancel')}</Button>
          <Button variant="contained" onClick={saveEditList}>{t('lists.save')}</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={!!snack} autoHideDuration={3000} onClose={() => setSnack(null)} message={snack}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }} />
    </Box>
  )
}
