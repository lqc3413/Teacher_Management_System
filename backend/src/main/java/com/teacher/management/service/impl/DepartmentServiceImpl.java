package com.teacher.management.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teacher.management.entity.Department;
import com.teacher.management.mapper.DepartmentMapper;
import com.teacher.management.service.IDepartmentService;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {
}
