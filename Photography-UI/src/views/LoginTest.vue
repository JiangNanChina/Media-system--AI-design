<template>
  <div class="login-test">
    <el-card style="max-width: 600px; margin: 50px auto;">
      <template #header>
        <h2>登录测试工具</h2>
      </template>
      
      <el-form :model="testForm" label-width="120px">
        <el-form-item label="后端服务地址">
          <el-input v-model="apiBaseUrl" placeholder="http://localhost:8080/api" />
        </el-form-item>
        
        <el-form-item label="测试连接">
          <el-button @click="testConnection" :loading="testing">
            测试后端连接
          </el-button>
          <span v-if="connectionStatus" :class="connectionStatus.type" style="margin-left: 10px;">
            {{ connectionStatus.message }}
          </span>
        </el-form-item>
        
        <el-divider />
        
        <h3>预设测试账户</h3>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <h4>管理员账户</h4>
              <p>用户名: admin</p>
              <p>密码: 123456</p>
              <el-button type="primary" @click="testLogin('admin', '123456')" :loading="loginTesting">
                测试管理员登录
              </el-button>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <h4>普通用户账户</h4>
              <p>用户名: user1</p>
              <p>密码: password123</p>
              <el-button type="success" @click="testLogin('user1', 'password123')" :loading="loginTesting">
                测试用户登录
              </el-button>
            </el-card>
          </el-col>
        </el-row>
        
        <el-divider />
        
        <h3>自定义测试</h3>
        <el-form-item label="用户名">
          <el-input v-model="testForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="testForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="testCustomLogin" :loading="loginTesting">
            测试自定义登录
          </el-button>
        </el-form-item>
        
        <el-divider />
        
        <div v-if="testResults.length > 0">
          <h3>测试结果</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(result, index) in testResults"
              :key="index"
              :type="result.success ? 'success' : 'danger'"
              :timestamp="result.timestamp"
            >
              <strong>{{ result.action }}</strong>
              <br />
              <span :class="result.success ? 'success-text' : 'error-text'">
                {{ result.message }}
              </span>
              <div v-if="result.details" class="result-details">
                <pre>{{ result.details }}</pre>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const apiBaseUrl = ref('http://localhost:8080/api')
const testing = ref(false)
const loginTesting = ref(false)
const connectionStatus = ref(null)
const testResults = ref([])

const testForm = reactive({
  username: '',
  password: ''
})

// 测试后端连接
const testConnection = async () => {
  testing.value = true
  connectionStatus.value = null
  
  try {
    const response = await axios.get(`${apiBaseUrl.value}/health`, {
      timeout: 5000
    })
    
    connectionStatus.value = {
      type: 'success-text',
      message: '✅ 后端服务连接正常'
    }
    
    addTestResult('连接测试', true, '后端服务连接成功', response.data)
  } catch (error) {
    console.error('连接测试失败:', error)
    
    let message = '❌ 后端服务连接失败'
    if (error.code === 'ECONNABORTED') {
      message += ' - 请求超时'
    } else if (error.code === 'ERR_NETWORK') {
      message += ' - 网络错误，请检查后端服务是否启动'
    } else if (error.response?.status) {
      message += ` - HTTP ${error.response.status}`
    }
    
    connectionStatus.value = {
      type: 'error-text',
      message
    }
    
    addTestResult('连接测试', false, message, error.message)
  } finally {
    testing.value = false
  }
}

// 测试登录
const testLogin = async (username, password) => {
  loginTesting.value = true
  
  try {
    const response = await axios.post(`${apiBaseUrl.value}/auth/login`, {
      username,
      password
    }, {
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      }
    })
    
    console.log('登录响应:', response.data)
    
    if (response.data.success !== false && response.data.data) {
      ElMessage.success(`登录成功！欢迎 ${response.data.data.user.realName || username}`)
      addTestResult(
        `登录测试 (${username})`, 
        true, 
        '登录成功', 
        {
          token: response.data.data.token ? '已获取' : '未获取',
          user: response.data.data.user
        }
      )
    } else {
      ElMessage.error(response.data.message || '登录失败')
      addTestResult(
        `登录测试 (${username})`, 
        false, 
        response.data.message || '登录失败',
        response.data
      )
    }
  } catch (error) {
    console.error('登录测试失败:', error)
    
    let message = '登录失败'
    if (error.response?.data?.message) {
      message = error.response.data.message
    } else if (error.response?.status === 401) {
      message = '用户名或密码错误'
    } else if (error.response?.status === 404) {
      message = 'API 接口不存在，请检查后端服务'
    } else if (error.code === 'ERR_NETWORK') {
      message = '网络连接失败，请检查后端服务是否启动'
    }
    
    ElMessage.error(message)
    addTestResult(`登录测试 (${username})`, false, message, error.response?.data || error.message)
  } finally {
    loginTesting.value = false
  }
}

// 测试自定义登录
const testCustomLogin = async () => {
  if (!testForm.username || !testForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  await testLogin(testForm.username, testForm.password)
}

// 添加测试结果
const addTestResult = (action, success, message, details) => {
  testResults.value.unshift({
    action,
    success,
    message,
    details: details ? JSON.stringify(details, null, 2) : null,
    timestamp: new Date().toLocaleString('zh-CN')
  })
}
</script>

<style scoped>
.login-test {
  padding: 20px;
}

.success-text {
  color: #67c23a;
}

.error-text {
  color: #f56c6c;
}

.result-details {
  margin-top: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.result-details pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
