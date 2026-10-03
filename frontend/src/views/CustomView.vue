<script setup>
import { onMounted, ref } from 'vue'
import api from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const statuses = ['待确认', '制作中', '待取', '已交付', '退订']

const drawer = ref(false)
const current = ref(null)

const dialog = ref(false)
const form = ref({ customer: '', design: '', deposit: 0, total: 0, status: '待确认', dueDate: '' })

async function load() {
  list.value = await api.listCustoms()
}
function openDrawer(row) {
  current.value = row
  drawer.value = true
}
function openCreate() {
  form.value = { customer: '', design: '', deposit: 0, total: 0, status: '待确认', dueDate: '' }
  dialog.value = true
}
async function save() {
  const payload = {
    ...form.value,
    deposit: Number(form.value.deposit),
    total: Number(form.value.total)
  }
  await api.createCustom(payload)
  ElMessage.success('已登记定制')
  dialog.value = false
  await load()
  emit('changed')
}
async function changeStatus(row) {
  try {
    const { value } = await ElMessageBox.prompt('输入目标状态：' + statuses.join(' / '), '变更定制状态', {
      inputPattern: /^(待确认|制作中|待取|已交付|退订)$/,
      inputErrorMessage: '状态非法'
    })
    await api.transferCustom(row.id, value)
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
      <h2>客户定制</h2>
      <el-button type="primary" @click="openCreate">登记定制</el-button>
    </div>
    <el-table :data="list" border stripe @row-dblclick="openDrawer" style="cursor:pointer">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="customer" label="客户" width="100" />
      <el-table-column prop="design" label="图样" width="140" />
      <el-table-column prop="deposit" label="定金" width="90">
        <template #default="{ row }">¥{{ row.deposit }}</template>
      </el-table-column>
      <el-table-column prop="total" label="总价" width="90">
        <template #default="{ row }">¥{{ row.total }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '已交付' ? 'success' : (row.status === '退订' ? 'info' : 'warning')">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="dueDate" label="约定交付" width="120" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="changeStatus(row)">改状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawer" title="定制详情" direction="rtl" size="380px">
      <el-descriptions :column="1" border v-if="current">
        <el-descriptions-item label="ID">{{ current.id }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ current.customer }}</el-descriptions-item>
        <el-descriptions-item label="图样">{{ current.design }}</el-descriptions-item>
        <el-descriptions-item label="定金">¥{{ current.deposit }}</el-descriptions-item>
        <el-descriptions-item label="总价">¥{{ current.total }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ current.status }}</el-descriptions-item>
        <el-descriptions-item label="约定交付">{{ current.dueDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ current.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ current.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <el-dialog v-model="dialog" title="登记客户定制">
      <el-form :model="form" label-width="90px">
        <el-form-item label="客户"><el-input v-model="form.customer" placeholder="客户姓名" /></el-form-item>
        <el-form-item label="图样"><el-input v-model="form.design" placeholder="定制图样" /></el-form-item>
        <el-form-item label="定金"><el-input-number v-model="form.deposit" :min="0" :step="100" /></el-form-item>
        <el-form-item label="总价"><el-input-number v-model="form.total" :min="0" :step="100" /></el-form-item>
        <el-form-item label="约定交付"><el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
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
