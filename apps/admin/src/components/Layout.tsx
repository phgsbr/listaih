import { useState, useRef, useEffect } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  IconButton,
  Avatar,
  Chip,
  Divider,
  useMediaQuery,
  useTheme,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Select,
  InputLabel,
  FormControl,
} from '@mui/material'
import {
  Dashboard as DashboardIcon,
  ShoppingCart,
  Groups,
  Smartphone,
  Extension,
  Settings as SettingsIcon,
  Menu as MenuIcon,
  DarkMode,
  LightMode,
  Logout,
  Person,
  Lock,
  PhotoCamera,
  Language,
} from '@mui/icons-material'
import { useAuth } from '@/hooks/useAuth'
import { useHousehold } from '@/hooks/useHousehold'
import { useI18n, type Lang } from '@/hooks/useI18n'
import api from '@/services/api'

const LANGUAGES = [
  { code: 'pt-BR', label: 'Português (Brasil)' },
  { code: 'en-US', label: 'English (US)' },
  { code: 'es-ES', label: 'Español' },
]

const SIDEBAR_WIDTH = 260

interface LayoutProps {
  mode: 'light' | 'dark'
  onToggleTheme: () => void
}

export default function Layout({ mode, onToggleTheme }: LayoutProps) {
  const navigate = useNavigate()
  const location = useLocation()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [drawerOpen, setDrawerOpen] = useState(false)
  const { user, logout, updateUser } = useAuth()
  const { activeHousehold } = useHousehold()
  const { lang, setLang, t } = useI18n()

  const NAV_ITEMS = [
    { label: t('nav.dashboard'), icon: <DashboardIcon />, path: '/admin' },
    { label: t('nav.lists'), icon: <ShoppingCart />, path: '/admin/lists' },
    { label: t('nav.members'), icon: <Groups />, path: '/admin/members' },
    { label: t('nav.clients'), icon: <Smartphone />, path: '/admin/clients' },
    { label: t('nav.integrations'), icon: <Extension />, path: '/admin/integrations' },
    { label: t('nav.settings'), icon: <SettingsIcon />, path: '/admin/settings' },
  ]

  // Avatar menu + profile dialog
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null)
  const [pwOpen, setPwOpen] = useState(false)
  const [currentPw, setCurrentPw] = useState('')
  const [newPw, setNewPw] = useState('')
  const [confirmPw, setConfirmPw] = useState('')
  const [pwError, setPwError] = useState('')
  const [savingPw, setSavingPw] = useState(false)
  const [profileName, setProfileName] = useState('')
  const [profileAvatar, setProfileAvatar] = useState('')
  const [savingProfile, setSavingProfile] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)

  useEffect(() => {
    if (user) {
      setProfileName(user.name)
      setProfileAvatar((user as any).avatar || '')
    }
  }, [user])

  const initials = user?.name ? user.name.split(' ').map((n) => n[0]).join('').slice(0, 2).toUpperCase() : '?'

  const handleMenu = (e: React.MouseEvent<HTMLElement>) => setAnchorEl(e.currentTarget)
  const handleClose = () => setAnchorEl(null)

  const saveProfile = async () => {
    setSavingProfile(true)
    try {
      const updatedUser = await api.put('/users/me', { name: profileName, avatar: profileAvatar || undefined })
      updateUser(updatedUser.data)
      setProfileOpen(false)
    } catch {}
    setSavingProfile(false)
  }

  const changePassword = async () => {
    setPwError('')
    if (newPw.length < 6) { setPwError(t('pw.tooShort')); return }
    if (newPw !== confirmPw) { setPwError(t('pw.mismatch')); return }
    setSavingPw(true)
    try {
      await api.post('/auth/change-password', { currentPassword: currentPw, newPassword: newPw })
      setPwOpen(false)
      setCurrentPw(''); setNewPw(''); setConfirmPw('')
    } catch {
      setPwError(t('pw.incorrect'))
    } finally {
      setSavingPw(false)
    }
  }

  const drawer = (
    <Box sx={{ width: SIDEBAR_WIDTH, height: '100%', display: 'flex', flexDirection: 'column', bgcolor: mode === 'dark' ? '#111410' : '#1A1C19', color: '#E3E3E0' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, px: 2.5, py: 2, borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
        <Box sx={{ width: 36, height: 36, flexShrink: 0 }}>
          <img src="./logo.svg" alt="Listaih" width="36" height="36" />
        </Box>
        <Box>
          <Typography sx={{ fontSize: 18, fontWeight: 700 }}>Listaih</Typography>
          <Typography sx={{ fontSize: 12, fontWeight: 400, opacity: 0.5 }}>{activeHousehold?.name || t('layout.home')}</Typography>
        </Box>
      </Box>

      <List sx={{ flex: 1, px: 0, py: 1.5, overflowY: 'auto', '&::-webkit-scrollbar': { width: 0 } }}>
        {NAV_ITEMS.map((item) => {
          const active = item.path === '/admin' ? location.pathname === '/admin' : location.pathname.startsWith(item.path)
          return (
            <ListItem key={item.path} disablePadding>
              <ListItemButton
                onClick={() => { navigate(item.path); if (isMobile) setDrawerOpen(false) }}
                sx={{
                  mx: 1,
                  borderRadius: 2,
                  py: 1.5,
                  bgcolor: active ? 'rgba(125,217,164,0.12)' : 'transparent',
                  borderLeft: active ? '3px solid #7DD9A4' : '3px solid transparent',
                  '&:hover': { bgcolor: 'rgba(255,255,255,0.06)' },
                }}
              >
                <ListItemIcon sx={{ minWidth: 36, color: active ? '#7DD9A4' : '#E3E3E0' }}>{item.icon}</ListItemIcon>
                <ListItemText
                  primary={item.label}
                  sx={{ '& .MuiListItemText-primary': { color: active ? '#7DD9A4' : '#E3E3E0', fontSize: 14, fontWeight: 500 } }}
                />
              </ListItemButton>
            </ListItem>
          )
        })}
      </List>

      <Box sx={{ px: 2, py: 1.5, borderTop: '1px solid rgba(255,255,255,0.08)' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
          <Language sx={{ fontSize: 18, opacity: 0.6 }} />
          <Select
            size="small"
            value={lang}
            onChange={(e) => setLang(e.target.value as Lang)}
            variant="outlined"
            fullWidth
            sx={{
              fontSize: 12,
              color: '#E3E3E0',
              '& .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.15)' },
              '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255,255,255,0.3)' },
              '& .MuiSelect-icon': { color: 'rgba(255,255,255,0.5)' },
            }}
          >
            {LANGUAGES.map((l) => (
              <MenuItem key={l.code} value={l.code} sx={{ fontSize: 13 }}>
                {l.label}
              </MenuItem>
            ))}
          </Select>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, fontSize: 12, opacity: 0.6 }}>
          <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#4CAF50', animation: 'pulse 2s infinite' }} />
          {t('layout.online')}
        </Box>
      </Box>
    </Box>
  )

  const currentPage = NAV_ITEMS.find((n) => n.path === '/admin' ? location.pathname === '/admin' : location.pathname.startsWith(n.path))

  return (
    <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      {!isMobile && (
        <Box component="nav" sx={{ width: SIDEBAR_WIDTH, flexShrink: 0 }}>
          {drawer}
        </Box>
      )}
      <Drawer
        variant="temporary"
        open={isMobile && drawerOpen}
        onClose={() => setDrawerOpen(false)}
        ModalProps={{ keepMounted: true }}
        sx={{ display: isMobile ? 'block' : 'none', '& .MuiDrawer-paper': { width: SIDEBAR_WIDTH } }}
      >
        {drawer}
      </Drawer>

      <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <AppBar position="static" elevation={0} sx={{ bgcolor: 'background.paper', borderBottom: '1px solid', borderColor: 'divider', height: 64, justifyContent: 'center' }}>
          <Toolbar sx={{ gap: 2 }}>
            {isMobile && (
              <IconButton edge="start" onClick={() => setDrawerOpen(true)}>
                <MenuIcon />
              </IconButton>
            )}
            <Typography sx={{ flex: 1, fontSize: 20, fontWeight: 600 }}>{currentPage?.label || 'Listaih'}</Typography>
            <Box sx={{ display: { xs: 'none', md: 'flex' }, gap: 1 }}>
              <Chip size="small" label="PostgreSQL" color="success" variant="outlined" sx={{ fontSize: 12 }} />
              <Chip size="small" label="Redis" color="warning" variant="outlined" sx={{ fontSize: 12 }} />
            </Box>
            <IconButton onClick={onToggleTheme}>{mode === 'dark' ? <LightMode /> : <DarkMode />}</IconButton>
            <IconButton onClick={handleMenu} sx={{ p: 0.5 }}>
              <Avatar sx={{ bgcolor: 'primary.main', width: 40, height: 40, fontSize: 14, fontWeight: 600 }}>
                {(user as any)?.avatar ? <Box component="img" src={(user as any).avatar} sx={{ width: 40, height: 40, borderRadius: '50%', objectFit: 'cover' }} /> : initials}
              </Avatar>
            </IconButton>
            <Menu
              anchorEl={anchorEl}
              open={!!anchorEl}
              onClose={handleClose}
              anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              transformOrigin={{ vertical: 'top', horizontal: 'right' }}
              slotProps={{ paper: { sx: { minWidth: 220 } } }}
            >
              <Box sx={{ px: 2, py: 1.5 }}>
                <Typography sx={{ fontWeight: 600 }}>{user?.name || t('layout.user')}</Typography>
                <Typography variant="body2" color="text.secondary">{user?.email}</Typography>
              </Box>
              <Divider />
              <MenuItem onClick={() => { handleClose(); setProfileOpen(true) }}>
                <ListItemIcon><Person fontSize="small" /></ListItemIcon>
                {t('layout.menu.profile')}
              </MenuItem>
              <MenuItem onClick={() => { handleClose(); setPwOpen(true) }}>
                <ListItemIcon><Lock fontSize="small" /></ListItemIcon>
                {t('layout.menu.password')}
              </MenuItem>
              <Divider />
              <MenuItem onClick={() => { handleClose(); logout() }} sx={{ color: 'error.main' }}>
                <ListItemIcon><Logout fontSize="small" sx={{ color: 'error.main' }} /></ListItemIcon>
                {t('layout.menu.logout')}
              </MenuItem>
            </Menu>
          </Toolbar>
        </AppBar>
        <Box component="main" sx={{ flex: 1, overflow: 'auto', p: 3 }}>
          <Outlet />
        </Box>
      </Box>

      {/* Edit profile dialog */}
      <Dialog open={profileOpen} onClose={() => setProfileOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{t('profile.title')}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1, mb: 2, mt: 1 }}>
            <Avatar sx={{ bgcolor: 'primary.main', width: 80, height: 80, fontSize: 28, fontWeight: 700 }}>
              {profileAvatar ? <Box component="img" src={profileAvatar} sx={{ width: 80, height: 80, borderRadius: '50%', objectFit: 'cover' }} /> : initials}
            </Avatar>
            <Button component="label" variant="outlined" size="small" startIcon={<PhotoCamera />}>
              {t('profile.changePhoto')}
              <input type="file" accept="image/*" hidden onChange={(e) => {
                const file = e.target.files?.[0]
                if (file) {
                  const img = new Image()
                  const reader = new FileReader()
                  reader.onload = () => { img.src = reader.result as string }
                  reader.readAsDataURL(file)
                  img.onload = () => {
                    const canvas = document.createElement('canvas')
                    const size = 256
                    canvas.width = size
                    canvas.height = size
                    const ctx = canvas.getContext('2d')!
                    const min = Math.min(img.width, img.height)
                    const sx = (img.width - min) / 2
                    const sy = (img.height - min) / 2
                    ctx.drawImage(img, sx, sy, min, min, 0, 0, size, size)
                    setProfileAvatar(canvas.toDataURL('image/jpeg', 0.85))
                  }
                }
              }} />
            </Button>
          </Box>
          <TextField label={t('profile.name')} value={profileName} onChange={(e) => setProfileName(e.target.value)} fullWidth />
          <TextField label={t('profile.email')} value={user?.email || ''} disabled fullWidth sx={{ mt: 2 }} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setProfileOpen(false)}>{t('profile.cancel')}</Button>
          <Button variant="contained" onClick={saveProfile} disabled={savingProfile || !profileName.trim()}>
            {savingProfile ? t('profile.saving') : t('profile.save')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Change password dialog */}
      <Dialog open={pwOpen} onClose={() => setPwOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>{t('pw.title')}</DialogTitle>
        <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: '16px !important' }}>
          <TextField label={t('pw.current')} type="password" value={currentPw} onChange={(e) => setCurrentPw(e.target.value)} fullWidth />
          <TextField label={t('pw.new')} type="password" value={newPw} onChange={(e) => setNewPw(e.target.value)} fullWidth />
          <TextField label={t('pw.confirm')} type="password" value={confirmPw} onChange={(e) => setConfirmPw(e.target.value)} fullWidth />
          {pwError && <Typography color="error" variant="body2">{pwError}</Typography>}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPwOpen(false)}>{t('profile.cancel')}</Button>
          <Button variant="contained" onClick={changePassword} disabled={savingPw || !currentPw || !newPw}>
            {savingPw ? t('pw.saving') : t('pw.change')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
