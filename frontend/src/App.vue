<script setup>
import { computed, onMounted, ref } from 'vue'
import router from './router'
import api from './api'

import SilverView from './views/SilverView.vue'
import ForgeView from './views/ForgeView.vue'
import CustomView from './views/CustomView.vue'
import MoldView from './views/MoldView.vue'

// ------------------------------------------------------------------
// 导航形态：两级导航（顶部业务组 + 子项）。本仓操作范式为「双击行 → 右侧抽屉」。
// 四个业务模块作为同一顶级业务组下的四个子项，点击切换当前模块视图。
// ------------------------------------------------------------------

const group = {
  title: '银饰锻造坊',
  items: [
    { path: '/silver', label: '银料库存', comp: SilverView },
    { path: '/forge', label: '锻造工单', comp: ForgeView },
    { path: '/custom', label: '客户定制', comp: CustomView },
    { path: '/mold', label: '工具模具', comp: MoldView }
  ]
}

const allItems = group.items
const currentPath = ref(router.currentRoute.value.path)
const counts = ref({})

const currentView = computed(() => {
  const m = allItems.find(i => i.path === currentPath.value)
  return m ? m.comp : SilverView
})

async function loadCounts() {
  try {
    const [s, f, c, m] = await Promise.all([
      api.listSilvers(), api.listForges(), api.listCustoms(), api.listMolds()
    ])
    counts.value = {
      '/silver': s.length,
      '/forge': f.length,
      '/custom': c.length,
      '/mold': m.length
    }
  } catch (e) { /* 计数失败不影响主流程 */ }
}

function onSelect(index) {
  if (index !== currentPath.value) {
    router.push(index)
  }
}

router.afterEach((to) => { currentPath.value = to.path })

onMounted(async () => {
  currentPath.value = router.currentRoute.value.path
  await loadCounts()
})
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">银</span>
        <div class="brand-text">
          <div class="brand-title">银饰锻造坊</div>
          <div class="brand-sub">银灰主题 · 两级导航</div>
        </div>
      </div>
      <el-menu
        class="nav-menu"
        :default-active="currentPath"
        @select="onSelect"
      >
        <el-sub-menu :index="group.title">
          <template #title>{{ group.title }}</template>
          <el-menu-item
            v-for="it in group.items"
            :key="it.path"
            :index="it.path"
          >{{ it.label }}（{{ counts[it.path] ?? 0 }}）</el-menu-item>
        </el-sub-menu>
      </el-menu>
      <div class="hint">提示：双击任意行查看详情</div>
    </aside>

    <main class="content">
      <component :is="currentView" @changed="loadCounts" />
    </main>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; display: flex; }

.sidebar {
  width: 220px; flex-shrink: 0;
  background: #fff; border-right: 1px solid var(--line);
  display: flex; flex-direction: column;
}
.brand {
  display: flex; align-items: center; gap: 10px;
  padding: 16px 18px; border-bottom: 1px solid var(--line);
}
.brand-mark {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  background: var(--primary); color: #fff; font-weight: 700; font-size: 18px;
}
.brand-title { font-size: 15px; font-weight: 600; }
.brand-sub { font-size: 12px; color: var(--muted); }

.nav-menu { border-right: none; flex: 1; }
.hint { padding: 12px 18px; font-size: 12px; color: var(--muted); border-top: 1px solid var(--line); }

.content { flex: 1; padding: 20px 24px 28px; overflow: auto; }
</style>
