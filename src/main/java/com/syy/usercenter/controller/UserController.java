package com.syy.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.syy.usercenter.common.BaseResponse;
import com.syy.usercenter.common.ErrorCode;
import com.syy.usercenter.common.ResultUtils;
import com.syy.usercenter.exception.BusinessException;
import com.syy.usercenter.model.domain.User;
import com.syy.usercenter.model.domain.request.UpdateTagRequest;
import com.syy.usercenter.model.domain.request.UserLoginRequest;
import com.syy.usercenter.model.domain.request.UserRegisterRequest;
import com.syy.usercenter.model.domain.response.SafetyUserResponse;
import com.syy.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.syy.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author Syy
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            // return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        if (StringUtils.isAnyBlank(userAccount, userAccount, checkPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<SafetyUserResponse> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SafetyUserResponse user = userService.userLogin(userAccount, userPassword, request);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        return ResultUtils.success(userService.userLogout(request));
    }

    @GetMapping("/current")
    public BaseResponse<SafetyUserResponse> getCurrentUser(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        SafetyUserResponse currentUser = (SafetyUserResponse) userObject;
        //https://www.youtube.com/watch?v=Gg0wHcCRJ48
        if (userObject == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        Long id = currentUser.getId();
        // todo 校验用户是否合法
        SafetyUserResponse safetyUser = userService.getSafetyUser(userService.getById(id));
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<SafetyUserResponse>> searchUsers(String username, HttpServletRequest request) {
        // 仅管理员可以查询
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }

        List<User> userList = userService.list(queryWrapper);
        List<SafetyUserResponse> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<SafetyUserResponse>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<SafetyUserResponse> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<List<SafetyUserResponse>> recommendUsers(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("shenyu:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存 直接读缓存
        List<SafetyUserResponse> userList = (List<SafetyUserResponse>) valueOperations.get(redisKey);
        if (userList != null) {
            return ResultUtils.success(userList);
        }
        // 如果没有缓存 直接查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> user = userService.list(queryWrapper);

        userList = user.stream().map(u -> {
            SafetyUserResponse safetyUserResponse = new SafetyUserResponse();
            safetyUserResponse.setId(u.getId());
            safetyUserResponse.setUsername(u.getUsername());
            safetyUserResponse.setUserAccount(u.getUserAccount());
            safetyUserResponse.setAvatarUrl(u.getAvatarUrl());
            safetyUserResponse.setUserProfile(u.getUserProfile());
            safetyUserResponse.setEmail(u.getEmail());
            safetyUserResponse.setPhone(u.getPhone());
            safetyUserResponse.setUserStatus(u.getUserStatus());
            safetyUserResponse.setUserRole(u.getUserRole());
            safetyUserResponse.setTags(u.getTags());
            safetyUserResponse.setGender(u.getGender());
            safetyUserResponse.setCreateTime(u.getCreateTime());
            // 不复制不需要的字段，例如updateTime和userPassword
            return safetyUserResponse;
        }).collect(Collectors.toList());

        //写缓存,30s过期
        try {
            valueOperations.set(redisKey, userList, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userList);
    }


    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 判断参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验权限
        User loginUser = userService.getLoginUser(request);
        // 触发更新
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/update/tags")
    public BaseResponse<Integer> updateTagById(@RequestBody UpdateTagRequest tagRequest, HttpServletRequest request) {
        if (tagRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        int updateTag = userService.updateTagById(tagRequest, currentUser);
//        redisTemplate.delete(userService.redisFormat(currentUser.getId()));
//        request.getSession().removeAttribute(USER_LOGIN_STATE);
        request.getSession().setAttribute(USER_LOGIN_STATE, getCurrentUser(request).getData());
        return ResultUtils.success(updateTag);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        // 仅管理员可以删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<SafetyUserResponse>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }
}

















