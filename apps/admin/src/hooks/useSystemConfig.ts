import { useState, useEffect, useCallback } from 'react'
import api from '@/services/api'

export interface SystemConfig {
  isSetup: boolean
  installedAt: string | null
  currency: string
  grocyUrl: string | null
  grocyApiKey: string | null
  grocyEnabled: boolean
  haUrl: string | null
  haWebhookToken: string | null
  haEnabled: boolean
}

const CURRENCY_SYMBOLS: Record<string, string> = {
  BRL: 'R$',
  USD: '$',
  EUR: '\u20AC',
  GBP: '\u00A3',
  JPY: '\u00A5',
  ARS: '$',
  MXN: '$',
  COP: '$',
  CLP: '$',
  PEN: 'S/',
  UYU: '$U',
  PYG: '\u20B2',
  BOB: 'Bs',
  VES: 'Bs',
  INR: '\u20B9',
  CNY: '\u00A5',
  AUD: '$',
  CAD: '$',
  CHF: 'CHF',
}

export function getCurrencySymbol(currency: string = 'BRL'): string {
  return CURRENCY_SYMBOLS[currency] || currency
}

export function formatCurrency(amount: number, currency: string = 'BRL'): string {
  const symbol = CURRENCY_SYMBOLS[currency] || currency
  return `${symbol} ${amount.toFixed(2)}`
}

export function useSystemConfig() {
  const [config, setConfig] = useState<SystemConfig | null>(null)
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    try {
      const res = await api.get('/system/config')
      setConfig(res.data)
    } catch {
      setConfig(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load])

  const updateConfig = async (data: Partial<SystemConfig>) => {
    const res = await api.put('/system/config', data)
    setConfig(res.data)
    return res.data
  }

  return { config, loading, reload: load, updateConfig }
}
