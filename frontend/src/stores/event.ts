import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { EventItem } from '@/types'

export const useEventStore = defineStore('event', () => {
  const current = ref<EventItem | null>(null)
  const list = ref<EventItem[]>([])
  const activeEventId = ref<number | string | null>(localStorage.getItem('active-event-id'))

  function setCurrent(e: EventItem | null) {
    current.value = e
  }

  function setList(items: EventItem[]) {
    list.value = items
  }

  function setActiveEventId(id: number | string | null) {
    activeEventId.value = id
    if (id == null) localStorage.removeItem('active-event-id')
    else localStorage.setItem('active-event-id', String(id))
  }

  return { current, list, activeEventId, setCurrent, setList, setActiveEventId }
})
