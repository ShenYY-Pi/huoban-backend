package com.syy.usercenter.service;

import com.syy.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;


    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 增
        valueOperations.set("shenyyString", "niuuuu");
        valueOperations.set("shenyyInt", 1);
        valueOperations.set("shenyyDouble", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("shenyy");
        valueOperations.set("shenyyUser", user);

        // 查
        Object shenyy = valueOperations.get("shenyyString");
        Assertions.assertTrue("niuuuu".equals((String) shenyy));
        shenyy = valueOperations.get("shenyyInt");
        Assertions.assertTrue(1 == (Integer) shenyy);
        shenyy = valueOperations.get("shenyyDouble");
        Assertions.assertTrue(2.0 == (Double) shenyy);
        System.out.println(valueOperations.get("shenyyUser"));

        //删
        redisTemplate.delete("shenyyString");
    }

}