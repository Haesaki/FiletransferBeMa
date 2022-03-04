package com.sin.simplecloud4u.controller;


import com.sin.simplecloud4u.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BaseController {
    @Value("${sc4u.account.registered.max-file-size}")
    protected int REGISTERED_MAX_FILE;

    @Value("${sc4u.account.visitor.max-file-size}")
    protected int VISITOR_MAX_FILE;

    @Value("${sc4u.tempFile.directory}")
    protected String tempFilePath;

    @Value("${sc4u.account.visitor.file-expired-time}")
    protected String VISITOR_FILE_EXPIRED_TIME;

    @Value("${sc4u.account.registered.file-expired-time}")
    protected String REGISTERED_FILE_EXPIRED_TIME;


    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;
    protected User loginUser;

    @ModelAttribute
    public void setModelAttribute(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.session = request.getSession(true);
        this.loginUser = (User) session.getAttribute("loginUser");
    }
}
