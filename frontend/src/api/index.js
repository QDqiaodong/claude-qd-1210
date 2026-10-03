import axios from 'axios'

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error?.response?.data?.message || error?.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

async function call(promise) {
  const response = await promise
  const body = response.data
  if (!body || body.ok === false) {
    throw new Error((body && body.message) || '请求失败')
  }
  return body.data
}

const params = (o) => ({ params: o })

export default {
  // ---------------- 银料 ----------------
  listSilvers: () => call(http.get('/silvers')),
  createSilver: (d) => call(http.post('/silvers', d)),
  updateSilver: (id, d) => call(http.put(`/silvers/${id}`, d)),
  transferSilver: (id, status) => call(http.post(`/silvers/${id}/status`, null, params({ status }))),

  // ---------------- 锻造工单 ----------------
  listForges: () => call(http.get('/forges')),
  createForge: (d) => call(http.post('/forges', d)),
  updateForge: (id, d) => call(http.put(`/forges/${id}`, d)),
  transferForge: (id, status) => call(http.post(`/forges/${id}/status`, null, params({ status }))),

  // ---------------- 客户定制 ----------------
  listCustoms: () => call(http.get('/customs')),
  createCustom: (d) => call(http.post('/customs', d)),
  updateCustom: (id, d) => call(http.put(`/customs/${id}`, d)),
  transferCustom: (id, status) => call(http.post(`/customs/${id}/status`, null, params({ status }))),

  // ---------------- 工具模具 ----------------
  listMolds: () => call(http.get('/molds')),
  createMold: (d) => call(http.post('/molds', d)),
  updateMold: (id, d) => call(http.put(`/molds/${id}`, d)),
  transferMold: (id, status) => call(http.post(`/molds/${id}/status`, null, params({ status })))
}
