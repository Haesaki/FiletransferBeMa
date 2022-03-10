package com.sin.simplecloud4u.config;

import com.sin.simplecloud4u.interceptor.LoginHandlerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.MultipartConfigElement;

@Configuration
public class MVCConfiguration implements WebMvcConfigurer, ErrorPageRegistrar {

    @Value("sc4u.account.registered.max-file-size")
    private static int MAX_FILE_SIZE;

    @Value("sc4u.account.registered.max-file-size")
    private static int MAX_REQUEST_SIZE;


    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        //文件最大
        factory.setMaxFileSize(DataSize.parse("1024000KB"));
        /// 设置总上传数据总大小
        factory.setMaxRequestSize(DataSize.parse("1024000KB"));
        return factory.createMultipartConfig();
    }

    /**
     * @return void
     * @Description 注册视图控制器
     **/
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/temp-file").setViewName("temp-file");
        registry.addViewController("/error400Page").setViewName("error/400");
        registry.addViewController("/error401Page").setViewName("error/401");
        registry.addViewController("/error404Page").setViewName("error/404");
        registry.addViewController("/error500Page").setViewName("error/500");
    }

    /**
     * @return void
     * @Description 注册登录拦截器 addPathPatterns() -> 拦截的请求  excludePathPatterns -> 不拦截的请求
     **/
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginHandlerInterceptor()).addPathPatterns("/**")
                .excludePathPatterns(
                        "/", "/file/*", "/error400Page", "/error401Page", "/error404Page", "/error500Page", "/uploadTempFile", "/admin", "/sendCode", "/loginByQQ", "/login", "/register", "/file/share", "/connection",
                        "/asserts/**", "/**/*.css", "/**/*.js", "/**/*.png ", "/**/*.jpg",
                        "/**/*.jpeg", "/**/*.gif", "/**/fonts/*", "/**/*.svg",
                        "/sc4u/file/share");
    }

    /**
     * @return void
     * @Description 配置错误页面
     **/
    @Override
    public void registerErrorPages(ErrorPageRegistry registry) {
        ErrorPage error400Page = new ErrorPage(HttpStatus.BAD_REQUEST, "/error400Page");
        ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/error401Page");
        ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/error404Page");
        ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error500Page");
        registry.addErrorPages(error400Page, error401Page, error404Page, error500Page);
    }
}