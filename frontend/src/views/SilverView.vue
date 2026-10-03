<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const alloys = ['999', '925', '苗银']
const sources = ['采购', '回收']
const statuses = ['在用', '熔炼中', '已成材', '报废']

const drawer = ref(false)
const current = ref(null)

const dialog = ref(false)
const editing = ref(null)
const form = ref({ alloy: '999', weight: 10, source: '采购', status: '在用', purityCheck: false })

async function load() {
  list.value = await api.listSilvers()
}
function openDrawer(row) {
  current.value = row
  drawer.value = true
}
function openCreate() {
  editing.value = null
  form.value = { alloy: '999', weight: 10, source: '采购', status: '在用', purityCheck: false }
  dialog.value = true
}
async function save() {
  const payload = { ...form.value, weight: Number(form.value.weight) }
  await api.createSilver(payload)
  ElMessage.success('已登记银料')
  dialog.value = false
  await load()
  emit('changed')
}
async function changeStatus(row) {
  try {
    const { value } = await ElMessageBox.prompt('输入目标状态：' + statuses.join(' / '), '变更银料状态', {
      inputPattern: /^(在用|熔炼中|已成材|报废)$/,
      inputErrorMessage: '状态非法'
    })
    await api.transferSilver(row.id, value)
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
      <h2>银料库存</h2>
      <el-button type="primary" @click="openCreate">登记银料</el-button>
    </div>
    <el-table :data="list" border stripe @row-dblclick="openDrawer" style="cursor:pointer">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="alloy" label="合金" width="90" />
      <el-table-column prop="weight" label="重量(克)" width="100" />
      <el-table-column prop="source" label="来源" width="90" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '报废' ? 'danger' : (row.status === '熔炼中' ? 'warning' : 'success')">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="purityCheck" label="纯度已检" width="100">
        <template #default="{ row }">{{ row.purityCheck ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="changeStatus(row)">改状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawer" title="银料详情" direction="rtl" size="360px">
      <el-descriptions :column="1" border v-if="current">
        <el-descriptions-item label="ID">{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="合金">{{ current.alloy }}</el-descriptions-item>
        <el-descriptions-item label="重量(克)">{{ current.weight }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ current.source }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ current.status }}</el-descriptions-item>
        <el-descriptions-item label="纯度已检">{{ current.purityCheck ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ current.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ current.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="dialog" title="登记银料">
      <el-form :model="form" label-width="90px">
        <el-form-item label="合金">
          <el-select v-model="form.alloy"><el-option v-for="a in alloys" :key="a" :label="a" :value="a" /></el-select>
        </el-form-item>
        <el-form-item label="重量(克)"><el-input-number v-model="form.weight" :min="0.1" :step="1" /></el-form-item>
        <el-form-item label="来源">
          <el-select v-model="form.source"><el-option v-for="s in sources" :key="s" :label="s" :value="s" /></el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option v-for="s in statuses" :key="s" :label="s" :value="s" /></el-select>
        </el-form-item>
        <el-form-item label="纯度已检"><el-switch v-model="form.purityCheck" /></el-form-item>
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
