package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.management.common.Result;
import com.teacher.management.entity.Department;
import com.teacher.management.entity.User;
import com.teacher.management.mapper.UserMapper;
import com.teacher.management.service.IDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    private UserMapper userMapper;

    /** 分页查询（含部门人数） */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<Department> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, Department::getName, name);
        wrapper.orderByAsc(Department::getSortOrder);
        Page<Department> result = departmentService.page(page, wrapper);

        // 填充每个部门的人数
        for (Department dept : result.getRecords()) {
            Long count = userMapper.selectCount(
                    new QueryWrapper<User>().eq("dept_id", dept.getId()));
            dept.setMemberCount(count.intValue());
        }

        return Result.success(result);
    }

    /** 查询全部（下拉选择用） */
    @GetMapping("/all")
    public Result<?> all() {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getStatus, 1);
        wrapper.orderByAsc(Department::getSortOrder);
        return Result.success(departmentService.list(wrapper));
    }

    /** 查询某部门下的成员列表 */
    @GetMapping("/{id}/members")
    public Result<?> members(@PathVariable Long id) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeptId, id);
        wrapper.select(User::getId, User::getUsername, User::getRealName,
                User::getEmployeeNo, User::getPhone, User::getEmail, User::getGender);
        List<User> users = userMapper.selectList(wrapper);
        return Result.success(users);
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(departmentService.getById(id));
    }

    /** 新增 */
    @PostMapping
    public Result<?> save(@RequestBody Department department) {
        return departmentService.save(department) ? Result.success() : Result.error("新增失败");
    }

    /** 修改 */
    @PutMapping
    public Result<?> update(@RequestBody Department department) {
        return departmentService.updateById(department) ? Result.success() : Result.error("修改失败");
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // 检查部门下是否还有成员
        long memberCount = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.teacher.management.entity.User>()
                        .eq(com.teacher.management.entity.User::getDeptId, id));
        if (memberCount > 0) {
            return Result.error("该部门下仍有 " + memberCount + " 名成员，请先转移后再删除");
        }
        return departmentService.removeById(id) ? Result.success() : Result.error("删除失败");
    }
}
