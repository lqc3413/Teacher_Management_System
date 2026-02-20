<template>
  <div class="user-manage">
    <!-- Search Bar -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="searchForm.employeeNo" placeholder="请输入工号" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="searchForm.deptId" placeholder="请选择部门" clearable style="width: 180px">
            <el-option
              v-for="item in deptOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          <el-button @click="resetSearch" :icon="Refresh">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right; margin-right: 0;">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Table -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        stripe
        style="width: 100%"
        v-loading="loading"
        border
      >
        <el-table-column type="index" label="序号" width="80" align="center" :index="indexMethod" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="employeeNo" label="工号" width="120" />
        <el-table-column label="部门" min-width="100">
          <template #default="scope">
            {{ getDeptName(scope.row.deptId) }}
          </template>
        </el-table-column>
        <el-table-column prop="roleName" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.roleId === 1 ? 'danger' : 'success'">
              {{ scope.row.roleId === 1 ? '管理员' : '教师' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(scope.row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="searchForm.pageSize"
          v-model:current-page="searchForm.pageNum"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="dialogType === 'edit'" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="工号" prop="employeeNo">
          <el-input v-model="form.employeeNo" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="部门" prop="deptId">
          <el-select v-model="form.deptId" placeholder="请选择部门" style="width: 100%">
            <el-option
              v-for="item in deptOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-radio-group v-model="form.roleId">
            <el-radio :label="2">教师</el-radio>
            <el-radio :label="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="form.password" type="password" show-password placeholder="默认密码 123456" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getUserList, addUser, updateUser, deleteUser, getAllDepts } from '../../api/admin'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const deptOptions = ref([]) // 部门下拉选项
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' | 'edit'
const formRef = ref(null)

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  realName: '',
  employeeNo: '',
  deptId: ''
})

const form = reactive({
  id: '',
  username: '',
  realName: '',
  employeeNo: '',
  deptId: '',
  roleId: 2,
  password: '' // 仅新增使用
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  employeeNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 加载部门数据
const loadDepts = async () => {
  try {
    const res = await getAllDepts()
    deptOptions.value = res.data || []
  } catch (error) {
    console.error('获取部门失败', error)
  }
}

const getDeptName = (deptId) => {
  if (!deptId) return '-'
  const dept = deptOptions.value.find(d => d.id === deptId)
  return dept ? dept.name : '-'
}

// 加载用户列表
const loadData = async () => {
  loading.value = true
  try {
    // 过滤掉空字符串参数，避免后端类型转换错误
    const params = {}
    params.pageNum = searchForm.pageNum
    params.pageSize = searchForm.pageSize
    if (searchForm.realName) params.realName = searchForm.realName
    if (searchForm.employeeNo) params.employeeNo = searchForm.employeeNo
    if (searchForm.deptId !== '' && searchForm.deptId !== null && searchForm.deptId !== undefined) {
      params.deptId = searchForm.deptId
    }
    
    const res = await getUserList(params)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.pageNum = 1
  loadData()
}

const resetSearch = () => {
  searchForm.realName = ''
  searchForm.employeeNo = ''
  searchForm.deptId = ''
  handleSearch()
}

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const indexMethod = (index) => {
  return (searchForm.pageNum - 1) * searchForm.pageSize + index + 1
}

// 新增
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  // 重置表单
  Object.keys(form).forEach(key => form[key] = '')
  form.roleId = 2
  form.password = '123456' // 默认密码
}

// 编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  // 回显数据
  Object.keys(form).forEach(key => form[key] = row[key])
  form.password = '' // 编辑时不显示密码
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (dialogType.value === 'add') {
        await addUser(form)
        ElMessage.success('新增成功')
      } else {
        await updateUser(form)
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error(error)
    } finally {
      submitting.value = false
    }
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除用户 "${row.realName}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error(error)
    }
  })
}

// 重置密码
const handleResetPwd = (row) => {
  ElMessageBox.prompt('请输入新密码', '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.{6,}/,
    inputErrorMessage: '密码长度不能少于6位',
    inputValue: '123456'
  }).then(async ({ value }) => {
    try {
      await updateUser({ id: row.id, password: value })
      ElMessage.success('密码已重置')
    } catch (error) {
      console.error(error)
    }
  })
}

// 状态切换
const handleStatusChange = async (row) => {
  try {
    await updateUser({ id: row.id, status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1 // 回滚
    console.error(error)
  }
}

onMounted(() => {
  loadData()
  loadDepts()
})
</script>

<style scoped lang="scss">
.user-manage {
  .search-card {
    margin-bottom: 20px;
    border-radius: 12px;
    border: none;
    /* Glass Effect */
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
    
    :deep(.el-card__body) {
      padding: 24px 24px 8px;
    }

    .el-form-item {
      margin-bottom: 16px;
      margin-right: 24px;
      
      :deep(.el-form-item__label) {
        font-weight: 500;
        color: var(--color-text);
      }
    }
  }



  .table-card {
    border-radius: 12px;
    border: none;
    /* Glass Effect */
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
    padding: 8px;

    :deep(.el-table) {
      background-color: transparent;
      --el-table-tr-bg-color: transparent;
      --el-table-header-bg-color: rgba(28, 25, 23, 0.03);
      --el-table-header-text-color: var(--color-primary);
      border-radius: 8px;
      overflow: hidden;

      th.el-table__cell {
        background-color: rgba(28, 25, 23, 0.03);
        font-weight: 600;
        font-family: var(--font-heading);
      }
    }
    
    .pagination {
      margin-top: 24px;
      display: flex;
      justify-content: flex-end;
      padding-right: 16px;
    }
  }
}


</style>
