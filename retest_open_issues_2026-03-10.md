# 未完全修复问题复测总结（2026-03-10，已更新）

## 1. 说明

- 本文档基于 `2026-03-10` 对当前仓库代码和本地接口的复测结果整理。
- 本次已结合业务确认结果更新结论。
- 原始问题来源参考：`业务逻辑漏洞测试报告_2026-03-08.md`

## 2. 最终结论摘要

| 编号 | 风险 | 最终结论 | 说明 |
|---|---|---|---|
| B04 | 高 | 已修复 | 审核详情页已切换为 `paperList` 渲染 |
| B08 | 中（待确认规则） | 不视为漏洞 | 业务已确认“允许超时重提” |
| N01 | 中 | 已修复 | 教师访问管理类只读接口已被限制 |

## 3. 详细结论

### B04 高：多论文场景下审核详情页仍只展示首篇论文

- 最终结论
  - 已修复。
- 复测结果
  - 后端详情接口继续返回 `paperList`。
  - 管理员审核页、主任审核页已改为按 `paperList` 渲染，不再以单个 `paper` 字段作为主展示逻辑。
- 涉及位置
  - `src/main/java/com/teacher/management/service/TeacherService.java`
  - `src/main/java/com/teacher/management/vo/SubmissionDetailVO.java`
  - `frontend/src/views/admin/MaterialAudit.vue`
  - `frontend/src/views/dept-director/DeptAudit.vue`
- 备注
  - 当前数据库中没有“同一次提交含多篇论文”的现成样本，因此本次主要依据代码路径与接口返回结构确认修复有效。

### B08 中（待确认规则）：被退回后仍可绕过任务截止时间再次提交

- 最终结论
  - 不视为漏洞。
- 业务确认
  - 业务规则已明确：允许超时重提。
- 说明
  - 当前实现允许退回后的再次提交跳过截止时间校验。
  - 该行为与当前业务规则一致，因此不再作为缺陷处理。
- 建议
  - 可在代码注释、需求说明或测试用例中明确该规则，避免后续再次被误判为漏洞。

### N01 中：普通教师仍可访问部分管理类只读接口

- 最终结论
  - 已修复。
- 复测结果
  - 使用教师账号登录后，以下接口现在均返回 `403`：
  - `GET /api/users/1`
  - `GET /api/config/list`
  - `GET /api/departments/list`
  - `GET /api/departments/1/members`
  - 同时保留 `GET /api/departments/all` 可访问，用于教师端下拉数据场景。
- 涉及位置
  - `src/main/java/com/teacher/management/config/SecurityConfig.java`
  - `src/main/java/com/teacher/management/controller/UserController.java`
  - `src/main/java/com/teacher/management/controller/DepartmentController.java`
  - `src/main/java/com/teacher/management/controller/SysConfigController.java`

## 4. 当前状态

- 本次待复测清单中的问题已全部关闭。
- 其中：
  - `B04` 已修复
  - `N01` 已修复
  - `B08` 经业务确认后判定为符合规则

## 5. 验证记录

- 后端构建验证
  - `mvn test -q` 通过
  - `mvn -q -DskipTests package` 通过
- 前端构建验证
  - `npm run build` 通过
- 接口复测结论
  - 教师访问管理类只读接口：已被正确拦截
  - 管理员详情接口：可正常返回 `paperList`

## 6. 备注

- 本次已确认以下原问题修复有效：`B01`、`B02`（写接口部分）、`B03`、`B04`、`B05`、`B06`、`B07`
- `B08` 按当前业务规则保留现有实现，不再视为漏洞
