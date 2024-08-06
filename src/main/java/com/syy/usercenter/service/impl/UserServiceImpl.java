package com.syy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.syy.usercenter.common.ErrorCode;
import com.syy.usercenter.exception.BusinessException;
import com.syy.usercenter.model.domain.request.UpdateTagRequest;
import com.syy.usercenter.model.domain.response.SafetyUserResponse;
import com.syy.usercenter.service.UserService;
import com.syy.usercenter.model.domain.User;
import com.syy.usercenter.mapper.UserMapper;
import com.syy.usercenter.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.syy.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.syy.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * @author SYY
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-01-30 13:01:11
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 盐值
     */
    private static final String SALT = "syy";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount, userAccount, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号小于4位");
        }
//        if (userPassword.length() < 8 || checkPassword.length() < 8) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码小于8位");
//        }
//        if (planetCode.length() > 5) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
//        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }

        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号重复");
        }

//        // 星球编号不能重复
//        queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("planetCode", planetCode);
//        count = userMapper.selectCount(queryWrapper);
//        if (count > 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号重复");
//        }

        // 校验密码是否相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不同");
        }

        // 2. 对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }

        return user.getId();
    }

    @Override
    public SafetyUserResponse userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount, userAccount)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        //if (userPassword.length() < 8) {
        //    return null;
        //}

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }

        // 2. 对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户或密码错误");
        }

        // 3.脱敏
        SafetyUserResponse safetyUser = getSafetyUser(user);

        // 4.记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
//        String redisKey = redisFormat(safetyUser.getId());
//        redisTemplate.opsForValue().set(redisKey, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public SafetyUserResponse getSafetyUser(User originUser) {
        if (originUser == null)
            return null;
        SafetyUserResponse safetyUser = new SafetyUserResponse();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setUserProfile(originUser.getUserProfile());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<SafetyUserResponse> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 内存查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        Gson gson = new Gson();
        // 在内存中判断是否包含需要的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            Set<String> tempNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempNameSet = Optional.ofNullable(tempNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员 可以更新任意用户
        // 如果不是管理员 只允许更新自己的信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        SafetyUserResponse safetyUser = (SafetyUserResponse) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (safetyUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User user = new User();
        BeanUtils.copyProperties(safetyUser, user);
        return (User) user;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        SafetyUserResponse user = (SafetyUserResponse) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<SafetyUserResponse> matchUsers(long num, User loginUser) {
        // 查询所有有标签的用户
        List<User> userList = getAllUsersWithTags();

        //查询当前用户的标签
        String tags = loginUser.getTags();
        if (StringUtils.isEmpty(tags)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "您还没有任何标签！");
        }

        List<String> loginUserTags = parseTags(tags);

        // 计算相似度并排序
        List<Pair<User, Long>> sortedUsers = calculateAndSortUsersBySimilarity(userList, loginUser, loginUserTags, num);

        // 获取最终的用户响应列表
        return getUserResponsesByIds(sortedUsers);
    }

    private List<User> getAllUsersWithTags() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        return this.list(queryWrapper);
    }

    private List<String> parseTags(String tags) {
        Gson gson = new Gson();
        return gson.fromJson(tags, new TypeToken<List<String>>() {}.getType());
    }

    private List<Pair<User, Long>> calculateAndSortUsersBySimilarity(List<User> userList, User loginUser, List<String> loginUserTags, long num) {
        List<Pair<User, Long>> list = new ArrayList<>();
        Gson gson = new Gson();

        for (User user : userList) {
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags) || user.getId() == (loginUser.getId())) {
                continue;
            }

            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {}.getType());
            long distance = AlgorithmUtils.minDistance(loginUserTags, userTagList);
            list.add(new Pair<>(user, distance));
        }

        return list.stream()
                .sorted(Comparator.comparingLong(Pair::getValue))
                .limit(num)
                .collect(Collectors.toList());
    }

    private List<SafetyUserResponse> getUserResponsesByIds(List<Pair<User, Long>> sortedUsers) {
        List<Long> userIdList = sortedUsers.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);

        Map<Long, List<SafetyUserResponse>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(SafetyUserResponse::getId));

        List<SafetyUserResponse> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }

        return finalUserList;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTagById(UpdateTagRequest updateTag, User currentUser) {
        long id = currentUser.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户不存在");
        }
        Set<String> newTags = updateTag.getTagList();
        if (newTags.size() > 12) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多设置12个标签");
        }
        if (!isAdmin(currentUser) && id != currentUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        User user = userMapper.selectById(id);
        Gson gson = new Gson();
        Set<String> oldTags = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {
        }.getType());
        if(oldTags == null){
            oldTags = new HashSet<>();
        }
        Set<String> oldTagsCapitalize = toCapitalize(oldTags);
        Set<String> newTagsCapitalize = toCapitalize(newTags);

        // 添加 newTagsCapitalize 中 oldTagsCapitalize 中不存在的元素
        oldTagsCapitalize.addAll(newTagsCapitalize.stream().filter(tag -> !oldTagsCapitalize.contains(tag)).collect(Collectors.toSet()));
        // 移除 oldTagsCapitalize 中 newTagsCapitalize 中不存在的元素
        oldTagsCapitalize.removeAll(oldTagsCapitalize.stream().filter(tag -> !newTagsCapitalize.contains(tag)).collect(Collectors.toSet()));
        String tagsJson = gson.toJson(oldTagsCapitalize);
        user.setTags(tagsJson);
        return userMapper.updateById(user);
    }

    @Override
    public String redisFormat(long id) {
        return String.format("shenyuyang:user:search:%s", id);
    }

    private Set<String> toCapitalize(Set<String> oldSet) {
        return oldSet.stream().map(StringUtils::capitalize).collect(Collectors.toSet());
    }

    /**
     * 根据标签搜索用户 (sql查询)
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Deprecated
    private List<SafetyUserResponse> searchUsersByTagsBySql(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // sql 查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接and查询
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

}




