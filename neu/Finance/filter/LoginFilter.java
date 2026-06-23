package com.neu.Finance.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.Finance.common.Result;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

public class LoginFilter implements Filter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String[] EXCLUDED_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/error"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(request, response);
            return;
        }

        String path = req.getRequestURI();
        for (String excluded : EXCLUDED_PATHS) {
            if (PATH_MATCHER.match(excluded, path)) {
                chain.doFilter(request, response);
                return;
            }
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            resp.setContentType("application/json;charset=UTF-8");
            Result<Object> errorResult = Result.error("未登录，请先登录"); // 只传 message
            ObjectMapper mapper = new ObjectMapper();
            resp.getWriter().write(mapper.writeValueAsString(errorResult));
            return;
        }

        chain.doFilter(request, response);
    }
}