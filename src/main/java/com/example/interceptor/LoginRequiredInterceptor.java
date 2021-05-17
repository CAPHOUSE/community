package com.example.interceptor;

import com.example.annotation.LginRequired;
import com.example.utils.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Value("${community.path.domain}")
    private String domain;


    /**
     * 设置标注注解拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//
        if (handler instanceof HandlerMethod){
           HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LginRequired annotation = method.getAnnotation(LginRequired.class);
            if (annotation != null && hostHolder.getUser() == null){
                response.sendRedirect(domain + "/login");
                return false;
            }
        }
        return true;
    }
}
