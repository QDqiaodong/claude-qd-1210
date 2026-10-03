<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const types = ['錾子', '砧', '花丝模', '抛光轮']
const statuses = ['良好', '待修', '报废']

const drawer = ref(false)
const current = ref(null)

const dialog = ref(false)
const form = ref({ name: '', type: '錾子', wear: 0, lastMaintain: '', status: '良好' })

async function load() {
  list.value = await api.listMolds()
}
function openDrawer(row) {
  current.value = row
  drawer.value = true
}
function openCreate() {
  form.value = { name: '', type: '錾子', wear: 0, lastMaintain: '', status: '良好' }
  dialog.value = true
}
async function save() {
  const payload = { ...form.value, wear: Number(form.value.wear) }
  await api.createMold(payload)
  ElMessage.success('已登记模具')
  dialog.value = false
  await load()
  emit('changed')
}
async function changeStatus(row) {
  try {
    const { value } = await ElMessageBox.prompt('输入目标状态：' + statuses.join(' / '), '变更模具状态', {
      inputPattern: /^(良好|待修|报废)$/,
      inputErrorMessage: '状态非法'
    })
    await api.transferMold(row.id, value)
    ElMessage.success('状态已更新')
    await load()
    emit('changed')
  } catch (e) { if (e !== 'cancel') ElMessage.error(e.message) }
}

const emit = defineEmits(['changed'])
onMounted(load)
</script>

<template>
  <div>
    <div class="bar">
      <h2>工具模具</h2>
      <el-button type="primary" @click="openCreate">登记模具</el-button>
    </div>
    <el-table :data="list" border stripe @row-dblclick="openDrawer" style="cursor:pointer">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" width="140" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column prop="wear" label="磨损度%" width="100">
        <template #default="{ row }">
          <el-tag :type="row.wear > 80 ? 'danger' : (row.wear > 50 ? 'warning' : 'success')">{{ row.wear }}%</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastMaintain" label="上次维护" width="130" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '报废' ? 'danger' : (row.status === '待修' ? 'warning' : 'success')">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="changeStatus(row)">改状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawer" title="模具详情" direction="rtl" size="360px">
      <el-descriptions :column="1" border v-if="current">
        <el-descriptions-item label="ID">{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ current.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ current.type }}</el-descriptions-item>
        <el-descriptions-item label="磨损度%">{{ current.wear }}%</el-descriptions-item>
        <el-descriptions-item label="上次维护">{{ current.lastMaintain }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ current.status }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ current.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ current.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="dialog" title="登记工具模具">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="模具名称" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type"><el-option v-for="t in types" :key="t" :label="t" :value="t" /></el-select>
        </el-form-item>
        <el-form-item label="磨损度%"><el-input-number v-model="form.wear" :min="0" :max="100" :step="5" /></el-form-item>
        <el-form-item label="上次维护"><el-date-picker v-model="form.lastMaintain" type="date" value-format="YYYY-MM-DD" /></el-form-item>
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
