import { ref } from 'vue'

export function usePagination(defaultSize = 20) {
  const page = ref(0)
  const size = ref(defaultSize)
  const total = ref(0)

  function resetPage() {
    page.value = 0
  }

  return { page, size, total, resetPage }
}
