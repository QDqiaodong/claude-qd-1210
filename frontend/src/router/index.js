import { createRouter, createWebHashHistory } from 'vue-router'

// 注意：main.js 里没有 app.use(router)，路由实例由 App.vue 直接驱动，
// 所以这里全部用静态 import —— <component :is> 拿到的是组件对象本身。
import SilverView from '../views/SilverView.vue'
import ForgeView from '../views/ForgeView.vue'
import CustomView from '../views/CustomView.vue'
import MoldView from '../views/MoldView.vue'

const routes = [
  { path: '/', redirect: '/silver' },
  { path: '/silver', name: 'silver', component: SilverView },
  { path: '/forge', name: 'forge', component: ForgeView },
  { path: '/custom', name: 'custom', component: CustomView },
  { path: '/mold', name: 'mold', component: MoldView },
  { path: '/:pathMatch(.*)*', redirect: '/silver' }
]

export default createRouter({
  history: createWebHashHistory(),
  routes
})
