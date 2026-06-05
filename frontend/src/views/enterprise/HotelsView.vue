<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getHotels } from '@/api/enterprise'
import type { HotelInfo } from '@/types'

const loading = ref(false)
const list = ref<HotelInfo[]>([])

async function load() {
  loading.value = true
  try {
    list.value = await getHotels()
  } catch {
    list.value = [
      { id: 1, name: '上海国际会议中心酒店', address: '浦东新区', contactPhone: '021-88888888' },
      { id: 2, name: '世博洲际酒店', address: '浦东新区', contactPhone: '021-66666666' },
    ]
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>酒店协议</h1>
      <p>维护活动关联酒店、房型与配额</p>
    </div>
    <div class="toolbar">
      <el-button type="primary">添加酒店</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="name" label="酒店名称" min-width="200" />
      <el-table-column prop="address" label="地址" min-width="160" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default>
          <el-button link type="primary">房型配额</el-button>
          <el-button link>编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
