# 教师端 Excel 导入培训日期报错复测与修复建议（2026-03-10）

## 1. 复测结论

该问题当前 **可以稳定复现**，并且已经确认根因不在前端上传，而在于：

- Excel 中“教师培训”页的开始日期/结束日期如果被保存为 **带时分秒的日期时间值**，EasyExcel 会把它读成类似 `2026-03-10 00:00:00` 的字符串。
- 后端导入逻辑没有对该字符串做格式归一化，直接写入数据库。
- 数据库表 `sys_training_record.start_date` / `end_date` 的字段长度只有 `varchar(10)`。
- 因此在插入时触发 MySQL `Data truncation: Data too long for column 'start_date'`。

结论：这是一个 **后端 Excel 导入数据格式兼容性问题**，本质上是“导入值长度 19”与“数据库字段长度 10”之间的不匹配。

## 2. 本次复测过程

### 2.1 黑盒接口复测

使用项目本地服务进行了真实接口复测：

1. 使用教师身份访问系统接口并下载 Excel 模板 `/api/teacher/excel/template`
2. 仅保留“教师培训”页一条有效数据，避免其他 sheet 示例行干扰
3. 将“开始日期 / 结束日期”单元格设置为 Excel 日期时间格式：`yyyy-MM-dd HH:mm:ss`
4. 上传到 `/api/teacher/excel/import`

接口返回与截图同类的错误：

```text
导入失败：
### Error updating database.  Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation:
Data truncation: Data too long for column 'start_date' at row 1
```

说明该问题不是偶发，而是可以通过标准导入流程稳定重现。

### 2.2 EasyExcel 中间值验证

对同一份测试文件单独读取“教师培训”sheet，得到：

```text
rows=1
name=excel-import-repro
startDate=2026-03-10 00:00:00
startDateLen=19
endDate=2026-03-10 00:00:00
endDateLen=19
```

这说明：

- Excel 日期时间单元格被读成了 `String`
- 实际长度是 `19`
- 不是前端拼接出来的，也不是数据库额外转换出来的，而是导入阶段就已经是超长值

### 2.3 数据库字段验证

数据库中字段定义为：

```sql
start_date varchar(10)
end_date   varchar(10)
```

因此像 `2026-03-10 00:00:00` 这样的 19 位字符串写入时必然报错。

## 3. 根因定位

### 3.1 前端不是根因

前端只负责把 Excel 文件原样上传：

- `frontend/src/views/teacher/InfoFill.vue:965-971`
- `frontend/src/api/teacher.js:91-97`

前端没有对培训日期做任何字符串拼接或二次转换，因此错误不来自前端页面。

### 3.2 后端导入链路存在原样透传

教师导入逻辑中：

- `src/main/java/com/teacher/management/service/TeacherService.java:839-841`
  - 使用 EasyExcel 读取“教师培训”sheet
- `src/main/java/com/teacher/management/service/TeacherService.java:901-910`
  - 将 `row.getStartDate()`、`row.getEndDate()` 原样放入 `TrainingDTO`
- `src/main/java/com/teacher/management/service/TeacherService.java:199-210`
  - 再将 `TrainingDTO.startDate/endDate` 原样写入 `sys_training_record`

也就是说，目前链路是：

```text
Excel 单元格 -> EasyExcel String -> TrainingDTO String -> TrainingRecord String -> DB varchar(10)
```

中间没有任何一步做：

- 日期格式标准化
- 长度校验
- 用户友好错误提示

### 3.3 DTO 与数据库约束不一致

当前培训相关对象定义：

- `src/main/java/com/teacher/management/dto/excel/TrainingExcelDTO.java`
- `src/main/java/com/teacher/management/dto/TrainingDTO.java`
- `src/main/java/com/teacher/management/entity/TrainingRecord.java`

这些字段都是 `String`，而数据库只允许 10 位日期字符串。

这意味着只要 Excel 实际读取结果不是 `yyyy-MM-dd`，就存在再次报错的风险。

