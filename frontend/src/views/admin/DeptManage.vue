<template>
  <div class="dept-manage">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="部门名称">
          <el-input v-model="searchForm.name" placeholder="请输入部门名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">查询</el-button>
          <el-button @click="resetSearch" :icon="Refresh">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right; margin-right: 0;">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增部门</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <el-table 
        :data="tableData" 
        stripe 
        style="width: 100%" 
        v-loading="loading"
        border
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="部门名称" min-width="150">
          <template #default="scope">
            <span class="dept-name">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="部门人数" width="120" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="handleViewMembers(scope.row)">
              <el-icon style="margin-right: 4px"><User /></el-icon>
              {{ scope.row.memberCount ?? 0 }} 人
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" effect="light">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增部门' : '编辑部门'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看成员 弹窗 -->
    <el-dialog
      v-model="memberDialogVisible"
      :title="`${currentDeptName} — 成员列表`"
      width="700px"
      destroy-on-close
    >
      <el-table :data="memberList" v-loading="memberLoading" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="realName" label="姓名" min-width="100">
          <template #default="scope">
            <span style="font-weight: 500">{{ scope.row.realName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="employeeNo" label="工号" width="120" align="center" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="scope">
            {{ scope.row.gender === 1 ? '男' : scope.row.gender === 2 ? '女' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" align="center">
          <template #default="scope">
            {{ scope.row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160">
          <template #default="scope">
            {{ scope.row.email || '-' }}
          </template>
        </el-table-column>
      </el-table>
      <div v-if="memberList.length === 0 && !memberLoading" class="empty-tip">
        <el-empty description="该部门暂无成员" />
      </div>
      <template #footer>
        <el-button @click="memberDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptList, addDept, updateDept, deleteDept, getDeptMembers } from '../../api/admin'
import dayjs from 'dayjs'

// 搜索
const searchForm = reactive({ name: '' })

// 表格
const loading = ref(false)
const tableData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 新增/编辑弹窗
const dialogVisible = ref(false)
const dialogType = ref('add')
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  name: '',
  sortOrder: 0,
  status: 1
})
const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 成员弹窗
const memberDialogVisible = ref(false)
const memberLoading = ref(false)
const memberList = ref([])
const currentDeptName = ref('')

onMounted(() => { loadData() })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDeptList({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      name: searchForm.name || undefined
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    console.error('获取部门列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pageNum.value = 1; loadData() }
const resetSearch = () => { searchForm.name = ''; handleSearch() }
const handleSizeChange = () => loadData()
const handleCurrentChange = () => loadData()

const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : '-'

// ====== 新增 / 编辑 / 删除 ======
const handleAdd = () => {
  dialogType.value = 'add'
  form.id = null; form.name = ''; form.sortOrder = 0; form.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  form.id = row.id; form.name = row.name; form.sortOrder = row.sortOrder; form.status = row.status
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除部门 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    await deleteDept(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (dialogType.value === 'add') {
        await addDept(form)
        ElMessage.success('新增成功')
      } else {
        await updateDept(form)
        ElMessage.success('修改成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitLoading.value = false
    }
  })
}

// ====== 查看成员 ======
const handleViewMembers = async (row) => {
  currentDeptName.value = row.name
  memberDialogVisible.value = true
  memberLoading.value = true
  try {
    const res = await getDeptMembers(row.id)
    memberList.value = res.data || []
  } catch (e) {
    console.error('获取成员列表失败', e)
    memberList.value = []
  } finally {
    memberLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.dept-manage {
  .search-card {
    margin-bottom: 20px;
    border-radius: 12px;
    border: none;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(12px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
    
    :deep(.el-card__body) {
      padding: 24px 24px 8px;
    }
    
    .el-form-item {
      margin-bottom: 16px;
    }
  }



  .table-card {
    border-radius: 12px;
    border: none;
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
      
      th.el-table__cell {
        font-family: var(--font-heading);
        font-weight: 600;
      }
    }
  }

  .dept-name {
    font-weight: 600;
    color: var(--color-primary);
    font-family: var(--font-heading);
  }

  .pagination {
    margin-top: 24px;
    padding-right: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .empty-tip {
    padding: 40px 0;
  }
}


</style>
