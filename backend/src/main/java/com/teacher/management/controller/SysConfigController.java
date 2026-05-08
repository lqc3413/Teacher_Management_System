package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.management.common.Result;
import com.teacher.management.entity.SysConfig;
import com.teacher.management.mapper.SysConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统参数配置控制器
 */
@RestController
@RequestMapping("/api/config")
public class SysConfigController {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    /**
     * 获取所有配置项
     */
    @GetMapping("/list")
    public Result<?> list() {
        List<SysConfig> list = sysConfigMapper.selectList(
                new LambdaQueryWrapper<SysConfig>().orderByAsc(SysConfig::getId));
        return Result.success(list);
    }

    /**
     * 批量更新配置项
     * 接收格式: [ { id, configValue }, ... ]
     */
    @PutMapping("/batch")
    public Result<?> batchUpdate(@RequestBody List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            Long id = Long.valueOf(item.get("id").toString());
            String value = item.get("configValue") != null ? item.get("configValue").toString() : "";
            SysConfig config = new SysConfig();
            config.setId(id);
            config.setConfigValue(value);
            sysConfigMapper.updateById(config);
        }
        return Result.success();
    }
}
