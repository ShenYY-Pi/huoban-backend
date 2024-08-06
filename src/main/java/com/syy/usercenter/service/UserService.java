package com.syy.usercenter.service;

import com.syy.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.syy.usercenter.model.domain.request.UpdateTagRequest;
import com.syy.usercenter.model.domain.response.SafetyUserResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * @author PYY
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2024-01-30 13:01:11
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword,
                      String checkPassword);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    SafetyUserResponse userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    SafetyUserResponse getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    List<SafetyUserResponse> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user,User loginUser);

    /**
     * 获取当前登录用户
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<SafetyUserResponse> matchUsers(long num, User loginUser);

    int updateTagById(UpdateTagRequest updateTag, User currentUser);

    String redisFormat(long id);
}
