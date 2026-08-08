import { useState, useEffect, useCallback } from 'react'
import api from '@/services/api'

export interface Household {
  id: string
  name: string
  inviteCode: string
  role: string
  members: HouseholdMember[]
}

export interface HouseholdMember {
  id: string
  householdId: string
  userId: string
  role: string
  joinedAt: string
  user: { id: string; name: string; avatar: string | null }
}

export interface ShoppingListItem {
  id: string
  listId: string
  name: string
  quantity: number
  unit: string
  estimatedPrice: number | null
  actualPrice: number | null
  category: string | null
  notes: string | null
  position: number
  checked: boolean
  checkedById: string | null
  checkedAt: string | null
  addedById: string
  addedAt: string
  updatedAt: string
}

export interface ShoppingList {
  id: string
  name: string
  householdId: string
  template: boolean
  archivedAt: string | null
  createdAt: string
  updatedAt: string
  category: string | null
  items?: ShoppingListItem[]
}

export function useHousehold() {
  const [households, setHouseholds] = useState<Household[]>([])
  const [activeHousehold, setActiveHousehold] = useState<Household | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    try {
      const res = await api.get('/users/households')
      setHouseholds(res.data)
      const stored = localStorage.getItem('listaih_active_household')
      const found = stored ? res.data.find((h: Household) => h.id === stored) : null
      setActiveHousehold(found || res.data[0] || null)
    } catch {
      setHouseholds([])
      setActiveHousehold(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const switchHousehold = (id: string) => {
    const h = households.find((h) => h.id === id)
    if (h) {
      setActiveHousehold(h)
      localStorage.setItem('listaih_active_household', h.id)
    }
  }

  return { households, activeHousehold, switchHousehold, loading, reload: load }
}
