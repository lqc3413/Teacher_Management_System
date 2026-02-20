<template>
  <div class="system-settings">
    <h2 class="page-title">系统设置</h2>

    <el-card class="content-card" shadow="never" v-loading="configLoading">
      <el-form
        :model="configForm"
        label-width="160px"
        class="config-form"
      >
        <el-form-item
          v-for="item in configList"
          :key="item.id"
          :label="item.configDesc"
        >
          <el-input
            v-model="item.configValue"
            style="max-width: 400px"
            :placeholder="'请输入' + item.configDesc"
          />
        </el-form-item>
      </el-form>
      <div class="form-actions">
        <el-button type="primary" @click="handleSaveConfig" :loading="configSaving">
          保存配置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getConfigList, updateConfigBatch
} from '../../api/admin'

// ========== 系统参数配置 ==========
const configLoading = ref(false)
const configSaving = ref(false)
const configList = ref([])
const configForm = ref({})

const fetchConfig = async () => {
  configLoading.value = true
  try {
    const res = await getConfigList()
    configList.value = res.data || []
  } catch (e) {
    console.error('获取配置失败', e)
  } finally {
    configLoading.value = false
  }
}

const handleSaveConfig = async () => {
  configSaving.value = true
  try {
    const items = configList.value.map(item => ({
      id: item.id,
      configValue: item.configValue
    }))
    await updateConfigBatch(items)
    ElMessage.success('配置保存成功')
  } catch (e) {
    console.error('保存配置失败', e)
  } finally {
    configSaving.value = false
  }
}

// ========== 初始化 ==========
onMounted(() => {
  fetchConfig()
})
</script>

<style scoped lang="scss">
.system-settings {
  max-width: 1200px;
  margin: 0 auto;

  .page-title {
    font-size: 24px;
    font-weight: 700;
    font-family: var(--font-heading);
    color: var(--color-primary);
    margin-bottom: 24px;
    letter-spacing: -0.5px;
  }

  .content-card {
    border-radius: 12px;
    border: none;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
    padding: 8px;
  }

  .config-form {
    max-width: 600px;
    
    .el-form-item {
      margin-bottom: 24px;
    }
  }

  .form-actions {
    padding-top: 24px;
    border-top: 1px solid rgba(0, 0, 0, 0.05);
    display: flex;
    justify-content: flex-end;
  }
}

/* Button override */
:deep(.el-button--primary) {
  background-color: var(--color-cta);
  border-color: var(--color-cta);
  
  &:hover {
    background-color: var(--color-cta-hover);
    border-color: var(--color-cta-hover);
  }
}
</style>
