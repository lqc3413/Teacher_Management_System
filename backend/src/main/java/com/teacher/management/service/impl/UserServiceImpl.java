package com.teacher.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teacher.management.common.Result;
import com.teacher.management.entity.User;
import com.teacher.management.entity.UserInfo;
import com.teacher.management.mapper.UserInfoMapper;
import com.teacher.management.mapper.UserMapper;
import com.teacher.management.service.IUserService;
import com.teacher.management.vo.UserDetailVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final String CACHE_KEY_PREFIX = "user:detail:";

    private final UserInfoMapper userInfoMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public UserServiceImpl(UserInfoMapper userInfoMapper,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.userInfoMapper = userInfoMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;
        String json = redisTemplate.opsForValue().get(key);

        if (json != null && !json.isBlank()) {
            try {
                return Result.success(objectMapper.readValue(json, UserDetailVO.class));
            } catch (Exception e) {
                redisTemplate.delete(key);
            }
        }

        UserDetailVO detail = userInfoMapper.getUserDetail(userId);
        if (detail == null) {
            return Result.error("用户不存在");
        }

        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(detail), 10, TimeUnit.MINUTES);
        } catch (JsonProcessingException ignored) {
        }

        return Result.success(detail);
    }

    @Override
    @Transactional
    public Result<String> updateUserInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.getUserId() == null) {
            return Result.error("用户信息不能为空，且 userId 不能为空");
        }

        User user = this.getById(userInfo.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userInfo.getUserId());
        UserInfo existing = userInfoMapper.selectOne(wrapper);

        if (existing == null) {
            UserInfo toInsert = new UserInfo();
            toInsert.setUserId(userInfo.getUserId());
            toInsert.setRealName(userInfo.getRealName());
            toInsert.setPhone(userInfo.getPhone());
            toInsert.setAddress(userInfo.getAddress());
            userInfoMapper.insert(toInsert);
        } else {
            if (userInfo.getRealName() != null) {
                existing.setRealName(userInfo.getRealName());
            }
            if (userInfo.getPhone() != null) {
                existing.setPhone(userInfo.getPhone());
            }
            if (userInfo.getAddress() != null) {
                existing.setAddress(userInfo.getAddress());
            }
            userInfoMapper.updateById(existing);
        }

        redisTemplate.delete(CACHE_KEY_PREFIX + userInfo.getUserId());
        return Result.success("更新成功");
    }

    @Override
    @Transactional
    public Result<String> deleteUser(Long userId) {
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }

        userInfoMapper.delete(new QueryWrapper<UserInfo>().eq("user_id", userId));
        boolean removed = this.removeById(userId);
        redisTemplate.delete(CACHE_KEY_PREFIX + userId);
        return removed ? Result.success("删除成功") : Result.error("删除失败");
    }
}
