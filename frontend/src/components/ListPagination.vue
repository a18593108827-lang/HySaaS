<script setup lang="ts">
defineProps<{
  total: number
  page: number
  size: number
}>()

const emit = defineEmits<{
  change: [page: number]
  sizeChange: [size: number]
}>()
</script>

<template>
  <el-pagination
    v-if="total > 0"
    class="list-pagination"
    layout="total, sizes, prev, pager, next"
    :total="total"
    :current-page="page + 1"
    :page-size="size"
    :page-sizes="[10, 20, 50]"
    @current-change="(p: number) => emit('change', p - 1)"
    @size-change="(s: number) => emit('sizeChange', s)"
  />
</template>
