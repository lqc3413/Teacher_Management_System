package com.teacher.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teacher.management.common.Result;
import com.teacher.management.entity.User;
import com.teacher.management.entity.UserInfo;
import com.teacher.management.vo.UserDetailVO;

public interface IUserService extends IService<User> {

    Result<UserDetailVO> getUserDetail(Long userId);

    Result<String> updateUserInfo(UserInfo userInfo);

    Result<String> deleteUser(Long userId);
}
