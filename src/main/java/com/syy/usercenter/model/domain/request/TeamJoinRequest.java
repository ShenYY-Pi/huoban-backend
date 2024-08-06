package com.syy.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 加入队伍请求体
 */

@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = -977500402840409039L;
    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
