package com.sin.simplecloud4u.config;

import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

//@Configuration
public class ShiroConfig {
    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(@Qualifier("securityManager") DefaultWebSecurityManager defaultWebSecurityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        //设置安全管理器
        bean.setSecurityManager(defaultWebSecurityManager);

        /*
            shiro的内置过滤器
            anon: 无需认证就可访问
            authc：必须认证才能访问
            user：必须拥有记住我功能才能访问
            perms: 拥有对某个资源的权限才能访问
            role:拥有某个角色权限才能访问
       */

        // 授权
        Map<String, String> filterMap = new LinkedHashMap<>();
        // filterMap.put("/admin/blog", "perms[user:admin]");
        // filterMap.put("/admin/*", "authc");
        //设置登出
        filterMap.put("/admin/logout", "logout");

        // bean.setFilterChainDefinitionMap(filterMap);

        //设置登录请求
        // bean.setLoginUrl("/login");
        //设置未授权页面
        // bean.setUnauthorizedUrl("/404");

        return bean;
    }

    @Bean(name = "securityManager")
    public DefaultWebSecurityManager getDefaultWebSecurityManager(@Qualifier("userRealm") UserRealm userRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //关联UserRealm

        securityManager.setRealm(userRealm);

        return securityManager;
    }

    //创建realm对象 ，需要自定义
    @Bean(name = "userRealm")
    public UserRealm userRealm() {
        return new UserRealm();
    }
}