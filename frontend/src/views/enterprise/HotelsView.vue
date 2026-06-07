<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createHotel,
  createHotelRoomType,
  deleteHotel,
  deleteHotelRoomType,
  getHotelRoomTypes,
  getHotels,
  updateHotel,
  updateHotelRoomType,
} from '@/api/enterprise'
import type { HotelInfo, HotelRoomType } from '@/types'

const loading = ref(false)
const list = ref<HotelInfo[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref({ name: '', address: '', contactPhone: '' })

const drawerVisible = ref(false)
const roomLoading = ref(false)
const activeHotel = ref<HotelInfo | null>(null)
const roomList = ref<HotelRoomType[]>([])
const roomDialogVisible = ref(false)
const editingRoomId = ref<number | null>(null)
const roomForm = ref({ name: '', price: 0, quota: 0 })

const demoList: HotelInfo[] = [
  { id: 1, name: '上海国际会议中心酒店', address: '浦东新区世博大道', contactPhone: '021-88888888' },
  { id: 2, name: '世博洲际酒店', address: '浦东新区国展路', contactPhone: '021-66666666' },
]

const demoRoomMap: Record<number, HotelRoomType[]> = {
  1: [
    { id: 1, hotelId: 1, name: '标准大床房', price: 680, quota: 50, used: 12 },
    { id: 2, hotelId: 1, name: '豪华双床房', price: 880, quota: 30, used: 5 },
  ],
  2: [
    { id: 3, hotelId: 2, name: '商务标间', price: 520, quota: 40, used: 8 },
  ],
}

async function load() {
  loading.value = true
  try {
    list.value = await getHotels()
  } catch {
    list.value = [...demoList]
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { name: '', address: '', contactPhone: '' }
  dialogVisible.value = true
}

function openEdit(row: HotelInfo) {
  editingId.value = row.id
  form.value = { name: row.name, address: row.address, contactPhone: row.contactPhone }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.name || !form.value.contactPhone) return ElMessage.warning('请填写酒店名称和联系电话')
  const payload = { ...form.value }
  if (editingId.value) {
    try {
      await updateHotel(editingId.value, payload)
      ElMessage.success('已保存')
    } catch {
      ElMessage.success('演示：已保存')
    }
    const row = list.value.find((h) => h.id === editingId.value)
    if (row) Object.assign(row, payload)
  } else {
    try {
      await createHotel(payload)
      ElMessage.success('酒店已添加')
      load()
    } catch {
      list.value.unshift({ id: Date.now(), ...payload })
      ElMessage.success('演示：酒店已添加')
    }
  }
  dialogVisible.value = false
}

async function handleDelete(row: HotelInfo) {
  await ElMessageBox.confirm(`删除酒店「${row.name}」？`, '删除确认', { type: 'warning' })
  try {
    await deleteHotel(row.id)
    ElMessage.success('已删除')
  } catch {
    ElMessage.success('演示：已删除')
  }
  list.value = list.value.filter((h) => h.id !== row.id)
  delete demoRoomMap[row.id]
}

async function openRooms(row: HotelInfo) {
  activeHotel.value = row
  drawerVisible.value = true
  await loadRooms()
}

async function loadRooms() {
  if (!activeHotel.value) return
  roomLoading.value = true
  try {
    roomList.value = await getHotelRoomTypes(activeHotel.value.id)
  } catch {
    roomList.value = [...(demoRoomMap[activeHotel.value.id] ?? [])]
  } finally {
    roomLoading.value = false
  }
}

function openRoomCreate() {
  editingRoomId.value = null
  roomForm.value = { name: '', price: 0, quota: 0 }
  roomDialogVisible.value = true
}

function openRoomEdit(row: HotelRoomType) {
  editingRoomId.value = row.id
  roomForm.value = { name: row.name, price: row.price, quota: row.quota }
  roomDialogVisible.value = true
}

async function handleRoomSubmit() {
  if (!activeHotel.value) return
  if (!roomForm.value.name) return ElMessage.warning('请填写房型名称')
  if (roomForm.value.price <= 0) return ElMessage.warning('协议价须大于 0')
  if (roomForm.value.quota <= 0) return ElMessage.warning('配额须大于 0')
  const hotelId = activeHotel.value.id
  const payload = { ...roomForm.value }
  if (editingRoomId.value) {
    try {
      await updateHotelRoomType(hotelId, editingRoomId.value, payload)
      ElMessage.success('已保存')
    } catch {
      ElMessage.success('演示：已保存')
    }
    const row = roomList.value.find((r) => r.id === editingRoomId.value)
    if (row) Object.assign(row, payload)
  } else {
    try {
      await createHotelRoomType(hotelId, payload)
      ElMessage.success('房型已添加')
      loadRooms()
    } catch {
      const newRoom: HotelRoomType = { id: Date.now(), hotelId, ...payload, used: 0 }
      roomList.value.push(newRoom)
      if (!demoRoomMap[hotelId]) demoRoomMap[hotelId] = []
      demoRoomMap[hotelId].push(newRoom)
      ElMessage.success('演示：房型已添加')
    }
  }
  roomDialogVisible.value = false
}

async function handleRoomDelete(row: HotelRoomType) {
  if (!activeHotel.value) return
  await ElMessageBox.confirm(`删除房型「${row.name}」？`, '删除确认', { type: 'warning' })
  const hotelId = activeHotel.value.id
  try {
    await deleteHotelRoomType(hotelId, row.id)
    ElMessage.success('已删除')
  } catch {
    ElMessage.success('演示：已删除')
  }
  roomList.value = roomList.value.filter((r) => r.id !== row.id)
  if (demoRoomMap[hotelId]) {
    demoRoomMap[hotelId] = demoRoomMap[hotelId].filter((r) => r.id !== row.id)
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-header">
      <h1>酒店协议</h1>
      <p>维护协议酒店、房型与配额，供活动开启酒店预订后使用</p>
    </div>
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">添加酒店</el-button>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column prop="name" label="酒店名称" min-width="200" />
      <el-table-column prop="address" label="地址" min-width="160" />
      <el-table-column prop="contactPhone" label="联系电话" width="140" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openRooms(row)">房型配额</el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑酒店' : '添加酒店'" width="480px">
      <el-form label-width="80px">
        <el-form-item label="酒店名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="form.contactPhone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">{{ editingId ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" :title="`${activeHotel?.name} — 房型配额`" size="560px">
      <div class="toolbar" style="margin-bottom: 12px">
        <el-button type="primary" size="small" @click="openRoomCreate">添加房型</el-button>
        <el-button size="small" @click="loadRooms">刷新</el-button>
      </div>
      <el-table v-loading="roomLoading" :data="roomList" border stripe size="small">
        <el-table-column prop="name" label="房型" min-width="120" />
        <el-table-column prop="price" label="协议价(元)" width="100" />
        <el-table-column label="配额" width="100">
          <template #default="{ row }">
            {{ row.used ?? 0 }} / {{ row.quota }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openRoomEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleRoomDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="roomDialogVisible" :title="editingRoomId ? '编辑房型' : '添加房型'" width="420px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="房型名称" required>
          <el-input v-model="roomForm.name" />
        </el-form-item>
        <el-form-item label="协议价" required>
          <el-input-number v-model="roomForm.price" :min="1" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="配额" required>
          <el-input-number v-model="roomForm.quota" :min="1" :step="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoomSubmit">{{ editingRoomId ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>
