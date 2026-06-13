<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getEvent } from '@/api/enterprise'
import { useEventStore } from '@/stores/event'

const route = useRoute()
const eventStore = useEventStore()
const eventId = computed(() => String(route.params.id))
const eventTitle = ref('')
const loading = ref(false)

function resolveLocalTitle(id: string) {
  if (eventStore.current && String(eventStore.current.id) === id) return eventStore.current.title
  return eventStore.list.find((e) => String(e.id) === id)?.title ?? `活动 #${id}`
}

async function loadEvent() {
  const id = eventId.value
  const cached = resolveLocalTitle(id)
  if (cached && !cached.startsWith('活动 #')) {
    eventTitle.value = cached
  }
  loading.value = true
  try {
    const event = await getEvent(id)
    eventTitle.value = event.title
    eventStore.setCurrent(event)
  } catch {
    eventTitle.value = resolveLocalTitle(id)
  } finally {
    loading.value = false
  }
}

watch(
  eventId,
  (id) => {
    if (id) {
      eventStore.setActiveEventId(id)
      loadEvent()
    }
  },
  { immediate: true },
)
</script>

<template>
  <div class="event-detail">
    <header v-loading="loading" class="detail-header">
      <h1>{{ eventTitle || '活动详情' }}</h1>
    </header>
    <router-view />
  </div>
</template>

<style scoped>
.detail-header h1 {
  margin: 0 0 1.25rem;
  font-size: 1.375rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}
</style>
