package com.riven.common.filter;


import com.alibaba.fastjson.JSON;
import com.riven.common.controller.Result;
import com.riven.common.utils.SpringUtil;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

//@Component  等申请安全网关再启用
@Slf4j
public class AuthFilter implements Filter {

    public static final String WIND_SESSION_ID = "wind.sessionid";


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest hsr = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            userId(hsr);
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("非内部用户访问", e);
            // 设置响应内容类型为JSON
            httpResponse.setContentType("application/json;charset=UTF-8");
            // 返回 JSON 实体
            httpResponse.getWriter().write(JSON.toJSONString(Result.ofFail("500","非内部用户访问")));
            // 返回，不再继续执行过滤器链
        }
    }

    @Override
    public void destroy() {

    }

    private void userId(HttpServletRequest hsr) throws Exception {

    }



}