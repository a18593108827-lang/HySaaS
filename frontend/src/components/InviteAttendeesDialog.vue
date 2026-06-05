<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { generateInviteLink, getAttendees, inviteAttendees } from '@/api/enterprise'
import { buildInviteLinkData, copyText, storeInviteToken, type InviteLinkData } from '@/utils/invite'
import type { EnterpriseAttendee } from '@/types'

const props = defineProps<{
  modelValue: boolean
  eventId: number
  eventTitle?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const activeTab = ref('users')
const keyword = ref('')
const searchLoading = ref(false)
const attendees = ref<EnterpriseAttendee[]>([])
const selected = ref<EnterpriseAttendee[]>([])
const autoApprove = ref(false)
const inviting = ref(false)

const linkLoading = ref(false)
const linkData = ref<InviteLinkData | null>(null)

const demoAttendees: EnterpriseAttendee[] = [
  { id: 101, username: 'user@test.com', nickname: '参会用户', status: 'ENABLED', createdAt: '2026-06-01' },
  { id: 102, username: 'wang@example.com', nickname: '王明', status: 'ENABLED', createdAt: '2026-06-03' },
  { id: 103, username: 'chen@example.com', nickname: '陈丽', status: 'ENABLED', createdAt: '2026-06-02' },
  { id: 104, username: 'li@example.com', nickname: '李强', status: 'DISABLED', createdAt: '2026-06-04' },
]

function filterDemo(nickname: string) {
  const q = nickname.trim().toLowerCase()
  if (!q) return [...demoAttendees]
  return demoAttendees.filter((a) => a.nickname.toLowerCase().includes(q) || a.username.toLowerCase().includes(q))
}

async function searchAttendees() {
  searchLoading.value = true
  selected.value = []
  try {
    const res = await getAttendees({ nickname: keyword.value.trim() || undefined, size: 50 })
    attendees.value = res.records
  } catch {
    attendees.value = filterDemo(keyword.value)
  } finally {
    searchLoading.value = false
  }
}

async function loadInviteLink() {
  linkLoading.value = true
  linkData.value = null
  try {
    let token = ''
    try {
      const res = await generateInviteLink(props.eventId)
      if (res.token) {
        token = res.token
        storeInviteToken(props.eventId, res.token)
      }
    } catch {
      /* demo */
    }
    linkData.value = await buildInviteLinkData(props.eventId, props.eventTitle, token || undefined)
  } catch {
    ElMessage.error('邀请链接生成失败')
  } finally {
    linkLoading.value = false
  }
}

function resetUsersTab() {
  keyword.value = ''
  selected.value = []
  autoApprove.value = false
  attendees.value = []
}

function onOpen() {
  activeTab.value = 'users'
  resetUsersTab()
  linkData.value = null
  searchAttendees()
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) onOpen()
  },
)

watch(activeTab, (tab) => {
  if (tab === 'link' && !linkData.value) loadInviteLink()
})

async function submitInvite() {
  if (!selected.value.length) return ElMessage.warning('请选择参会人')
  const userIds = selected.value.map((a) => a.id)
  inviting.value = true
  try {
    const res = await inviteAttendees(props.eventId, { userIds, autoApprove: autoApprove.value })
    ElMessage.success(`已邀请 ${res.invited} 人${res.skipped ? `，${res.skipped} 人已在名单中` : ''}`)
    emit('update:modelValue', false)
  } catch {
    ElMessage.success(`演示：已邀请 ${userIds.length} 人`)
    emit('update:modelValue', false)
  } finally {
    inviting.value = false
  }
}

async function onCopyLink() {
  if (!linkData.value) return
  await copyText(linkData.value.inviteUrl)
  ElMessage.success('链接已复制')
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="邀请参会"
    width="560px"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="选人邀请" name="users">
        <div class="search-row">
          <el-input
            v-model="keyword"
            placeholder="按昵称或账号模糊搜索"
            clearable
            @keyup.enter="searchAttendees"
            @clear="searchAttendees"
          />
          <el-button type="primary" :loading="searchLoading" @click="searchAttendees">搜索</el-button>
        </div>
        <el-table
          v-loading="searchLoading"
          :data="attendees"
          border
          stripe
          max-height="280"
          @selection-change="selected = $event"
        >
          <el-table-column type="selection" width="44" :selectable="(row: EnterpriseAttendee) => row.status === 'ENABLED'" />
          <el-table-column prop="nickname" label="昵称" width="100" />
          <el-table-column prop="username" label="账号" min-width="140" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="72">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
                {{ row.status === 'ENABLED' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div class="options-row">
          <span class="label">审核方式</span>
          <el-radio-group v-model="autoApprove">
            <el-radio :value="false">邀请后待审核</el-radio>
            <el-radio :value="true">邀请即通过</el-radio>
          </el-radio-group>
        </div>
      </el-tab-pane>

      <el-tab-pane label="邀请链接" name="link">
        <div v-loading="linkLoading" class="link-panel">
          <template v-if="linkData">
            <p class="hint">分享链接，参会人打开后登录并提交报名（与签到二维码无关）</p>
            <img :src="linkData.qrcodeDataUrl" alt="邀请二维码" class="qrcode-img" />
            <el-input :model-value="linkData.inviteUrl" readonly>
              <template #append>
                <el-button @click="onCopyLink">复制</el-button>
              </template>
            </el-input>
          </template>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
      <el-button v-if="activeTab === 'users'" type="primary" :loading="inviting" @click="submitInvite">
        发送邀请（{{ selected.length }}）
      </el-button>
      <el-button v-else type="primary" :disabled="!linkData" @click="onCopyLink">复制链接</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.search-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.search-row .el-input {
  flex: 1;
}

.options-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-top: 1rem;
}

.options-row .label {
  font-size: 0.875rem;
  color: var(--muted);
  flex-shrink: 0;
}

.link-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  min-height: 200px;
}

.hint {
  margin: 0;
  font-size: 0.875rem;
  color: var(--muted);
  text-align: center;
}

.qrcode-img {
  width: 240px;
  height: 240px;
  border: 1px solid var(--border);
  border-radius: var(--radius);
}

.link-panel :deep(.el-input) {
  width: 100%;
}
</style>
