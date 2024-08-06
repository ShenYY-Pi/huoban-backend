package com.syy.usercenter.model.domain.response;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *  脱敏的安全对象
 */
@Data
public class SafetyUserResponse implements Serializable {
    private static final long serialVersionUID = -5005999719850708352L;
    /**
     * 用户id
     */
    private long id;
    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String userProfile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 状态 0-正常
     */
    private Integer userStatus;

    /**
     * 用户角色 0 -普通用户 1 -管理员
     */
    private Integer userRole;

    /**
     * 标签列表JSON
     */
    private String tags;


    /**
     * 性别
     */
    private Integer gender;

    /**
     * 创建时间
     */
    private Date createTime;


}
