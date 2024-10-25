package com.lt.questnest.interceptor;

import com.lt.questnest.configuration.CustomWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// 登录拦截器，用户登录前先执行
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();

        // 检查用户是否已登录:如果用户已经登录，会将email放入session中
        if (session.getAttribute("email") == null) {
            logger.info("LoginInterceptor取出的session:{}",session.getAttribute("email"));
            // 用户未登录，重定向到登录页面
            response.sendRedirect("/loginByPasswd");
            return false;
        }

        // 用户已登录，放行请求
        return true;
    }

}
