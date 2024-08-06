package com.syy.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.syy.usercenter.mapper.UserTeamMapper;
import com.syy.usercenter.model.domain.UserTeam;
import com.syy.usercenter.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author PYY
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-02-19 11:16:55
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




