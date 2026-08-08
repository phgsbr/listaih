import { useState } from 'react'
import { Box, Card, Typography, Button, Chip, IconButton, Avatar, Divider, Menu, MenuItem, Dialog, DialogTitle, DialogContent, DialogActions, Snackbar, Alert } from '@mui/material'
import { PersonAdd, MoreVert, ContentCopy, Check } from '@mui/icons-material'
import api from '@/services/api'
import { useHousehold, type HouseholdMember } from '@/hooks/useHousehold'
import { useI18n } from '@/hooks/useI18n'

export default function Members() {
  const { t } = useI18n()
  const { activeHousehold, reload } = useHousehold()
  const members: HouseholdMember[] = activeHousehold?.members || []

  const ROLE_LABELS: Record<string, { label: string; color: 'primary' | 'secondary' | 'default' }> = {
    ADMIN: { label: t('members.role.admin'), color: 'primary' },
    EDITOR: { label: t('members.role.editor'), color: 'secondary' },
    VIEWER: { label: t('members.role.viewer'), color: 'default' },
  }

  const [anchorEl, setAnchorEl] = useState<(EventTarget & HTMLElement) | null>(null)
  const [selectedMember, setSelectedMember] = useState<HouseholdMember | null>(null)
  const [snack, setSnack] = useState<string | null>(null)
  const [copied, setCopied] = useState(false)

  const copyInviteCode = () => {
    if (activeHousehold) {
      navigator.clipboard.writeText(activeHousehold.inviteCode)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  const changeRole = async (member: HouseholdMember, role: string) => {
    try {
      await api.patch(`/users/households/${activeHousehold?.id}/members/${member.id}`, { role })
      await reload()
      setSnack(t('members.roleUpdated'))
    } catch {
      setSnack(t('members.roleError'))
    }
    setAnchorEl(null)
    setSelectedMember(null)
  }

  const removeMember = async (member: HouseholdMember) => {
    try {
      await api.delete(`/users/households/${activeHousehold?.id}/members/${member.id}`)
      await reload()
      setSnack(t('members.removed'))
    } catch {
      setSnack(t('members.removeError'))
    }
    setAnchorEl(null)
    setSelectedMember(null)
  }

  return (
    <Box sx={{ maxWidth: 1200 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ mb: 0.5 }}>{t('members.title')}</Typography>
          <Typography color="text.secondary">{t('members.subtitle')}</Typography>
        </Box>
        <Button variant="contained" startIcon={<PersonAdd />} onClick={copyInviteCode}>{t('members.invite')}</Button>
      </Box>

      <Card sx={{ mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 2.5, borderBottom: '1px solid', borderColor: 'divider' }}>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>{activeHousehold?.name || 'Casa'} · {members.length} {t('members.members')}</Typography>
        </Box>
        {members.length === 0 ? (
          <Box sx={{ p: 4, textAlign: 'center' }}>
            <Typography color="text.secondary">{t('members.none')}</Typography>
          </Box>
        ) : (
          members.map((m, i) => {
            const initials = m.user.name?.split(' ').map((n) => n[0]).join('').slice(0, 2).toUpperCase() || '?'
            const role = ROLE_LABELS[m.role] || ROLE_LABELS.VIEWER
            return (
              <Box key={m.id}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 2.5 }}>
                  <Avatar sx={{ bgcolor: 'primary.main', width: 44, height: 44, fontWeight: 600 }}>
                    {m.user.avatar ? <Box component="img" src={m.user.avatar} sx={{ width: 44, height: 44, borderRadius: '50%', objectFit: 'cover' }} /> : initials}
                  </Avatar>
                  <Box sx={{ flex: 1 }}>
                    <Typography sx={{ fontWeight: 600 }}>{m.user.name}</Typography>
                    <Typography variant="body2" color="text.secondary">{t('members.joined')} {new Date(m.joinedAt).toLocaleDateString('pt-BR')}</Typography>
                  </Box>
                  <Chip label={role.label} size="small" color={role.color as any} sx={{ fontSize: 12, fontWeight: 600 }} />
                  <IconButton
                    onClick={(e) => { setAnchorEl(e.currentTarget); setSelectedMember(m) }}
                  ><MoreVert /></IconButton>
                </Box>
                {i < members.length - 1 && <Divider />}
              </Box>
            )
          })
        )}
      </Card>

      {activeHousehold && (
        <Card sx={{ p: 3 }}>
          <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>{t('members.inviteCode')}</Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Typography sx={{ fontFamily: 'monospace', fontSize: 24, fontWeight: 700, letterSpacing: 2, color: 'primary.main', bgcolor: 'action.hover', p: 1.5, px: 3, borderRadius: 2 }}>
              {activeHousehold.inviteCode}
            </Typography>
            <Button variant="outlined" size="small" startIcon={copied ? <Check /> : <ContentCopy />} onClick={copyInviteCode}>
              {copied ? t('members.copied') : t('members.copy')}
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>{t('members.shareCode')}</Typography>
        </Card>
      )}

      <Menu
        anchorEl={anchorEl}
        open={!!anchorEl}
        onClose={() => { setAnchorEl(null); setSelectedMember(null) }}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Box sx={{ px: 2, py: 1 }}>
          <Typography sx={{ fontWeight: 600 }}>{selectedMember?.user.name}</Typography>
        </Box>
        <Divider />
        <Typography sx={{ px: 2, pt: 1, fontSize: 12, color: 'text.secondary' }}>{t('members.changeRole')}</Typography>
        <MenuItem onClick={() => selectedMember && changeRole(selectedMember, 'ADMIN')} disabled={selectedMember?.role === 'ADMIN'}>
          {t('members.adminFull')}
        </MenuItem>
        <MenuItem onClick={() => selectedMember && changeRole(selectedMember, 'EDITOR')} disabled={selectedMember?.role === 'EDITOR'}>
          {t('members.editorFull')}
        </MenuItem>
        <MenuItem onClick={() => selectedMember && changeRole(selectedMember, 'VIEWER')} disabled={selectedMember?.role === 'VIEWER'}>
          {t('members.viewerFull')}
        </MenuItem>
        <Divider />
        <MenuItem onClick={() => selectedMember && removeMember(selectedMember)} sx={{ color: 'error.main' }}>
          {t('members.remove')}
        </MenuItem>
      </Menu>

      <Snackbar open={!!snack} autoHideDuration={3000} onClose={() => setSnack(null)} message={snack}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }} />
    </Box>
  )
}