## 4. 为什么会出现这种情况

用户在 Excel 里看到的可能只是“日期”，但 Excel 实际单元格常见有两种情况：

1. 单元格值本身就是纯文本，例如 `2026-03-10`
2. 单元格值是日期时间值，只是界面显示成日期或日期时间

当单元格是第 2 种情况时，EasyExcel 读取到的字符串可能是：

- `2026-03-10 00:00:00`
- `2026/3/10 0:00:00`
- 其他本地化日期时间表示

一旦后端不做统一转换，就会直接把超长字符串写入数据库，最终触发截断异常。

## 5. 修复建议

### 5.1 推荐方案：后端统一归一化为 `yyyy-MM-dd`

最推荐的修复方式是在后端导入时，对培训开始日期和结束日期做统一标准化处理，再入库。

建议在 `TeacherService.importExcel(...)` 中，对这两个字段增加类似逻辑：

```text
normalizeExcelDate(value) -> yyyy-MM-dd
```

建议支持的输入形式：

- `yyyy-MM-dd`
- `yyyy-M-d`
- `yyyy/MM/dd`
- `yyyy.MM.dd`
- `yyyy-MM-dd HH:mm:ss`
- Excel 日期单元格被转换后的常见字符串形式

如果无法识别，应返回明确业务错误，例如：

```text
教师培训开始日期格式不正确，请使用 yyyy-MM-dd
```

这样用户就不会再看到底层 SQL 异常。

### 5.2 不建议仅仅扩大数据库字段长度

另一种做法是把 `start_date` / `end_date` 从 `varchar(10)` 改成 `varchar(19)` 或 `datetime`。

这虽然能消除当前报错，但不一定符合系统的业务语义。

因为模板说明明确写的是：

- 开始日期：`格式：2025-06-01`
- 结束日期：`格式：2025-06-30`

说明当前业务设计更接近“日期”而不是“日期时间”。

因此更合理的做法是：

- 保持数据库存储为日期粒度
- 在导入层把所有兼容输入统一归一化为 `yyyy-MM-dd`

### 5.3 建议同步排查其他日期字段

同类问题不只可能出现在培训日期，建议顺手排查以下 Excel 导入字段：

- 竞赛获奖时间
- 咨询报告采纳日期
- 著作出版日期 / 入选日期
- 获奖日期
- 论文发表日期
- 纵向 / 横向项目立项时间、验收时间
- 创新创业项目起始时间

如果这些字段也使用 `String` 接收并直接写库，就可能存在相同风险。

## 6. 额外发现

本次第一次上传测试时，还遇到了一个相邻问题：

- 模板每个 sheet 的第 2 行是“示例说明行”
- 当前导入逻辑默认按 1 行表头读取
- 因此如果用户没有覆盖示例行，而是从下一行开始填写，示例行也会被当作真实数据参与导入

这不是本次截图里 `start_date` 报错的直接根因，但属于同一批 Excel 导入链路中的高风险点，建议一并修正。

可选处理方式：

- 导入时显式跳过示例说明行
- 或模板取消“示例说明行”，改为批注/备注/单独说明页
- 或读取时使用更明确的有效数据判定逻辑

## 7. 建议的回归测试

修复后建议至少验证以下场景：

1. 培训开始/结束日期填写 `2026-03-10`，导入成功
2. Excel 单元格为日期时间格式 `yyyy-MM-dd HH:mm:ss`，导入后仍成功，且库内保存为 `2026-03-10`
3. 输入 `2026/3/10` 这类常见格式，导入成功并被归一化
4. 输入非法日期文本，接口返回友好错误信息，而不是 SQL 异常
5. 模板示例行未覆盖时，不应被错误识别为业务数据

## 8. 本次复测结论摘要

本次问题已经确认：

- 能复现
- 原因明确
- 根因在后端导入层
- 推荐修复点是 `TeacherService.importExcel(...)` 中对培训日期做标准化和校验

如果后续需要，我可以直接继续帮你把这部分后端修复代码也一起补上。
