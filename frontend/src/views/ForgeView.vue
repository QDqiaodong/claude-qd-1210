<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const products = ['手镯', '戒指', '项链', '耳环', '吊坠']
const crafts = ['錾刻', '花丝', '烧蓝']
const statuses = ['开料', '锻打', '抛光', '质检', '完成', '返工']

const silvers = ref([])
const molds = ref([])

const drawer = ref(false)
const current = ref(null)

const dialog = ref(false)
const form = ref({ product: '手镯', silverId: '', moldId: '', planWeight: 10, actualWeight: '', craft: '錾刻', status: '开料' })

async function load() {
  list.value = await api.listForges()
}
async function loadRefs() {
  silvers.value = await api.listSilvers()
  molds.value = await api.listMolds()
}
async function silverLabel(id) {
  const s = silvers.value.find(x => x.id === id)
  return s ? `#${s.id} ${s.alloy}/${s.status}` : id
}
async function moldLabel(id) {
  const m = molds.value.find(x => x.id === id)
  return m ? `#${m.id} ${m.name}/${m.status}` : id
}
function openDrawer(row) {
  current.value = row
  drawer.value = true
}
function openCreate() {
  form.value = { product: '手镯', silverId: '', moldId: '', planWeight: 10, actualWeight: '', craft: '錾刻', status: '开料' }
  dialog.value = true
}
async function save() {
  const payload = {
    ...form.value,
    silverId: Number(form.value.silverId),
    moldId: Number(form.value.moldId),
    planWeight: Number(form.value.planWeight),
    actualWeight: form.value.actualWeight === '' ? null : Number(form.value.actualWeight)
  }
  await api.createForge(payload)
  ElMessage.success('已开工单')
  dialog.value = false
  await load()
  emit('changed')
}
async function changeStatus(row) {
  try {
    const { value } = await ElMessageBox.prompt('输入目标状态：' + statuses.join(' / '), '变更工单状态', {
      inputPattern: /^(开料|锻打|抛光|质检|完成|返工)$/,
      inputErrorMessage: '状态非法'
    })
    await api.transferForge(row.id, value)
    ElMessage.success('状态已更新')
    await load()
    emit('changed')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message) }
}

const emit = defineEmits(['changed'])
onMounted(async () => { await load(); await loadRefs() })
</script>

<template>
  <div>
    <div class="bar">
      <h2>锻造工单</h2>
      <el-button type="primary" @click="openCreate">新建工单</el-button>
    </div>
    <el-table :data="list" border stripe @row-dblclick="openDrawer" style="cursor:pointer">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="product" label="产品" width="90" />
      <el-table-column prop="silverId" label="银料" width="140">
        <template #default="{ row }"><span>{{ silverLabel(row.silverId) }}</span></template>
      </el-table-column>
      <el-table-column prop="moldId" label="模具" width="140">
        <template #default="{ row }"><span>{{ moldLabel(row.moldId) }}</span></template>
      </el-table-column>
      <el-table-column prop="planWeight" label="计划(克)" width="100" />
      <el-table-column prop="actualWeight" label="实际(克)" width="100">
        <template #default="{ row }">{{ row.actualWeight ?? '—' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '完成' ? 'success' : (row.status === '返工' ? 'danger' : 'warning')">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="craft" label="工艺" width="80" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="changeStatus(row)">改状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawer" title="工单详情" direction="rtl" size="380px">
      <el-descriptions :column="1" border v-if="current">
        <el-descriptions-item label="ID">{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="产品">{{ current.product }}</el-descriptions-item>
        <el-descriptions-item label="银料">{{ silverLabel(current.silverId) }}</el-descriptions-item>
        <el-descriptions-item label="模具">{{ moldLabel(current.moldId) }}</el-descriptions-item>
        <el-descriptions-item label="计划重量(克)">{{ current.planWeight }}</el-descriptions-item>
        <el-descriptions-item label="实际重量(克)">{{ current.actualWeight ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ current.status }}</el-descriptions-item>
        <el-descriptions-item label="工艺">{{ current.craft }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ current.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ current.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="dialog" title="新建锻造工单">
      <el-form :model="form" label-width="90px">
        <el-form-item label="产品">
          <el-select v-model="form.product"><el-option v-for="p in products" :key="p" :label="p" :value="p" /></el-select>
        </el-form-item>
        <el-form-item label="银料">
          <el-select v-model="form.silverId" placeholder="选择银料">
            <el-option v-for="s in silvers" :key="s.id" :label="`#${s.id} ${s.alloy}/${s.status} 检:${s.purityCheck ? '是' : '否'}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="模具">
          <el-select v-model="form.moldId" placeholder="选择模具">
            <el-option v-for="m in molds" :key="m.id" :label="`#${m.id} ${m.name}/${m.status}`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划(克)"><el-input-number v-model="form.planWeight" :min="0.1" :step="1" /></el-form-item>
        <el-form-item label="实际(克)"><el-input-number v-model="form.actualWeight" :min="0" :step="0.5" /></el-form-item>
        <el-form-item label="工艺">
          <el-select v-model="form.craft"><el-option v-for="c in crafts" :key="c" :label="c" :value="c" /></el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option v-for="s in statuses" :key="s" :label="s" :value="s" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
</style>
