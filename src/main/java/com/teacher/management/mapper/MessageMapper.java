package com.teacher.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teacher.management.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
