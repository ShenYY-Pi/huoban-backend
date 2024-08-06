package com.syy.usercenter.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.syy.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


/**
 * 用户服务测试
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("Syy");
        user.setUserAccount("123");
        user.setAvatarUrl("https://cdn.nlark.com/yuque/0/2023/png/26306121/1703665151412-8acd817f-3584-45bb-a212-9174f4f05dc4.png?x-oss-process=image%2Fresize%2Cw_446%2Climit_0");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setEmail("123");
        user.setPhone("456");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    @Test
    void userRegister() {
        long l = userService.userRegister("yuhuihui", "12345678", "12345678");
        Assertions.assertTrue(l > 0);
    }

    @Test
    void searchUsersByTags() {
//        List<String> tagNameList = Arrays.asList("java","c++");
//        List<User> userList = userService.searchUsersByTags(tagNameList);
//        System.out.println(userList);
//        Assertions.assertNotNull(userList);
    }


}