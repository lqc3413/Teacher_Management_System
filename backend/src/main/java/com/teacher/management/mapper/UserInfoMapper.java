package com.teacher.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teacher.management.entity.UserInfo;
import com.teacher.management.vo.UserDetailVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserInfoMapper extends BaseMapper<UserInfo> {

    @Select("""
            SELECT
                u.id AS userId,
                u.username AS username,
                COALESCE(i.real_name, u.real_name) AS realName,
                COALESCE(i.phone, u.phone) AS phone,
                i.address AS address
            FROM users u
            LEFT JOIN user_info i ON u.id = i.user_id
            WHERE u.id = #{userId}
            """)
    UserDetailVO getUserDetail(@Param("userId") Long userId);
}
