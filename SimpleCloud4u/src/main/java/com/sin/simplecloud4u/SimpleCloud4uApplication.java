package com.sin.simplecloud4u;

import com.sin.simplecloud4u.config.RedisConfiguration;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class SimpleCloud4uApplication {
//    @Autowired
//    RedisConnectionFactory redisConnectionFactory;

    public static void main(String[] args) {
        SpringApplication.run(SimpleCloud4uApplication.class, args);
    }

    // 在关闭之前清空redis数据库
//    public @PreDestroy
//    void flushRedis() {
//        redisConnectionFactory.getConnection().flushDb();
//    }
}