# Apifox 接口测试指南

本项目后端接口受 **Spring Security + JWT** 保护，因此在 Apifox 中测试时需要先获取 Token，并在后续请求中自动携带。

## 1. 环境配置 (Environment)

1. 打开 Apifox，新建或进入一个项目。
2. 点击右上角的 **“环境管理”** (Environment)。
3. 新建一个环境，命名为 `Local Dev` (或任意名称)。
4. **前置 URL** (Base URL) 设置为：`http://localhost:8080`
5. 点击 **“保存”**。

## 2. 登录并自动获取 Token

我们需要创建一个登录接口，并配置它在成功登录后自动将 Token 保存到环境变量中，这样后续接口就不需要手动复制 Token 了。

### 2.1 创建登录接口
1. 新建接口，命名为 `管理员登录`。
2. **方法**：`POST`
3. **路径**：`/api/users/login`
4. **Body** 类型选择 `json`，内容如下：
   ```json
   {
     "username": "admin",
     "password": "admin123",
     "role": "admin" // 或 "teacher"
   }
   ```

### 2.2 配置后置操作 (提取 Token)
1. 在该接口的 **“后置操作”** (Post-processors) 标签页中。
2. 点击 **“添加后置操作”** -> 选择 **“提取变量”** (Extract Variable)。
3. 填写配置：
   - **变量名**：`token`
   - **类型**：`JSONPath`
   - **JSONPath 表达式**：`$.data.token`
   - **保存到**：`环境变量` (Environment Variable)
4. 点击 **“保存”** 并点击 **“运行”**。如果登录成功，右上角的小眼睛图标（查看环境变量）里应该能看到 `token` 变量已有值。

## 3. 配置全局认证 (自动携带 Token)

为了让所有接口都自动带上 Token，我们在项目概览或根目录配置全局参数。

1. 点击左侧目录树的根节点（**项目概览** 或 **根目录**）。
2. 在 **“Auth”** (认证) 或 **“Header”** 标签页中配置。
   - **推荐方式 (Header)**：
     - 参数名：`Authorization`
     - 参数值：`Bearer {{token}}` （注意 `Bearer` 后有一个空格）
3. 这样配置后，项目下所有接口都会自动带上这个 Header。

---

## 4. 业务接口测试演示

### A. 获取用户列表
- **方法**：`GET`
- **路径**：`/api/users/list?pageNum=1&pageSize=10`

### B. 获取部门列表
- **方法**：`GET`
- **路径**：`/api/departments/all`

---

## 5. 附录：完整后端 API 列表

以下是项目中所有可用的后端接口，你可以一并导入到 Apifox 中。

### 🔐 认证与用户 (Auth & User)
| 方法 | 路径 | 描述 | 参数示例 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | 教师注册 (公开) | `{ "username": "...", "password": "...", "employeeNo": "..." }` |
| `POST` | `/api/users/login` | 用户登录 (公开) | `{ "username": "...", "password": "..." }` |
| `GET` | `/api/users/list` | 用户列表 (分页) | Query: `pageNum=1`, `realName=张` |
| `GET` | `/api/users/{id}` | 获取用户详情 | Path: `id` |
| `POST` | `/api/users` | 新增用户 | Body: `{ "username": "...", "roleId": 2 ... }` |
| `PUT` | `/api/users` | 修改用户 | Body: `{ "id": 1, "realName": "..." }` |
| `DELETE` | `/api/users/{id}` | 删除用户 | Path: `id` |

### 🏢 部门管理 (Department)
| 方法 | 路径 | 描述 | 参数示例 |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/departments/list` | 部门列表 (分页) | Query: `pageNum=1`, `name=计算机` |
| `GET` | `/api/departments/all` | 所有部门 (不分页) | 无 |
| `GET` | `/api/departments/{id}` | 部门详情 | Path: `id` |
| `POST` | `/api/departments` | 新增部门 | Body: `{ "name": "...", "code": "..." }` |
| `PUT` | `/api/departments` | 修改部门 | Body: `{ "id": 1, "name": "..." }` |
| `DELETE` | `/api/departments/{id}` | 删除部门 | Path: `id` |

### 🏷️ 基础数据 (Category)
| 方法 | 路径 | 描述 | 参数示例 |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/categories/list` | 分类列表 (分页) | Query: `pageNum=1` |
| `GET` | `/api/categories/tree` | 分类树形结构 | 无 |
| `GET` | `/api/categories/children/{id}` | 子分类列表 | Path: `parentId` |
| `POST` | `/api/categories` | 新增分类 | Body: `{ "name": "...", "parentId": 0 }` |
| `PUT` | `/api/categories` | 修改分类 | Body: `{ "id": 1, "name": "..." }` |
| `DELETE` | `/api/categories/{id}` | 删除分类 | Path: `id` |

### 👨‍🏫 教师业务 (Teacher)
| 方法 | 路径 | 描述 | 参数示例 |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/teacher/submit` | 提交教学科研信息 | Body: `{ "submitMonth": "2026-02", ... }` |
| `GET` | `/api/teacher/history` | 提交历史记录 | Query: `year=2026` |
| `GET` | `/api/teacher/detail/{id}` | 提交详情 | Path: `id` |
| `GET` | `/api/teacher/dashboard` | 仪表盘统计数据 | 无 |

### 📚 教材/资料 (Material)
| 方法 | 路径 | 描述 | 参数示例 |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/materials/list` | 资料列表 (分页) | Query: `auditStatus=0` (0:待审核) |
| `POST` | `/api/materials` | 提交资料 | Body: `{ "title": "...", "categoryId": 1 }` |
| `PUT` | `/api/materials/audit/{id}` | **审核资料** (管理员) | Query: `auditStatus=1`, `auditRemark=通过` |
| `DELETE` | `/api/materials/{id}` | 删除资料 | Path: `id` |

### 🛠️ 开发工具 (Dev)
> 仅开发环境可用，生产环境请禁用
| 方法 | 路径 | 描述 | 参数示例 |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/dev/reset-password` | 重置密码 | Query: `username=admin`, `newPassword=123` |
