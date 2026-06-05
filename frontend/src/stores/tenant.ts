import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Tenant } from '@/types'

export const useTenantStore = defineStore('tenant', () => {
  const current = ref<Tenant | null>(null)

  function setCurrent(t: Tenant | null) {
    current.value = t
  }

  return { current, setCurrent }
})
